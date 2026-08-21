import os
import requests
import re
import asyncio
from celery import Celery
import grpc
import feed_pb2
import feed_pb2_grpc

# Konfiguracja brokera Redis pobierana ze zmiennych środowiskowych dockera
REDIS_URL = os.getenv("CELERY_BROKER_URL", "redis://redis:6379/0")

worker = Celery('linkguard_tasks', broker=REDIS_URL, backend=REDIS_URL)

# URL do dedykowanego serwisu inferencyjnego (Podejście A - GPU/CPU)
INFERENCE_SERVICE_URL = os.getenv("INFERENCE_SERVICE_URL", "http://inference-service:8000")

def extract_urls(text: str):
    # Regex to extract URLs (handles http and https links)
    return re.findall(r'(https?://[^\s]+)', text)

def pre_inspect_urls(content: str):
    urls = extract_urls(content)
    if not urls:
        return
    
    # Import Query dynamically to avoid circular import issues
    from main import Query
    query = Query()
    
    for url in urls:
        try:
            print(f"[LinkGuard Worker] Pre-inspecting URL: {url}")
            try:
                loop = asyncio.get_event_loop()
            except RuntimeError:
                loop = asyncio.new_event_loop()
                asyncio.set_event_loop(loop)
                
            if loop.is_running():
                asyncio.run_coroutine_threadsafe(query.inspect_link(url), loop)
            else:
                loop.run_until_complete(query.inspect_link(url))
        except Exception as e:
            print(f"[LinkGuard Worker] Pre-inspection failed for {url}: {e}")

@worker.task(name="tasks.moderate_post_task")
def moderate_post_task(post_id: str, content: str):
    """
    Zadanie asynchroniczne odpalane w tle przez Celery.
    Wysyła treść posta do zewnętrznego serwisu inferencji GPU (Inference Service)
    oraz pre-cache'uje wszelkie znalezione linki w Redisie.
    """
    # Pre-cache URLs in Redis
    pre_inspect_urls(content)
    
    try:
        # Odpytanie centralnego serwisu GPU o klasyfikację toksyczności
        res = requests.post(
            f"{INFERENCE_SERVICE_URL}/predict",
            json={"text": content},
            timeout=5
        )
        if res.status_code != 200:
            raise Exception(f"Inference Service returned status {res.status_code}: {res.text}")
        
        result = res.json()
        if "error" in result:
            raise Exception(result["error"])

        # Logika klasyfikacji ryzyka na podstawie scoringu BERT
        if result['label'] == 'toxic' and result['score'] > 0.55:
            status = "REJECTED"
        else:
            status = "ACTIVE"

        print(f"[LinkGuard Worker] Post {post_id} zweryfikowany przez Inference Service. Wynik: {status} ({result['score']:.4f})")

        # Wysyłamy status do FeedService przez gRPC
        try:
            grpc_address = os.getenv("FEED_SERVICE_GRPC_ADDRESS", "dns:///feedservice:9092").replace("dns:///", "")
            with grpc.insecure_channel(grpc_address) as channel:
                stub = feed_pb2_grpc.FeedGrpcServiceStub(channel)
                response = stub.UpdatePostStatus(
                    feed_pb2.UpdatePostStatusRequest(postId=post_id, status=status),
                    timeout=5
                )
                print(f"[LinkGuard Worker] Wysłano status do FeedService przez gRPC. Success={response.success}")
        except Exception as ex:
            print(f"[LinkGuard Worker] Nie udało się wysłać statusu do FeedService dla posta {post_id}: {ex}")

        return {"post_id": post_id, "status": status, "score": float(result['score'])}
    except Exception as e:
        print(f"[LinkGuard Worker] Błąd podczas moderacji BERT: {e}")
        return {"post_id": post_id, "status": "ERROR", "detail": str(e)}
