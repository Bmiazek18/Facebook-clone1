import os
import sys
import time
from pathlib import Path
sys.path.append(str(Path(__file__).resolve().parent.parent))
try:
    from config.sentry import init_sentry
    init_sentry(service_name="rag")
except ImportError:
    pass

try:
    from config.observability import (
        setup_observability,
        get_langfuse_callback,
        tracer,
        LLM_REQUESTS_TOTAL,
        LLM_TTFT_SECONDS,
        LLM_GENERATION_DURATION,
        TOOL_EXECUTIONS_TOTAL
    )
except ImportError:
    setup_observability = lambda app: None
    get_langfuse_callback = lambda **kw: None
    tracer = None
    LLM_REQUESTS_TOTAL = None
    LLM_TTFT_SECONDS = None
    LLM_GENERATION_DURATION = None
    TOOL_EXECUTIONS_TOTAL = None

import base64
import hashlib
import re
import uvicorn
import json
import sqlite3
from fastapi import FastAPI, Request, HTTPException
from fastapi.responses import StreamingResponse
from fastapi.middleware.cors import CORSMiddleware
from fastapi.staticfiles import StaticFiles
from datetime import datetime
from contextlib import asynccontextmanager

import matplotlib
matplotlib.use('Agg')
import matplotlib.pyplot as plt

from langchain_openai import ChatOpenAI
from langchain.tools import tool
from langchain_core.messages import HumanMessage, BaseMessage
from langchain_community.tools import DuckDuckGoSearchRun

from typing import Annotated, Sequence, List, Literal, TypedDict
from pydantic import BaseModel, Field
from langgraph.graph import StateGraph, START, END
from langgraph.graph.message import add_messages
from langgraph.prebuilt import ToolNode
from contextlib import asynccontextmanager

try:
    from langgraph.checkpoint.sqlite.aio import AsyncSqliteSaver
except Exception:
    try:
        from langgraph_checkpoint_sqlite.aio import AsyncSqliteSaver
    except Exception:
        try:
            from langgraph.checkpoint.sqlite import SqliteSaver as AsyncSqliteSaver
        except Exception:
            from langgraph.checkpoint.memory import MemorySaver
            class AsyncSqliteSaver(MemorySaver):
                @classmethod
                def from_conn_string(cls, conn_string):
                    @asynccontextmanager
                    async def _cm():
                        yield cls()
                    return _cm()

# --- GLOBALNA ZMIENNA DLA AGENTA ---
agent_executor = None
DB_PATH = "chat_history.db"
CHARTS_DIR = "generated_charts"  # Zmienna pomocnicza dla czytelności

# --- CONFIG INTERFEJSÓW KLIENTÓW ---
OLLAMA_BASE_URL = os.getenv("OLLAMA_BASE_URL", "http://localhost:11434/v1")
OLLAMA_MODEL = os.getenv("OLLAMA_MODEL", "qwen2.5:7b")

local_llm = ChatOpenAI(
    base_url=OLLAMA_BASE_URL,
    api_key="ollama",
    model=OLLAMA_MODEL,
    temperature=0.0
)

REDIS_HOST = os.getenv("REDIS_HOST", "redis")
_raw_redis_port = os.getenv("REDIS_PORT", "6379")
if "://" in _raw_redis_port:
    _raw_redis_port = _raw_redis_port.split(":")[-1]
try:
    REDIS_PORT = int(_raw_redis_port)
except Exception:
    REDIS_PORT = 6379
REDIS_CACHE_TTL = int(os.getenv("SEMANTIC_CACHE_TTL_SEC", "7200")) # 2h TTL

try:
    import redis.asyncio as aioredis
    redis_cache = aioredis.Redis(host=REDIS_HOST, port=REDIS_PORT, decode_responses=True)
    print(f"[SEMANTIC CACHE] Połączono z Redis cache na {REDIS_HOST}:{REDIS_PORT}")
except Exception as e:
    print(f"[SEMANTIC CACHE] Ostrzeżenie: brak połączenia z Redis: {e}")
    redis_cache = None


