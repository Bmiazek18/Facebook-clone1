# 🧠 Meta AI Backend Services (Python)

Serwis backendowy Meta AI dla klona Facebooka. Składa się z dwóch modułów opartych na frameworku FastAPI:

## 1. Moduł RAG & Agent Tools (`/rag`)
- **Port:** `8000`
- **Silnik:** LangChain + LangGraph StateGraph + Ollama / Local LLM (Qwen 2.5:7b)
- **Funkcjonalności:**
  - `POST /process-chat` – Strumieniowanie odpowiedzi AI (SSE / Plain text).
  - `GET /chat-threads` – Pobieranie listy wątków sesji.
  - `GET /chat-history/{thread_id}` – Pobieranie historii wiadomości z bazy SQLite (`chat_history.db`).
  - **Narzędzia Agenta:**
    - `generate_and_save_chart` – Automatyczne generowanie wykresów Matplotlib serwowanych pod `/generated_charts/`.
    - `web_search` – Wyszukiwarka internetowa DuckDuckGo.
    - `get_current_date_and_time` – Aktualny czas systemowy.

### Uruchomienie RAG:
```bash
cd rag
python main.py
```

---

## 2. Moduł Stable Diffusion Image Editor / Inpainting (`/ImageGenerator/sd`)
- **Port:** `8058`
- **Silnik:** PyTorch (MPS / CUDA / CPU) + Stable Diffusion v1.5 + DDPM Sampler
- **Funkcjonalności:**
  - `POST /generate` – Edycja obrazu (Inpainting) na podstawie maski ROI wyznaczonej w interfejsie oraz promptu tekstowego.

### Uruchomienie Image Generatora:
```bash
cd ImageGenerator/sd
python main.py
```

---

## 📦 Instalacja zależności
```bash
pip install -r requirements.txt
```
