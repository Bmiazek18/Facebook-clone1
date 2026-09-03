import asyncio
import os
import sys
import base64
import traceback
from io import BytesIO
from typing import List, Optional

import torch
import numpy as np
from PIL import Image, ImageDraw
from fastapi import FastAPI, HTTPException
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel
from transformers import pipeline, CLIPTokenizer

# Inicjalizacja Sentry
sentry_dsn = os.getenv("SENTRY_DSN", "")
if sentry_dsn:
    try:
        import sentry_sdk
        from sentry_sdk.integrations.fastapi import FastApiIntegration
        sentry_sdk.init(
            dsn=sentry_dsn,
            integrations=[FastApiIntegration()],
            traces_sample_rate=float(os.getenv("SENTRY_TRACES_SAMPLE_RATE", "1.0")),
            environment=os.getenv("ENVIRONMENT", "development"),
            release="inference-service@1.0.0"
        )
        print("[SENTRY] Pomyślnie zainicjalizowano Sentry dla inference-service.")
    except Exception as e:
        print(f"[SENTRY] Błąd inicjalizacji Sentry: {e}")

try:
    from config.observability import (
        setup_inference_observability,
        SD_GENERATION_DURATION,
        BERT_MODERATION_DURATION,
        GPU_VRAM_ALLOCATED_BYTES
    )
except ImportError:
    setup_inference_observability = lambda app: None
    SD_GENERATION_DURATION = None
    BERT_MODERATION_DURATION = None
    GPU_VRAM_ALLOCATED_BYTES = None

app = FastAPI(title="Unified GPU Inference Service", version="1.0.0")

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_methods=["*"],
    allow_headers=["*"],
)

setup_inference_observability(app)

# Auto-detect GPU/CUDA/MPS
DEVICE = os.getenv("DEVICE", "cuda" if torch.cuda.is_available() else ("mps" if torch.backends.mps.is_available() else "cpu"))
device_id = 0 if (DEVICE == "cuda" and torch.cuda.is_available()) else -1
print(f"[GPU INFERENCE] Uruchamianie na urządzeniu: {DEVICE} (device_id: {device_id})")

# 1. BERT TOXICITY MODERATOR
print("Ładowanie modelu BERT Toxicity...")
try:
    bert_moderator = pipeline(
        "text-classification",
        model="gravitee-io/bert-tiny-toxicity",
        device=device_id
    )
except Exception as e:
    print(f"Ostrzeżenie: Nie udało się załadować bert_moderator: {e}")
    bert_moderator = None

# 2. STABLE DIFFUSION MODELS (LAZY LOADING)
sd_tokenizer = None
sd_models = None
sys.path.append(os.path.join(os.path.dirname(__file__), "sd"))

def get_sd_models():
    global sd_tokenizer, sd_models
    if sd_models is None:
        import model_loader
        data_dir = os.path.join(os.path.dirname(__file__), "data")
        ckpt_path = os.getenv("CHECKPOINT_PATH", os.path.join(data_dir, "v1-5-pruned-emaonly.ckpt"))
        print(f"[SD] Ładowanie wag Stable Diffusion z {ckpt_path} na {DEVICE}...")
        sd_tokenizer = CLIPTokenizer.from_pretrained(data_dir, local_files_only=True)
        sd_models = model_loader.preload_models_from_standard_weights(ckpt_path, DEVICE)
    return sd_tokenizer, sd_models

# --- MODELE DANYCH ---
class InferenceRequest(BaseModel):
    text: str

class MaskCoords(BaseModel):
    x: float
    y: float
    w: float
    h: float

class ImageAttachment(BaseModel):
    image_base64: str
    description: Optional[str] = ""
    mask_coords: Optional[MaskCoords] = None

class GenerationRequest(BaseModel):
    prompt: str
    images: List[ImageAttachment]

# --- POMOCNICZE FUNKCJE OBRAZÓW ---
def base64_to_pil(b64_str: str) -> Image.Image:
    if "base64," in b64_str:
        b64_str = b64_str.split("base64,")[1]
    img_data = base64.b64decode(b64_str)
    return Image.open(BytesIO(img_data)).convert("RGB")

def pil_to_base64(img: Image.Image) -> str:
    buffered = BytesIO()
    img.save(buffered, format="PNG")
    return base64.b64encode(buffered.getvalue()).decode()

# --- ZARZĄDZANIE KOLEJKĄ I BLOKADA GPU (DEDYKOWANY WORKER GPU) ---
GPU_LOCK = asyncio.Lock()
active_tasks_count = 0
waiting_queue_count = 0

# --- KOLEJKA BATCH DLA BERT TOXICITY ---
request_queue = asyncio.Queue()
BATCH_SIZE = 32
BATCH_TIMEOUT_SEC = 0.005

