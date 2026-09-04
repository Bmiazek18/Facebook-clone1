import os
import json
import asyncio
import logging
from datetime import datetime
from typing import List, Dict, Optional
from fastapi import FastAPI, HTTPException
from pydantic import BaseModel
from feast import FeatureStore
import clickhouse_connect
from aiokafka import AIOKafkaConsumer
from qdrant_client import QdrantClient
from qdrant_client.models import Distance, VectorParams, PointStruct
from transformers import pipeline
import numpy as np
import pandas as pd
from autogluon.tabular import TabularPredictor
from prometheus_fastapi_instrumentator import Instrumentator
from prometheus_client import Histogram

# Configure logging
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger("recommend-service")

app = FastAPI(title="Recommend Service (Feast Feature Store, Qdrant, AutoGluon & Prometheus)", version="1.0.0")
Instrumentator().instrument(app).expose(app, endpoint="/metrics")

# Prometheus custom metrics for MLOps tracking
QDRANT_LATENCY = Histogram("recommend_qdrant_search_latency_seconds", "Latency of Qdrant vector search in seconds")
FEAST_LATENCY = Histogram("recommend_feast_retrieval_latency_seconds", "Latency of Feast online features retrieval in seconds")
MODEL_LATENCY = Histogram("recommend_model_inference_latency_seconds", "Latency of AutoGluon heavy ranking model inference in seconds")

# Database connection helper
def get_db_connection():
    host = os.getenv("CLICKHOUSE_HOST", "localhost")
    port = int(os.getenv("CLICKHOUSE_PORT", "8123"))
    database = os.getenv("CLICKHOUSE_DB", "analytics_db")
    username = os.getenv("CLICKHOUSE_USER", "default")
    password = os.getenv("CLICKHOUSE_PASSWORD", "default_password")
    return clickhouse_connect.get_client(
        host=host,
        port=port,
        database=database,
        username=username,
        password=password
    )

detected_bots_cache = set()
last_cache_refresh = 0.0

def check_tarpit(user_id: str):
    global last_cache_refresh
    if not user_id or user_id in ("0", "1"):
        return
    try:
        import time
        now = time.time()
        # Refresh cache every 15 seconds
        if now - last_cache_refresh > 15:
            client = get_db_connection()
            result = client.query("SELECT user_id FROM detected_bots FINAL")
            detected_bots_cache.clear()
            for row in result.result_rows:
                detected_bots_cache.add(row[0])
            client.close()
            last_cache_refresh = now

        if user_id in detected_bots_cache:
            logger.info(f"Tarpitting bot user {user_id} - introducing 5 seconds delay...")
            time.sleep(5)
    except Exception as e:
        logger.error(f"Error in check_tarpit: {e}")

# Initialize database schema
def init_db():
    client = get_db_connection()
    client.command("""
        CREATE TABLE IF NOT EXISTS user_features_offline (
            user_id String,
            total_interactions Int64 DEFAULT 0,
            avg_dwell_time_ms Float64 DEFAULT 0.0,
            total_fast_skips Int64 DEFAULT 0,
            total_hides Int64 DEFAULT 0,
            top_targets String,
            category_counts String,
            recent_seen String,
            event_timestamp DateTime,
            created_timestamp DateTime
        ) ENGINE = ReplacingMergeTree()
        ORDER BY user_id;
    """)
    client.close()
    logger.info("Database initialized successfully.")

# Global state
feast_store = None
qdrant_client = None
embedder = None
autogluon_predictor = None

# Initialize ML model for vector embeddings
def init_ml():
    global embedder
    try:
        model_name = "sentence-transformers/all-MiniLM-L6-v2"
        logger.info(f"Loading transformer model: {model_name}...")
        embedder = pipeline("feature-extraction", model=model_name)
        logger.info("Transformer model loaded successfully.")
    except Exception as e:
        logger.error(f"Failed to load transformer model: {e}")

# Helper to compute sentence embeddings
def get_text_embedding(text: str) -> List[float]:
    if embedder is None:
        return [0.0] * 384
    if not text or not text.strip():
        return [0.0] * 384
    
    try:
        features = embedder(text)
        np_feats = np.mean(features[0], axis=0)
        return np_feats.tolist()
    except Exception as e:
        logger.error(f"Failed to generate embedding: {e}")
        return [0.0] * 384

