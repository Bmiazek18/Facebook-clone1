import asyncio
import os
import torch
from fastapi import FastAPI
from pydantic import BaseModel
from transformers import pipeline

app = FastAPI(title="Inference Service", version="1.0.0")

# Auto-detect GPU/CUDA
device_id = 0 if torch.cuda.is_available() else -1
print(f"Loading BERT toxicity model on device: {device_id}...")

bert_moderator = pipeline(
    "text-classification",
    model="gravitee-io/bert-tiny-toxicity",
    device=device_id
)

class InferenceRequest(BaseModel):
    text: str

# Queue for incoming requests: tuple of (text, Future)
request_queue = asyncio.Queue()

# Hyperparameters for Dynamic Batching
BATCH_SIZE = 32
BATCH_TIMEOUT_SEC = 0.005 # 5ms aggregation window

async def batch_processor():
    """
    Background worker that aggregates requests from request_queue
    and processes them as a single batch on the GPU.
    """
    while True:
        # Wait for at least one item to start a batch
        item = await request_queue.get()
        batch = [item]
        
        # Collect more items up to BATCH_SIZE within the BATCH_TIMEOUT_SEC window
        start_time = asyncio.get_event_loop().time()
        while len(batch) < BATCH_SIZE:
            time_left = BATCH_TIMEOUT_SEC - (asyncio.get_event_loop().time() - start_time)
            if time_left <= 0:
                break
            try:
                next_item = await asyncio.wait_for(request_queue.get(), timeout=time_left)
                batch.append(next_item)
            except asyncio.TimeoutError:
                break
        
        # Extract texts and futures
        texts = [x[0] for x in batch]
        futures = [x[1] for x in batch]
        
        try:
            # Run batch inference on GPU (highly optimized)
            results = bert_moderator(texts)
            
            # Distribute results to their respective futures
            for future, res in zip(futures, results):
                if not future.done():
                    future.set_result(res)
        except Exception as e:
            # Set exception for all futures in case of batch failure
            for future in futures:
                if not future.done():
                    future.set_exception(e)
        finally:
            for _ in range(len(batch)):
                request_queue.task_done()

@app.on_event("startup")
async def startup_event():
    # Start the background batch processor task
    asyncio.create_task(batch_processor())

@app.get("/health")
def health():
    return {"status": "UP", "device": "GPU" if device_id >= 0 else "CPU"}

@app.post("/predict")
async def predict(request: InferenceRequest):
    loop = asyncio.get_running_loop()
    future = loop.create_future()
    
    # Enqueue text along with its future
    await request_queue.put((request.text, future))
    
    try:
        result = await future
        return {
            "label": result["label"],
            "score": float(result["score"])
        }
    except Exception as e:
        return {"error": str(e)}

if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=8000)