async def batch_processor():
    while True:
        item = await request_queue.get()
        batch = [item]
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
        
        texts = [x[0] for x in batch]
        futures = [x[1] for x in batch]
        
        try:
            if bert_moderator:
                results = bert_moderator(texts)
                for future, res in zip(futures, results):
                    if not future.done():
                        future.set_result(res)
            else:
                for future in futures:
                    if not future.done():
                        future.set_result({"label": "non-toxic", "score": 0.0})
        except Exception as e:
            for future in futures:
                if not future.done():
                    future.set_exception(e)
        finally:
            for _ in range(len(batch)):
                request_queue.task_done()

@app.on_event("startup")
async def startup_event():
    asyncio.create_task(batch_processor())

@app.get("/health")
def health():
    return {
        "status": "UP",
        "device": DEVICE,
        "gpu_available": torch.cuda.is_available() or torch.backends.mps.is_available()
    }

@app.get("/queue/status")
@app.get("/gpu/status")
def get_gpu_queue_status():
    vram_allocated = 0
    vram_reserved = 0
    total_vram = 0
    device_name = "CPU"
    
    if DEVICE == "cuda" and torch.cuda.is_available():
        device_name = torch.cuda.get_device_name(0)
        vram_allocated = round(torch.cuda.memory_allocated(0) / (1024 * 1024), 2)
        vram_reserved = round(torch.cuda.memory_reserved(0) / (1024 * 1024), 2)
        total_vram = round(torch.cuda.get_device_properties(0).total_memory / (1024 * 1024), 2)

    return {
        "device": DEVICE,
        "device_name": device_name,
        "active_gpu_task": GPU_LOCK.locked(),
        "waiting_in_queue": waiting_queue_count,
        "vram_allocated_mb": vram_allocated,
        "vram_reserved_mb": vram_reserved,
        "total_vram_mb": total_vram
    }

# --- ENDPOINT 1: MODERACJA TEKSTU (BERT) ---
@app.post("/predict")
async def predict(request: InferenceRequest):
    loop = asyncio.get_running_loop()
    future = loop.create_future()
    start_time = asyncio.get_event_loop().time()
    await request_queue.put((request.text, future))
    try:
        result = await future
        duration = asyncio.get_event_loop().time() - start_time
        if BERT_MODERATION_DURATION:
            BERT_MODERATION_DURATION.observe(duration)
        return {
            "label": result["label"],
            "score": float(result["score"])
        }
    except Exception as e:
        return {"error": str(e)}

# --- ENDPOINT 2: GENEROWANIE I INPAINTING OBRAZÓW (STABLE DIFFUSION) ---
@app.post("/generate-image")
@app.post("/generate")
async def generate_image(request: GenerationRequest):
    global waiting_queue_count, active_tasks_count
    waiting_queue_count += 1
    start_time = asyncio.get_event_loop().time()
    
    # Bezpieczna synchronizacja: tylko jedno zadanie generowania GPU na raz (ochrona przed OOM)
    async with GPU_LOCK:
        waiting_queue_count = max(0, waiting_queue_count - 1)
        active_tasks_count += 1
        try:
            tokenizer, models = get_sd_models()
            import pipeline

            img_item = request.images[0]
            input_image = base64_to_pil(img_item.image_base64).resize((512, 512))

            mask = Image.new("L", (512, 512), 0)
            if img_item.mask_coords:
                draw = ImageDraw.Draw(mask)
                c = img_item.mask_coords
                draw.rectangle([
                    int(c.x),
                    int(c.y),
                    int(c.x + c.w),
                    int(c.y + c.h)
                ], fill=255)

            final_prompt = f"{request.prompt}, {img_item.description}".strip(", ")

            output_array = pipeline.generate(
                prompt=final_prompt,
                uncond_prompt="blurry, low quality, distorted, deformed",
                input_image=input_image,
                mask_image=mask,
                strength=0.99,
                do_cfg=True,
                cfg_scale=12,
                sampler_name="ddpm",
                n_inference_steps=50,
                seed=42,
                models=models,
                device=DEVICE,
                idle_device="cpu",
                tokenizer=tokenizer,
            )

            result_img = Image.fromarray(output_array)
            duration = asyncio.get_event_loop().time() - start_time
            if SD_GENERATION_DURATION:
                SD_GENERATION_DURATION.observe(duration)

            return {
                "status": "success",
                "image": pil_to_base64(result_img),
                "duration_seconds": round(duration, 2)
            }
        except Exception as e:
            print("--- BŁĄD INFERENCE GPU ---")
            traceback.print_exc()
            raise HTTPException(status_code=500, detail=str(e))
        finally:
            active_tasks_count = max(0, active_tasks_count - 1)
            if DEVICE == "cuda" and torch.cuda.is_available():
                if GPU_VRAM_ALLOCATED_BYTES:
                    GPU_VRAM_ALLOCATED_BYTES.set(torch.cuda.memory_allocated())
                torch.cuda.empty_cache()
            elif DEVICE == "mps" and hasattr(torch, "mps") and hasattr(torch.mps, "empty_cache"):
                torch.mps.empty_cache()

if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=8000)