# --- DEFINICJA STANU GRAFU ---
class AgentState(TypedDict):
    messages: Annotated[Sequence[BaseMessage], add_messages]
    chat_title: str
    model_mode: str


# --- STRUKTURA PYDANTIC DLA STRUKTURYZOWANEGO REZULTATU ---
class ChartConfig(BaseModel):
    title: str = Field(
        description="Tytuł wykresu opisujący co przedstawiają dane, np. 'Cena chleba w weekend'"
    )
    type: Literal["bar", "line", "pie"] = Field(
        description="Typ wykresu: 'bar' (słupkowy), 'line' (liniowy) lub 'pie' (kołowy)"
    )
    labels: List[str] = Field(
        description="Etykiety dla osi X lub sekcji wykresu kołowego, np. ['Piątek', 'Sobota']"
    )
    values: List[float] = Field(
        description="Wartości liczbowe odpowiadające etykietom (musi być ich dokładnie tyle samo co etykiet)"
    )
    dark_mode: bool = Field(
        default=True,
        description="True jeśli wykres ma pasować do ciemnego motywu aplikacji, False dla jasnego motywu"
    )


# --- FUNKCJA GENERUJĄCA TYTUŁ CZATU ---
async def generate_chat_title(user_query: str, assistant_response: str) -> str:
    try:
        prompt = (
            f"Na podstawie poniższego pytania użytkownika oraz odpowiedzi asystenta, "
            f"wygeneruj bardzo krótki, maksymalnie 3-5 wyrazowy tytuł dla tej konwersacji w języku polskim. "
            f"Tytuł ma streszczać esencję rozmowy. Nie używaj cudzysłowów, ponumerowań ani dodatkowych słów. "
            f"Zwróć wyłącznie sam tekst tytułu.\n\n"
            f"Pytanie użytkownika: {user_query}\n\n"
            f"Odpowiedź asystenta: {assistant_response}"
        )
        title_llm = ChatOpenAI(
            base_url=OLLAMA_BASE_URL,
            api_key="ollama",
            model=OLLAMA_MODEL,
            temperature=0.5
        )
        response = await title_llm.ainvoke([HumanMessage(content=prompt)])
        title = response.content.strip().strip('"').strip("'").strip()
        return title if title else "Nowa rozmowa"
    except Exception as e:
        print(f"Błąd podczas generowania tytułu: {e}")
        return "Nowa rozmowa"


# --- IMPORT NARZĘDZI AGENTA ---
from tools import agent_tools


# --- WĘZŁY GRAFU ---
async def call_model(state: AgentState):
    messages = state["messages"]
    mode = state.get("model_mode", "Flash")

    if mode == "Thinking":
        system_content = (
            "Jesteś zaawansowanym asystentem AI. Przed sformułowaniem finalnej odpowiedzi "
            "zawsze dokładnie przemyśl i przeanalizuj problem krok po kroku w sekcji <think>...</think>. "
            "Po zamknięciu tagu </think> przedstaw ostateczną, czytelną odpowiedź w języku polskim. "
            "Jeśli użytkownik prosi o wykres lub dane, użyj narzędzia `generate_and_save_chart`. "
            "Jeśli użytkownik pyta o wgrane dokumenty lub PDF, użyj narzędzia `search_pdf_knowledge_base`."
        )
    else:
        system_content = (
            "Jesteś pomocnym asystentem, który odpowiada wyłącznie w języku polskim. "
            "Odpowiadaj bezpośrednio, precyzyjnie i zwięźle. "
            "Jeśli użytkownik prosi o wykres, diagram lub zestawienie danych, użyj narzędzia `generate_and_save_chart`. "
            "Jeśli użytkownik pyta o wgrane dokumenty lub PDF, użyj narzędzia `search_pdf_knowledge_base`."
        )

    system_msg = {"role": "system", "content": system_content}
    filtered_messages = [m for m in messages if getattr(m, "type", "") != "system"]
    messages = [system_msg] + filtered_messages

    model_with_tools = local_llm.bind_tools(agent_tools)
    response = await model_with_tools.ainvoke(messages)
    return {"messages": [response]}