# Initialize Qdrant Collection
def init_qdrant():
    global qdrant_client
    try:
        qdrant_host = os.getenv("QDRANT_HOST", "qdrant")
        qdrant_port = int(os.getenv("QDRANT_PORT", "6333"))
        logger.info(f"Connecting to Qdrant at {qdrant_host}:{qdrant_port}...")
        qdrant_client = QdrantClient(host=qdrant_host, port=qdrant_port)
        
        collections_resp = qdrant_client.get_collections()
        collection_names = [col.name for col in collections_resp.collections]
        
        if "posts" not in collection_names:
            logger.info("Creating Qdrant collection: posts")
            qdrant_client.recreate_collection(
                collection_name="posts",
                vectors_config=VectorParams(size=384, distance=Distance.COSINE)
            )
        logger.info("Qdrant collection setup complete.")
    except Exception as e:
        logger.error(f"Qdrant connection/initialization failed: {e}")

# Load trained AutoGluon model if exists
def load_autogluon_model():
    global autogluon_predictor
    try:
        model_path = os.path.join(os.path.dirname(__file__), "autogluon_model")
        if os.path.exists(model_path):
            autogluon_predictor = TabularPredictor.load(model_path)
            logger.info("AutoGluon heavy ranking predictor loaded successfully.")
        else:
            logger.warn("AutoGluon model directory not found. Running Heavy Ranking in heuristic fallback mode.")
    except Exception as e:
        logger.error(f"Failed to load AutoGluon predictor: {e}")

@app.on_event("startup")
async def startup_event():
    logger.info("Prometheus FastAPI Instrumentator active under /metrics")

    # 2. Initialize ML Pipeline (Option A)
    init_ml()
    load_autogluon_model()

    # 3. Initialize Qdrant Client & Collections (Option C)
    init_qdrant()

    # 4. Initialize DB tables
    try:
        init_db()
    except Exception as e:
        logger.error(f"Failed to initialize database: {e}")

    # 5. Apply Feast Feature Store (runs 'feast apply' programmatically)
    global feast_store
    try:
        os.system("feast apply")
        feast_store = FeatureStore(repo_path=".")
        logger.info("Feast Feature Store initialized and applied successfully.")
    except Exception as e:
        logger.error(f"Failed to initialize Feast: {e}")

    # 6. Start background Kafka Consumer to update Feature Store in real-time
    asyncio.create_task(consume_telemetry_stream())

    # 7. Start periodic AutoGluon model training scheduler
    asyncio.create_task(periodic_model_retrain_scheduler())

# Background task to retrain the model periodically (every 12 hours)
async def periodic_model_retrain_scheduler():
    logger.info("AutoGluon model retrain scheduler started.")
    # Short initial sleep before checking/triggering model training to let systems initialize
    await asyncio.sleep(30)
    
    while True:
        model_path = os.path.join(os.path.dirname(__file__), "autogluon_model")
        if not os.path.exists(model_path):
            logger.info("No AutoGluon model found on startup. Triggering initial training run...")
            try:
                from train import train_autogluon_model
                await asyncio.to_thread(train_autogluon_model)
                load_autogluon_model()
            except Exception as e:
                logger.error(f"Initial model training failed: {e}")
        
        # Sleep for 12 hours
        await asyncio.sleep(12 * 3600)
        
        logger.info("Triggering scheduled AutoGluon model retrain...")
        try:
            from train import train_autogluon_model
            await asyncio.to_thread(train_autogluon_model)
            load_autogluon_model()
            logger.info("AutoGluon model successfully retrained and reloaded.")
        except Exception as e:
            logger.error(f"Scheduled model training failed: {e}")

# Models
class TelemetryEvent(BaseModel):
    userId: str
    eventType: str
    postId: Optional[str] = None
    pageId: Optional[str] = None
    contentType: Optional[str] = None
    dwellTimeMs: Optional[float] = None
    isFollower: Optional[bool] = None
    completionPercent: Optional[float] = None
    loopCount: Optional[int] = None
    source: Optional[str] = None

class PostIngestRequest(BaseModel):
    id: str
    authorId: str
    content: str
    category: str
    timestamp: int

class UserFeatureResponse(BaseModel):
    userId: str
    totalInteractions: int
    avgDwellTimeMs: float
    totalFastSkips: int
    totalHides: int
    topTargets: List[str]
    categoryCounts: Dict[str, int]
    recentSeen: List[str]

