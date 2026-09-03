import os
import time
from typing import Optional

# 1. PROMETHEUS METRICS
from prometheus_client import Counter, Histogram, Gauge
from prometheus_fastapi_instrumentator import Instrumentator

# Metryki LLM / RAG
LLM_REQUESTS_TOTAL = Counter(
    "meta_ai_requests_total",
    "Liczba zapytań do Meta AI",
    ["model", "status"]
)

LLM_TTFT_SECONDS = Histogram(
    "meta_ai_time_to_first_token_seconds",
    "Czas do wygenerowania pierwszego tokena (TTFT)",
    ["model"],
    buckets=[0.05, 0.1, 0.25, 0.5, 1.0, 2.0, 5.0, 10.0]
)

LLM_GENERATION_DURATION = Histogram(
    "meta_ai_generation_duration_seconds",
    "Całkowity czas generowania odpowiedzi przez LLM",
    ["model"],
    buckets=[0.5, 1.0, 2.0, 5.0, 10.0, 20.0, 30.0, 60.0]
)

TOOL_EXECUTIONS_TOTAL = Counter(
    "meta_ai_tool_executions_total",
    "Liczba wywołań narzędzi przez agenta",
    ["tool_name", "status"]
)

# 2. OPENTELEMETRY TRACING (GenAI Semantic Conventions)
tracer = None
try:
    from opentelemetry import trace
    from opentelemetry.sdk.trace import TracerProvider
    from opentelemetry.sdk.trace.export import BatchSpanProcessor
    from opentelemetry.exporter.otlp.proto.http.trace_exporter import OTLPSpanExporter
    from opentelemetry.sdk.resources import Resource
    from opentelemetry.instrumentation.fastapi import FastAPIInstrumentor

    otlp_endpoint = os.getenv("OTEL_EXPORTER_OTLP_ENDPOINT", "http://otel-collector:4318/v1/traces")
    resource = Resource.create({
        "service.name": "meta-ai-service",
        "service.version": "1.0.0",
        "gen_ai.system": "langchain_langgraph",
    })
    provider = TracerProvider(resource=resource)
    exporter = OTLPSpanExporter(endpoint=otlp_endpoint)
    provider.add_span_processor(BatchSpanProcessor(exporter))
    trace.set_tracer_provider(provider)
    tracer = trace.get_tracer("meta-ai.rag", "1.0.0")
    print(f"[OTEL] Zainicjalizowano OpenTelemetry -> {otlp_endpoint}")
except Exception as e:
    print(f"[OTEL] Ostrzeżenie: Nie udało się zainicjalizować OpenTelemetry: {e}")

# 3. LANGFUSE INTEGRATION
def get_langfuse_callback(session_id: Optional[str] = None, user_id: Optional[str] = None):
    langfuse_public = os.getenv("LANGFUSE_PUBLIC_KEY", "")
    langfuse_secret = os.getenv("LANGFUSE_SECRET_KEY", "")
    langfuse_host = os.getenv("LANGFUSE_HOST", "https://cloud.langfuse.com")

    if langfuse_public and langfuse_secret:
        try:
            from langfuse.callback import CallbackHandler
            handler = CallbackHandler(
                public_key=langfuse_public,
                secret_key=langfuse_secret,
                host=langfuse_host,
                session_id=session_id,
                user_id=user_id,
                release="meta-ai-service@1.0.0"
            )
            return handler
        except Exception as err:
            print(f"[LANGFUSE] Błąd inicjalizacji CallbackHandler: {err}")
    return None

def setup_observability(app):
    # Instrumentacja FastAPI dla Prometheus (/metrics)
    Instrumentator().instrument(app).expose(app, endpoint="/metrics")

    # Instrumentacja FastAPI dla OpenTelemetry
    try:
        from opentelemetry.instrumentation.fastapi import FastAPIInstrumentor
        FastAPIInstrumentor.instrument_app(app)
    except Exception as e:
        print(f"[OTEL] Nie udało się zainstrumentować aplikacji FastAPI: {e}")