async def route_or_generate_title(state: AgentState):
    if state.get("chat_title") and state["chat_title"] != "Nowa rozmowa":
        return {}

    user_messages = [m for m in state["messages"] if m.type == "human"]
    ai_messages = [m for m in state["messages"] if m.type == "ai" and m.content]

    if len(user_messages) == 1 and ai_messages:
        user_query = user_messages[0].content
        assistant_response = ai_messages[-1].content
        title = await generate_chat_title(user_query, assistant_response)
        print(f"[METADATA] Wygenerowano tytuł: '{title}'")
        return {"chat_title": title}

    return {}


def should_continue(state: AgentState) -> Literal["tools", "generate_title"]:
    last_message = state["messages"][-1]
    if last_message.tool_calls:
        return "tools"
    return "generate_title"


# --- ASYNCHRONICZNY LIFESPAN I INICJALIZACJA GRAFU ---
@asynccontextmanager
async def lifespan(app: FastAPI):
    global agent_executor
    async with AsyncSqliteSaver.from_conn_string(DB_PATH) as chat_memory_db:
        print("Uruchamianie StateGraph z asynchroniczną bazą...")
        if hasattr(chat_memory_db, "setup"):
            try:
                await chat_memory_db.setup()
            except Exception as e:
                print(f"[LIFESPAN] setup() checkpointer error: {e}")

        workflow = StateGraph(AgentState)

        workflow.add_node("agent", call_model)
        workflow.add_node("tools", ToolNode(agent_tools))
        workflow.add_node("generate_title", route_or_generate_title)

        workflow.add_edge(START, "agent")
        workflow.add_conditional_edges("agent", should_continue)
        workflow.add_edge("tools", "agent")
        workflow.add_edge("generate_title", END)

        agent_executor = workflow.compile(checkpointer=chat_memory_db)
        yield
    print("Połączenie z bazą zostało zamknięte.")


app = FastAPI(title="Meta AI - RAG & Agent Orchestrator", version="1.0.0", lifespan=lifespan)

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_methods=["*"],
    allow_headers=["*"],
)

# Inicjalizacja Prometheus & OpenTelemetry
setup_observability(app)

# --- SERWOWANIE WYKRESÓW Z MINIO I LOKALNEGO CACHE ---
if not os.path.exists(CHARTS_DIR):
    os.makedirs(CHARTS_DIR)

@app.get("/generated_charts/{filename}")
@app.get("/api/generated_charts/{filename}")
async def get_generated_chart(filename: str):
    local_path = os.path.join(CHARTS_DIR, filename)
    if os.path.exists(local_path):
        return FileResponse(local_path, media_type="image/png")
    try:
        from rag.minio_client import get_chart_from_minio
        resp = get_chart_from_minio(filename)
        data = resp.read()
        resp.close()
        resp.release_conn()
        return Response(content=data, media_type="image/png")
    except Exception as e:
        raise HTTPException(status_code=404, detail=f"Chart '{filename}' not found in MinIO: {e}")


# --- ENDPOINT: LISTA WĄTKÓW CZATU ---
@app.get("/chat-threads")
async def get_chat_threads():
    if not os.path.exists(DB_PATH):
        return {"threads": []}

    try:
        conn = sqlite3.connect(DB_PATH)
        cursor = conn.cursor()
        cursor.execute("SELECT name FROM sqlite_master WHERE type='table' AND name='checkpoints'")
        if not cursor.fetchone():
            conn.close()
            return {"threads": []}
        cursor.execute("SELECT thread_id FROM checkpoints GROUP BY thread_id ORDER BY max(checkpoint_id) DESC")
        rows = cursor.fetchall()
        conn.close()

        threads_list = []
        for row in rows:
            thread_id = row[0]
            state = await agent_executor.aget_state({"configurable": {"thread_id": thread_id}})
            title = state.values.get("chat_title", "Nowa rozmowa") if state and state.values else "Nowa rozmowa"

            threads_list.append({
                "thread_id": thread_id,
                "title": title
            })

        return {"threads": threads_list}
    except Exception as e:
        print(f"Błąd podczas pobierania listy wątków: {e}")
        raise HTTPException(status_code=500, detail=str(e))