class RankRequest(BaseModel):
    userId: str
    candidates: List[str]

class RankedItem(BaseModel):
    postId: str
    score: float

class RankResponse(BaseModel):
    rankedPosts: List[RankedItem]

@app.get("/health")
def health():
    return {
        "status": "UP",
        "feature_store": "Feast (Postgres + Redis)",
        "vector_search": "Qdrant",
        "heavy_ranking_model": "AutoGluon" if autogluon_predictor is not None else "Heuristic-Fallback",
        "monitoring": "Prometheus active under /metrics"
    }

@app.post("/api/recommend/posts")
def ingest_post_vector(post: PostIngestRequest):
    if qdrant_client is None:
        raise HTTPException(status_code=500, detail="Qdrant client not initialized")

    try:
        vector = get_text_embedding(post.content)
        qdrant_client.upsert(
            collection_name="posts",
            points=[
                PointStruct(
                    id=post.id,
                    vector=vector,
                    payload={
                        "authorId": post.authorId,
                        "category": post.category,
                        "timestamp": post.timestamp
                    }
                )
            ]
        )
        logger.info(f"Stored vector embedding in Qdrant for post: {post.id}")
        return {"status": "success", "postId": post.id}
    except Exception as e:
        logger.error(f"Failed to ingest post vector: {e}")
        raise HTTPException(status_code=500, detail=str(e))

@app.get("/api/recommend/candidates/{user_id}")
def get_recommendation_candidates(user_id: str, limit: int = 100) -> List[str]:
    """
    Stage 1: Candidate Retrieval (Qdrant Vector Similarity Search).
    Option C: Dynamic User Profile Embedding with Negative Feedback (Subtraction) & Time-Weighting.
    """
    check_tarpit(user_id)
    if qdrant_client is None or feast_store is None:
        return []

    try:
        # 1. Fetch user favorite interactions and ignore logs from Feast
        with FEAST_LATENCY.time():
            feature_refs = [
                "user_interaction_features:top_targets",
                "user_interaction_features:recent_seen"
            ]
            response = feast_store.get_online_features(
                features=feature_refs,
                entity_rows=[{"user_id": user_id}]
            ).to_dict()

        top_targets_raw = response.get("top_targets", ["[]"])[0]
        recent_seen_raw = response.get("recent_seen", ["[]"])[0]

        top_targets = json.loads(top_targets_raw) if top_targets_raw else []
        recent_seen = json.loads(recent_seen_raw) if recent_seen_raw else []

        # Accumulate positive embeddings with exponential time decay (newer = higher weight)
        user_vector = np.zeros(384)
        positive_weight_sum = 0.0
        
        if top_targets:
            # Reversed list so most recent targets are processed with idx 0
            for idx, post_id in enumerate(reversed(top_targets)):
                try:
                    points = qdrant_client.retrieve(collection_name="posts", ids=[post_id], with_vectors=True)
                    if points and points[0].vector:
                        weight = 0.9 ** idx  # Exponential time decay weight
                        user_vector += np.array(points[0].vector) * weight
                        positive_weight_sum += weight
                except Exception:
                    continue

        if positive_weight_sum > 0:
            user_vector /= positive_weight_sum

        # Accumulate negative embeddings (posts seen but NOT interacted with)
        negative_targets = [pid for pid in recent_seen if pid not in top_targets]
        negative_vector = np.zeros(384)
        negative_weight_sum = 0.0

        if negative_targets:
            for idx, post_id in enumerate(reversed(negative_targets)):
                try:
                    points = qdrant_client.retrieve(collection_name="posts", ids=[post_id], with_vectors=True)
                    if points and points[0].vector:
                        weight = 0.95 ** idx
                        negative_vector += np.array(points[0].vector) * weight
                        negative_weight_sum += weight
                except Exception:
                    continue

        if negative_weight_sum > 0:
            negative_vector /= negative_weight_sum
            # Subtract negative feedback vector with 0.3 weight to push recommendations away from ignored topics
            user_vector -= 0.3 * negative_vector

        # Convert final user profile back to python list
        user_vector_list = user_vector.tolist()

        # 2. Query Qdrant for similar candidates
        with QDRANT_LATENCY.time():
            search_results = qdrant_client.search(
                collection_name="posts",
                query_vector=user_vector_list,
                limit=limit,
                with_payload=False
            )

        candidate_ids = [str(res.id) for res in search_results]
        logger.info(f"Dynamic Profile Qdrant Search returned {len(candidate_ids)} candidates for user: {user_id}")
        return candidate_ids
    except Exception as e:
        logger.error(f"Error generating recommendation candidates: {e}")
        return []

