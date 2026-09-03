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

# --- INTELIGENTNY SCHEDULER VRAM DLA JEDNOCZESNYCH ZADAŃ (LLM + ABR) ---
HEAVY_GPU_LOCK = asyncio.Lock()          # Wyłączność dla ciężkich zadań generowania obrazów (Stable Diffusion)
TEXT_LLM_SEMAPHORE = asyncio.Semaphore(4) # Współbieżne zapytania LLM / BERT (do 4 na raz)
VIDEO_ABR_SEMAPHORE = asyncio.Semaphore(2) # Współbieżne transkodowanie wideo (do 2 na raz)

active_llm_tasks = 0
active_video_tasks = 0
active_sd_tasks = 0
waiting_sd_queue = 0

def get_vram_info():
    if DEVICE == "cuda" and torch.cuda.is_available():
        try:
            free_b, total_b = torch.cuda.mem_get_info()
            allocated_b = torch.cuda.memory_allocated()
            reserved_b = torch.cuda.memory_reserved()
            return {
                "free_mb": round(free_b / (1024 * 1024), 2),
                "total_mb": round(total_b / (1024 * 1024), 2),
                "allocated_mb": round(allocated_b / (1024 * 1024), 2),
                "reserved_mb": round(reserved_b / (1024 * 1024), 2)
            }
        except Exception:
            pass
    return {"free_mb": 8192.0, "total_mb": 8192.0, "allocated_mb": 0.0, "reserved_mb": 0.0}

def ensure_vram_headroom(required_mb: float = 3500.0):
    """Zwalnia cache PyTorch jeśli wolny VRAM jest poniżej wymaganego progu."""
    if DEVICE == "cuda" and torch.cuda.is_available():
        vram = get_vram_info()
        if vram["free_mb"] < required_mb:
            print(f"[VRAM SCHEDULER] Zwalniam cache VRAM (obecnie wolne: {vram['free_mb']} MB, wymagane: {required_mb} MB)...")
            torch.cuda.empty_cache()

# --- KOLEJKA BATCH DLA BERT TOXICITY ---
request_queue = asyncio.Queue()
BATCH_SIZE = 32
BATCH_TIMEOUT_SEC = 0.005

async def batch_processor():
    global active_llm_tasks
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
        
        async with TEXT_LLM_SEMAPHORE:
            active_llm_tasks += 1
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
                active_llm_tasks = max(0, active_llm_tasks - 1)
                for _ in range(len(batch)):
                    request_queue.task_done()

@app.on_event("startup")
async def startup_event():
    asyncio.create_task(batch_processor())

@app.get("/health")
def health():
    vram = get_vram_info()
    return {
        "status": "UP",
        "device": DEVICE,
        "gpu_available": torch.cuda.is_available() or torch.backends.mps.is_available(),
        "vram_free_mb": vram["free_mb"],
        "vram_allocated_mb": vram["allocated_mb"]
    }

@app.get("/queue/status")
@app.get("/gpu/status")
def get_gpu_queue_status():
    vram = get_vram_info()
    device_name = "CPU"
    if DEVICE == "cuda" and torch.cuda.is_available():
        device_name = torch.cuda.get_device_name(0)

    # Sprawdzamy czy VRAM pozwala na równoczesne zadania
    can_run_concurrent_abr = vram["free_mb"] >= 500.0

    return {
        "device": DEVICE,
        "device_name": device_name,
        "vram_free_mb": vram["free_mb"],
        "vram_allocated_mb": vram["allocated_mb"],
        "vram_reserved_mb": vram["reserved_mb"],
        "total_vram_mb": vram["total_mb"],
        "active_tasks": {
            "llm_text": active_llm_tasks,
            "video_abr": active_video_tasks,
            "image_sd": active_sd_tasks
        },
        "waiting_in_sd_queue": waiting_sd_queue,
        "parallel_abr_and_llm_allowed": can_run_concurrent_abr
    }

# --- ENDPOINT 1: MODERACJA TEKSTU I LLM (Działa równolegle z ABR) ---
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
            "score": float(result["score"]),
            "duration_ms": round(duration * 1000, 2)
        }
    except Exception as e:
        return {"error": str(e)}

# --- ENDPOINT 2: GENEROWANIE OBRAZÓW STABLE DIFFUSION (Wymaga rezerwacji dużej pamięci VRAM) ---
@app.post("/generate-image")
@app.post("/generate")
async def generate_image(request: GenerationRequest):
    global waiting_sd_queue, active_sd_tasks
    waiting_sd_queue += 1
    start_time = asyncio.get_event_loop().time()

    # Oczekiwanie na wyłączność VRAM dla Stable Diffusion
    async with HEAVY_GPU_LOCK:
        waiting_sd_queue = max(0, waiting_sd_queue - 1)
        active_sd_tasks += 1
        ensure_vram_headroom(required_mb=3500.0)
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
            active_sd_tasks = max(0, active_sd_tasks - 1)
            if DEVICE == "cuda" and torch.cuda.is_available():
                if GPU_VRAM_ALLOCATED_BYTES:
                    GPU_VRAM_ALLOCATED_BYTES.set(torch.cuda.memory_allocated())
                torch.cuda.empty_cache()
            elif DEVICE == "mps" and hasattr(torch, "mps") and hasattr(torch.mps, "empty_cache"):
                torch.mps.empty_cache()

if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=8000)
