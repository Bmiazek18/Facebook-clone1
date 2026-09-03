import os
from prometheus_client import Counter, Histogram, Gauge
from prometheus_fastapi_instrumentator import Instrumentator

# Metryki GPU i Inference
SD_GENERATION_DURATION = Histogram(
    "inference_sd_generation_duration_seconds",
    "Czas generowania obrazu przez Stable Diffusion",
    buckets=[1.0, 2.5, 5.0, 10.0, 20.0, 30.0, 60.0]
)

BERT_MODERATION_DURATION = Histogram(
    "inference_bert_moderation_duration_seconds",
    "Czas moderacji tekstu przez model BERT",
    buckets=[0.005, 0.01, 0.025, 0.05, 0.1, 0.25, 0.5]
)

GPU_VRAM_ALLOCATED_BYTES = Gauge(
    "inference_gpu_vram_allocated_bytes",
    "Aktualnie zaalokowana pamięć VRAM na GPU w bajtach"
)

def setup_inference_observability(app):
    Instrumentator().instrument(app).expose(app, endpoint="/metrics")
    try:
        from opentelemetry import trace
        from opentelemetry.sdk.trace import TracerProvider
        from opentelemetry.sdk.trace.export import BatchSpanProcessor
        from opentelemetry.exporter.otlp.proto.http.trace_exporter import OTLPSpanExporter
        from opentelemetry.sdk.resources import Resource
        from opentelemetry.instrumentation.fastapi import FastAPIInstrumentor

        otlp_endpoint = os.getenv("OTEL_EXPORTER_OTLP_ENDPOINT", "http://otel-collector:4318/v1/traces")
        resource = Resource.create({
            "service.name": "inference-service",
            "service.version": "1.0.0",
        })
        provider = TracerProvider(resource=resource)
        exporter = OTLPSpanExporter(endpoint=otlp_endpoint)
        provider.add_span_processor(BatchSpanProcessor(exporter))
        trace.set_tracer_provider(provider)
        FastAPIInstrumentor.instrument_app(app)
        print(f"[OTEL] Inference Service połączone z OpenTelemetry -> {otlp_endpoint}")
    except Exception as e:
        print(f"[OTEL] Ostrzeżenie dla inference-service: {e}")