@app.post("/api/recommend/rank", response_model=RankResponse)
def rank_candidates_heavy(request: RankRequest):
    """
    Stage 2: Heavy Ranking.
    Option A: Uses Feast Feature Store to fetch user profile, then predicts engagement probability
    using the trained AutoGluon model (or falls back to Multi-Objective heuristic if model not loaded).
    """
    user_id = request.userId
    check_tarpit(user_id)
    if feast_store is None:
        raise HTTPException(status_code=500, detail="Feature store not initialized")

    try:
        user_id = request.userId
        post_ids = request.candidates

        if not post_ids:
            return RankResponse(rankedPosts=[])

        # 1. Fetch user features from Feast
        with FEAST_LATENCY.time():
            feature_refs = [
                "user_interaction_features:total_interactions",
                "user_interaction_features:avg_dwell_time_ms",
                "user_interaction_features:total_fast_skips",
                "user_interaction_features:total_hides",
                "user_interaction_features:category_counts",
                "user_interaction_features:recent_seen",
                "user_interaction_features:top_targets"
            ]
            response = feast_store.get_online_features(
                features=feature_refs,
                entity_rows=[{"user_id": user_id}]
            ).to_dict()

        total_interactions = response.get("total_interactions", [0])[0] or 0
        avg_dwell = response.get("avg_dwell_time_ms", [0.0])[0] or 0.0
        fast_skips = response.get("total_fast_skips", [0])[0] or 0
        hides = response.get("total_hides", [0])[0] or 0
        recent_seen_raw = response.get("recent_seen", ["[]"])[0]
        top_targets_raw = response.get("top_targets", ["[]"])[0]

        try:
            recent_seen = json.loads(recent_seen_raw) if recent_seen_raw else []
        except:
            recent_seen = []
            
        try:
            top_targets = json.loads(top_targets_raw) if top_targets_raw else []
        except:
            top_targets = []

        # 2. Run Heavy Ranking with AutoGluon model or fallback multi-objective formula
        scored_posts = []
        
        with MODEL_LATENCY.time():
            if autogluon_predictor is not None:
                # Prepare DataFrame for batch inference
                features_df = pd.DataFrame([{
                    "total_interactions": int(total_interactions),
                    "avg_dwell_time_ms": float(avg_dwell),
                    "total_fast_skips": int(fast_skips),
                    "total_hides": int(hides)
                }] * len(post_ids))
                
                # Predict probabilities (predict_proba returns classes 0 and 1)
                predictions = autogluon_predictor.predict_proba(features_df)
                # Use class 1 (Like likelihood) as base score
                scores = predictions[1].tolist()
                
                for post_id, base_score in zip(post_ids, scores):
                    score = base_score
                    # Apply Repetition Penalty
                    if post_id in recent_seen and post_id not in top_targets:
                        score *= 0.3
                    scored_posts.append((post_id, score))
            else:
                # Fallback to Multi-Objective heuristic scoring
                for post_id in post_ids:
                    # Baseline score from categories, simplified
                    p_like = 0.1
                    p_comment = 0.05
                    p_share = 0.02
                    p_dwell = 0.2
                    p_hide = 0.01
                    
                    score = (1.0 * p_like) + (2.0 * p_comment) + (3.0 * p_share) + (1.5 * p_dwell) - (5.0 * p_hide)
                    
                    # Apply Repetition Penalty
                    if post_id in recent_seen and post_id not in top_targets:
                        score *= 0.3
                        
                    scored_posts.append((post_id, score))

        # Sort descending by score
        scored_posts.sort(key=lambda x: x[1], reverse=True)

        ranked_items = [RankedItem(postId=post_id, score=score) for post_id, score in scored_posts]
        return RankResponse(rankedPosts=ranked_items)
    except Exception as e:
        logger.error(f"Error in heavy ranking endpoint: {e}")
        raise HTTPException(status_code=500, detail=str(e))

