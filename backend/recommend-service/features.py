from datetime import timedelta
from feast import (
    Entity,
    FeatureView,
    Field,
    ValueType,
)
from feast.types import Int64, Float64, String
from feast.infra.offline_stores.postgres_source import PostgreSQLSource

# 1. Entity definition
user_entity = Entity(
    name="user_id",
    value_type=ValueType.STRING,
    join_keys=["user_id"],
    description="Unique user identifier for recommendation engine"
)

# 2. Offline Data Source definition (PostgreSQL table)
user_interactions_source = PostgreSQLSource(
    name="user_interactions_source",
    query="SELECT user_id, total_interactions, avg_dwell_time_ms, total_fast_skips, total_hides, top_targets, category_counts, recent_seen, event_timestamp, created_timestamp FROM user_features_offline",
    timestamp_field="event_timestamp",
    created_timestamp_column="created_timestamp"
)

# 3. Feature View definition
user_interaction_features = FeatureView(
    name="user_interaction_features",
    entities=[user_entity],
    ttl=timedelta(days=90),
    schema=[
        Field(name="total_interactions", dtype=Int64),
        Field(name="avg_dwell_time_ms", dtype=Float64),
        Field(name="total_fast_skips", dtype=Int64),
        Field(name="total_hides", dtype=Int64),
        Field(name="top_targets", dtype=String),
        Field(name="category_counts", dtype=String),
        Field(name="recent_seen", dtype=String)
    ],
    online=True,
    source=user_interactions_source,
    tags={"team": "recommender", "framework": "gluon"}
)
