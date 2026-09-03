import os
import sentry_sdk
from sentry_sdk.integrations.fastapi import FastApiIntegration
from sentry_sdk.integrations.logging import LoggingIntegration

def init_sentry(service_name: str = "meta-ai"):
    sentry_dsn = os.getenv("SENTRY_DSN", "")
    if sentry_dsn:
        sentry_sdk.init(
            dsn=sentry_dsn,
            integrations=[
                FastApiIntegration(),
                LoggingIntegration()
            ],
            traces_sample_rate=float(os.getenv("SENTRY_TRACES_SAMPLE_RATE", "1.0")),
            profiles_sample_rate=float(os.getenv("SENTRY_PROFILES_SAMPLE_RATE", "1.0")),
            environment=os.getenv("ENVIRONMENT", "development"),
            release=os.getenv("RELEASE", f"meta-ai-{service_name}@1.0.0"),
            send_default_pii=True
        )
        print(f"[SENTRY] Pomyślnie połączono z Sentry dla modułu: {service_name}")
    else:
        print(f"[SENTRY] SENTRY_DSN nie jest ustawiony. Sentry nieaktywne dla {service_name}.")