@app.get("/api/recommend/features/user/{user_id}", response_model=UserFeatureResponse)
def get_user_features(user_id: str):
    if feast_store is None:
        raise HTTPException(status_code=500, detail="Feature store not initialized")

    try:
        feature_refs = [
            "user_interaction_features:total_interactions",
            "user_interaction_features:avg_dwell_time_ms",
            "user_interaction_features:total_fast_skips",
            "user_interaction_features:total_hides",
            "user_interaction_features:top_targets",
            "user_interaction_features:category_counts",
            "user_interaction_features:recent_seen"
        ]
        
        response = feast_store.get_online_features(
            features=feature_refs,
            entity_rows=[{"user_id": user_id}]
        ).to_dict()

        total_interactions = response.get("total_interactions", [None])[0]

        if total_interactions is None:
            return UserFeatureResponse(
                userId=user_id,
                totalInteractions=0,
                avgDwellTimeMs=0.0,
                totalFastSkips=0,
                totalHides=0,
                topTargets=[],
                categoryCounts={},
                recentSeen=[]
            )

        top_targets_raw = response.get("top_targets", ["[]"])[0]
        category_counts_raw = response.get("category_counts", ["{}"])[0]
        recent_seen_raw = response.get("recent_seen", ["[]"])[0]

        try:
            top_targets = json.loads(top_targets_raw) if top_targets_raw else []
        except:
            top_targets = []

        try:
            category_counts = json.loads(category_counts_raw) if category_counts_raw else {}
        except:
            category_counts = {}

        try:
            recent_seen = json.loads(recent_seen_raw) if recent_seen_raw else []
        except:
            recent_seen = []

        return UserFeatureResponse(
            userId=user_id,
            totalInteractions=total_interactions,
            avgDwellTimeMs=response.get("avg_dwell_time_ms", [0.0])[0] or 0.0,
            totalFastSkips=response.get("total_fast_skips", [0])[0] or 0,
            totalHides=response.get("total_hides", [0])[0] or 0,
            topTargets=top_targets,
            categoryCounts=category_counts,
            recentSeen=recent_seen
        )
    except Exception as e:
        logger.error(f"Error retrieving online features: {e}")
        raise HTTPException(status_code=500, detail=str(e))

@app.post("/api/recommend/telemetry")
def ingest_telemetry_direct(event: TelemetryEvent):
    try:
        update_user_features(event)
        return {"status": "success", "message": "Features updated in Feature Store"}
    except Exception as e:
        logger.error(f"Failed to ingest telemetry: {e}")
        raise HTTPException(status_code=500, detail=str(e))

@app.get("/api/recommend/gluon/export")
def export_historical_features():
    try:
        client = get_db_connection()
        result = client.query("SELECT * FROM user_features_offline FINAL ORDER BY event_timestamp DESC")
        rows = [dict(zip(result.column_names, row)) for row in result.result_rows]
        client.close()

        gluon_dataset = []
        for row in rows:
            try:
                cats = json.loads(row["category_counts"]) if row["category_counts"] else {}
            except:
                cats = {}
            gluon_dataset.append({
                "user_id": row["user_id"],
                "target": float(row["total_interactions"]),
                "start": row["event_timestamp"].isoformat() if row["event_timestamp"] else datetime.utcnow().isoformat(),
                "dynamic_feat": [
                    float(row["avg_dwell_time_ms"] or 0.0),
                    float(row["total_fast_skips"] or 0.0),
                    float(row["total_hides"] or 0.0)
                ],
                "cat": list(cats.keys())
            })
        return {"dataset": gluon_dataset, "format": "GluonTS-TimeSeries"}
    except Exception as e:
        logger.error(f"Error exporting features for Gluon: {e}")
        raise HTTPException(status_code=500, detail=str(e))