# --- ENDPOINT: HISTORIA CZATU ---
@app.get("/chat-history/{thread_id}")
async def get_chat_history(thread_id: str):
    if agent_executor is None:
        raise HTTPException(status_code=503, detail="Agent nie jest jeszcze zainicjalizowany.")

    config = {"configurable": {"thread_id": thread_id}}
    try:
        state = await agent_executor.aget_state(config)

        if not state or "messages" not in state.values:
            return {"messages": [], "title": "Nowa rozmowa"}

        chat_title = state.values.get("chat_title", "Nowa rozmowa")
        formatted_messages = []
        base_timestamp = int(datetime.now().timestamp() * 1000)

        for index, msg in enumerate(state.values["messages"]):
            if msg.type in ["human", "ai"]:
                role = "user" if msg.type == "human" else "assistant"

                if msg.type == "ai" and not msg.content and hasattr(msg, 'tool_calls') and msg.tool_calls:
                    continue

                formatted_messages.append({
                    "id": base_timestamp + index,
                    "role": role,
                    "content": str(msg.content)
                })

        return {"messages": formatted_messages, "title": chat_title}
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Błąd odczytu historii: {str(e)}")


# --- ENDPOINT: PROCESOWANIE I STRUMIENIOWANIE CZATU ---
@app.post("/process-chat")
async def process_chat(request: Request):
    data = await request.json()
    user_query = data.get("query", "")
    thread_id = data.get("thread_id", "default_session")
    model_mode = data.get("model", "Flash")

    # Langfuse Callback Tracing
    langfuse_handler = get_langfuse_callback(session_id=thread_id)
    callbacks = [langfuse_handler] if langfuse_handler else []

    config = {
        "configurable": {"thread_id": thread_id},
        "callbacks": callbacks
    }

    start_time = time.time()
    first_token_recorded = False

    # Normalizacja i klucz Semantic Cache w Redis
    normalized_query = user_query.strip().lower()
    query_hash = hashlib.sha256(normalized_query.encode("utf-8")).hexdigest()
    cache_key = f"semantic_ai_cache:{model_mode}:{query_hash}"

    async def stream():
        nonlocal first_token_recorded
        
        # 1. Sprawdzenie Semantic Cache w Redis (Odpowiedź w 1-2 ms bez angażowania GPU)
        if redis_cache and len(normalized_query) > 3:
            try:
                cached_text = await redis_cache.get(cache_key)
                if cached_text:
                    print(f"[SEMANTIC CACHE HIT] Zwracam zcache'owaną odpowiedź dla '{normalized_query[:30]}...' w 2 ms")
                    if LLM_REQUESTS_TOTAL:
                        LLM_REQUESTS_TOTAL.labels(model=OLLAMA_MODEL, status="cache_hit").inc()
                    yield cached_text
                    return
            except Exception as cache_err:
                print(f"[SEMANTIC CACHE ERROR]: {cache_err}")

        # 2. Generowanie przez LLM / Agenta gdy brak w cache (Cache MISS)
        full_response_chunks = []
        try:
            inputs = {
                "messages": [("user", user_query)],
                "model_mode": model_mode
            }

            async for msg, metadata in agent_executor.astream(
                    inputs,
                    config=config,
                    stream_mode="messages"
            ):
                # Zapis TTFT (Time To First Token) dla metryk Prometheusa
                if not first_token_recorded and msg.content:
                    ttft = time.time() - start_time
                    first_token_recorded = True
                    if LLM_TTFT_SECONDS:
                        LLM_TTFT_SECONDS.labels(model=OLLAMA_MODEL).observe(ttft)

                # 1. DEBUGOWANIE W KONSOLI SERWERA
                if metadata.get("langgraph_node") == "tools":
                    print("\n" + "=" * 60)
                    print(f" LOG: ODPOWIEDŹ Z NARZĘDZIA PRZEKAZANA DO LLM:")
                    print("=" * 60)
                    print(msg.content)
                    print("=" * 60 + "\n")

                # 2. STRUMIEŃ DLA KLIENTA: Informacja o uruchomieniu narzędzia
                if metadata.get("langgraph_node") == "agent" and hasattr(msg, "tool_calls") and msg.tool_calls:
                    for tool_call in msg.tool_calls:
                        tool_name = tool_call.get("name")
                        chunk = f"\n*[Asystent uruchamia narzędzie: {tool_name}...]*\n\n"
                        full_response_chunks.append(chunk)
                        yield chunk

                # 3. STRUMIEŃ DLA KLIENTA: Renderowanie tekstu lub linku do pliku statycznego
                if msg.content and metadata.get("langgraph_node") == "agent":
                    if hasattr(msg, "tool_calls") and msg.tool_calls:
                        continue
                    if msg.content.strip().startswith('{"name":') or msg.content.strip().startswith('{"arguments":'):
                        continue

                    text_chunk = str(msg.content)

                    pattern = r"__CHART_FILE__:(.*?)__"
                    match = re.search(pattern, text_chunk)

                    if match:
                        full_tag = match.group(0)
                        filename = os.path.basename(match.group(1))
                        img_url = f"/api/generated_charts/{filename}"
                        img_md = f"\n![Wykres]({img_url})\n"
                        rendered = text_chunk.replace(full_tag, img_md)
                        full_response_chunks.append(rendered)
                        yield rendered
                    else:
                        full_response_chunks.append(text_chunk)
                        yield text_chunk

            # Zapis pełnej wygenerowanej odpowiedzi do Redis Semantic Cache z czasem życia TTL
            if redis_cache and full_response_chunks and len(full_response_chunks) > 0:
                try:
                    full_text = "".join(full_response_chunks)
                    if not full_text.startswith("❌"):
                        await redis_cache.set(cache_key, full_text, ex=REDIS_CACHE_TTL)
                        print(f"[SEMANTIC CACHE SAVED] Zapisano odpowiedź w Redis dla '{normalized_query[:30]}...' (TTL: {REDIS_CACHE_TTL}s)")
                except Exception as save_err:
                    print(f"[SEMANTIC CACHE SAVE ERROR]: {save_err}")

            # Rejestracja całkowitego czasu generowania w Prometheus
            total_duration = time.time() - start_time
            if LLM_GENERATION_DURATION:
                LLM_GENERATION_DURATION.labels(model=OLLAMA_MODEL).observe(total_duration)
            if LLM_REQUESTS_TOTAL:
                LLM_REQUESTS_TOTAL.labels(model=OLLAMA_MODEL, status="success").inc()

        except Exception as e:
            if LLM_REQUESTS_TOTAL:
                LLM_REQUESTS_TOTAL.labels(model=OLLAMA_MODEL, status="error").inc()
            print(f"\n[CRITICAL ERROR IN STREAM]: {str(e)}")
            import traceback
            traceback.print_exc()
            yield f"\n\n❌ **Wystąpił błąd podczas generowania odpowiedzi:** {str(e)}\n"

    return StreamingResponse(stream(), media_type="text/plain")


# --- ENDPOINT: DELEGACJA DO GPU INFERENCE-SERVICE (STABLE DIFFUSION) ---
INFERENCE_SERVICE_URL = os.getenv("INFERENCE_SERVICE_URL", "http://inference-service:8000")

@app.post("/generate-image")
@app.post("/generate")
async def proxy_generate_image(request: Request):
    """
    Logika Meta AI: walidacja zapytania i oddelegowanie wykonania GPU do inference-service.
    """
    try:
        body = await request.json()
        import httpx
        async with httpx.AsyncClient(timeout=180.0) as client:
            resp = await client.post(f"{INFERENCE_SERVICE_URL}/generate-image", json=body)
            if resp.status_code != 200:
                raise HTTPException(status_code=resp.status_code, detail=resp.text)
            return resp.json()
    except HTTPException:
        raise
    except Exception as e:
        print(f"[META-AI -> INFERENCE-SERVICE GPU ERROR]: {e}")
        raise HTTPException(status_code=500, detail=f"Błąd komunikacji z GPU inference-service: {str(e)}")


if __name__ == "__main__":
    uvicorn.run(app, host="0.0.0.0", port=8000)