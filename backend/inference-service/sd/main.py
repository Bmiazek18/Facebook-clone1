import os
import sys
from pathlib import Path
sys.path.append(str(Path(__file__).resolve().parent.parent.parent))
try:
    from config.sentry import init_sentry
    init_sentry(service_name="image-generator")
except ImportError:
    pass

import torch
import numpy as np
from PIL import Image, ImageDraw
import base64
from io import BytesIO
from fastapi import FastAPI, HTTPException
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel
from typing import List, Optional
import traceback

# Importy Twoich modułów Stable Diffusion
import model_loader
import pipeline
from transformers import CLIPTokenizer

app = FastAPI(title="Meta AI - Image Generator & Inpainting", version="1.0.0")

# Konfiguracja CORS dla komunikacji z Vue
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_methods=["*"],
    allow_headers=["*"],
)


# --- MODELE DANYCH ---

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


# --- NARZĘDZIA POMOCNICZE ---

def base64_to_pil(b64_str: str) -> Image.Image:
    try:
        # Obsługa nagłówka data:image/...;base64,
        if "base64," in b64_str:
            b64_str = b64_str.split("base64,")[1]

        # Dekodowanie ciągu znaków do bajtów
        img_data = base64.b64decode(b64_str)

        # Otwarcie obrazu i wymuszenie formatu RGB
        return Image.open(BytesIO(img_data)).convert("RGB")
    except Exception as e:
        print(f"Błąd dekodowania obrazu: {e}")
        raise ValueError("Niepoprawny format obrazu base64")


def pil_to_base64(img: Image.Image) -> str:
    buffered = BytesIO()
    img.save(buffered, format="PNG")
    return base64.b64encode(buffered.getvalue()).decode()


# --- INICJALIZACJA MODELI ---

# Auto-detekcja sprzętu: CUDA (NVIDIA GPU) -> MPS (Apple Silicon Mac) -> CPU
DEVICE = os.getenv("DEVICE", "cuda" if torch.cuda.is_available() else ("mps" if torch.backends.mps.is_available() else "cpu"))
print(f"Inicjalizacja urządzenia: {DEVICE}")

# Ścieżki muszą być poprawne względem miejsca uruchomienia main.py
tokenizer = CLIPTokenizer.from_pretrained("../data/", local_files_only=True)
models = model_loader.preload_models_from_standard_weights("../data/v1-5-pruned-emaonly.ckpt", DEVICE)


# --- ENDPOINT GENEROWANIA ---

@app.post("/generate")
async def generate_image(request: GenerationRequest):
    try:
        # Pobranie danych pierwszego obrazu
        img_item = request.images[0]

        # Konwersja i skalowanie do wymogów modelu (512x512)
        input_image = base64_to_pil(img_item.image_base64).resize((512, 512))

        # Tworzenie czarnej maski
        mask = Image.new("L", (512, 512), 0)
        if img_item.mask_coords:
            draw = ImageDraw.Draw(mask)
            c = img_item.mask_coords
            # PIL wymaga liczb całkowitych dla prostokąta [left, top, right, bottom]
            draw.rectangle([
                int(c.x),
                int(c.y),
                int(c.x + c.w),
                int(c.y + c.h)
            ], fill=255)

        # Łączenie promptu głównego z opisem obszaru
        final_prompt = f"{request.prompt}, {img_item.description}"

        # Wywołanie Twojego pipeline'u generującego
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

        # Konwersja wyniku (numpy array) z powrotem na obraz PIL
        result_img = Image.fromarray(output_array)

        return {
            "status": "success",
            "image": pil_to_base64(result_img)
        }

    except Exception as e:
        print("--- POWAŻNY BŁĄD BACKENDU ---")
        traceback.print_exc()
        print("------------------------------")
        raise HTTPException(status_code=500, detail=str(e))
    finally:
        # Zwolnienie pamięci VRAM na GPU / MPS po zakończeniu generowania
        if DEVICE == "cuda" and torch.cuda.is_available():
            torch.cuda.empty_cache()
        elif DEVICE == "mps" and hasattr(torch, "mps") and hasattr(torch.mps, "empty_cache"):
            torch.mps.empty_cache()


if __name__ == "__main__":
    import uvicorn

    # Uruchomienie na porcie 8058, zgodnie z Twoją konfiguracją Vue
    uvicorn.run(app, host="0.0.0.0", port=8058)