def update_user_features(event: TelemetryEvent):
    if not event.userId or event.userId == "0" or event.userId == "1":
        return

    client = get_db_connection()

    result = client.query("SELECT * FROM user_features_offline FINAL WHERE user_id = {user_id:String}", parameters={"user_id": event.userId})
    row = dict(zip(result.column_names, result.result_rows[0])) if result.result_rows else None

    total_interactions = 1
    avg_dwell = event.dwellTimeMs if event.dwellTimeMs else 0.0
    fast_skips = 1 if event.eventType == "SKIP" or (event.dwellTimeMs and event.dwellTimeMs < 2000 and event.eventType == "VIDEO_PROGRESS") else 0
    hides = 1 if event.eventType == "HIDE" else 0

    top_targets = []
    category_counts = {}
    recent_seen = []

    if event.pageId:
        top_targets.append(event.pageId)
    if event.postId:
        top_targets.append(event.postId)
        recent_seen.append(event.postId)

    if event.contentType:
        category_counts[event.contentType.lower()] = 1

    if row:
        prev_interactions = row["total_interactions"]
        prev_dwell = row["avg_dwell_time_ms"] or 0.0
        total_interactions = prev_interactions + 1
        avg_dwell = ((prev_dwell * prev_interactions) + (event.dwellTimeMs or 0.0)) / total_interactions
        fast_skips = row["total_fast_skips"] + fast_skips
        hides = row["total_hides"] + hides

        try:
            prev_targets = json.loads(row["top_targets"]) if row["top_targets"] else []
        except:
            prev_targets = []
        
        for t in top_targets:
            if t not in prev_targets:
                prev_targets.append(t)
        top_targets = prev_targets[-10:]

        try:
            prev_categories = json.loads(row["category_counts"]) if row["category_counts"] else {}
        except:
            prev_categories = {}
        
        for k, v in category_counts.items():
            prev_categories[k] = prev_categories.get(k, 0) + v
        category_counts = prev_categories

        try:
            prev_seen = json.loads(row["recent_seen"]) if row["recent_seen"] else []
        except:
            prev_seen = []
        
        for s in recent_seen:
            if s not in prev_seen:
                prev_seen.append(s)
        recent_seen = prev_seen[-50:]

    created_ts = row["created_timestamp"] if row else datetime.utcnow()
    client.insert("user_features_offline", [[
        event.userId,
        total_interactions,
        avg_dwell,
        fast_skips,
        hides,
        json.dumps(top_targets),
        json.dumps(category_counts),
        json.dumps(recent_seen),
        datetime.utcnow(),
        created_ts
    ]], column_names=[
        "user_id", "total_interactions", "avg_dwell_time_ms", "total_fast_skips", "total_hides", 
        "top_targets", "category_counts", "recent_seen", "event_timestamp", "created_timestamp"
    ])
    client.close()

    if feast_store is not None:
        try:
            feast_store.write_to_online_store(
                feature_view_name="user_interaction_features",
                entity_rows=[{
                    "user_id": event.userId,
                    "total_interactions": int(total_interactions),
                    "avg_dwell_time_ms": float(avg_dwell),
                    "total_fast_skips": int(fast_skips),
                    "total_hides": int(hides),
                    "top_targets": json.dumps(top_targets),
                    "category_counts": json.dumps(category_counts),
                    "recent_seen": json.dumps(recent_seen),
                    "event_timestamp": datetime.utcnow()
                }]
            )
            logger.info(f"Pushed real-time features to Feast Redis for user: {event.userId}")
        except Exception as e:
            logger.error(f"Failed to write to Feast online store: {e}")

async def consume_telemetry_stream():
    kafka_servers = os.getenv("SPRING_KAFKA_BOOTSTRAP_SERVERS", "localhost:9092")
    consumer = AIOKafkaConsumer(
        "telemetry-events",
        bootstrap_servers=kafka_servers,
        group_id="recommend-service-feast-group",
        auto_offset_reset="earliest",
        enable_auto_commit=True
    )
    
    logger.info("Kafka consumer background worker starting...")
    while True:
        try:
            await consumer.start()
            logger.info("Kafka consumer connected successfully.")
            break
        except Exception as e:
            logger.error(f"Kafka connection failed, retrying in 5s...: {e}")
            await asyncio.sleep(5)

    try:
        async for msg in consumer:
            try:
                payload = json.loads(msg.value.decode("utf-8"))
                logger.info(f"Received telemetry event from Kafka: {payload}")
                
                event = TelemetryEvent(
                    userId=str(payload.get("userId")),
                    eventType=payload.get("eventType", ""),
                    postId=payload.get("postId"),
                    pageId=payload.get("pageId"),
                    contentType=payload.get("contentType"),
                    dwellTimeMs=float(payload.get("dwellTimeMs")) if payload.get("dwellTimeMs") is not None else None,
                    isFollower=payload.get("isFollower"),
                    completionPercent=float(payload.get("completionPercent")) if payload.get("completionPercent") is not None else None,
                    loopCount=payload.get("loopCount"),
                    source=payload.get("source")
                )
                update_user_features(event)
            except Exception as e:
                logger.error(f"Error parsing/saving telemetry stream message: {e}")
    finally:
        await consumer.stop()
