import os
import pandas as pd
import numpy as np
from autogluon.tabular import TabularPredictor
import clickhouse_connect

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

def train_autogluon_model():
    print("Connecting to ClickHouse offline Feature Store...")
    try:
        conn = get_db_connection()
        query = "SELECT total_interactions, avg_dwell_time_ms, total_fast_skips, total_hides FROM user_features_offline FINAL"
        df = conn.query_df(query)
        conn.close()
    except Exception as e:
        print(f"Could not read from database ({e}). Generating synthetic data for model cold-start...")
        df = pd.DataFrame()

    # Generate synthetic training data if database is empty or doesn't exist
    if df.empty or len(df) < 5:
        print("Feature Store has insufficient records. Creating synthetic dataset...")
        np.random.seed(42)
        n_samples = 200
        df = pd.DataFrame({
            "total_interactions": np.random.randint(1, 100, n_samples),
            "avg_dwell_time_ms": np.random.uniform(500, 15000, n_samples),
            "total_fast_skips": np.random.randint(0, 20, n_samples),
            "total_hides": np.random.randint(0, 5, n_samples)
        })

    # Create target labels for Multi-Task heavy ranking:
    # 1. Likelihood of Like (based on high interactions and low skips/hides)
    df["like_label"] = (df["total_interactions"] > 20) & (df["total_fast_skips"] < 5)
    df["like_label"] = df["like_label"].astype(int)

    # 2. Likelihood of Comment (based on high interactions)
    df["comment_label"] = (df["total_interactions"] > 40)
    df["comment_label"] = df["comment_label"].astype(int)

    # 3. Likelihood of long DwellTime (> 5 seconds)
    df["dwell_label"] = (df["avg_dwell_time_ms"] > 5000)
    df["dwell_label"] = df["dwell_label"].astype(int)

    # We will train a TabularPredictor for the 'like_label' to rank candidates
    print("Starting AutoGluon heavy ranking model training...")
    
    # Save model in directory 'autogluon_model'
    model_path = os.path.join(os.path.dirname(__file__), "autogluon_model")
    
    predictor = TabularPredictor(
        label="like_label",
        eval_metric="roc_auc",
        path=model_path
    ).fit(
        train_data=df,
        time_limit=30, # Limit to 30 seconds for quick local dev experience
        presets="light_quality"
    )

    print(f"AutoGluon model trained successfully and saved to: {model_path}")
    
    # Test prediction
    test_data = pd.DataFrame([{
        "total_interactions": 10,
        "avg_dwell_time_ms": 6000.0,
        "total_fast_skips": 2,
        "total_hides": 0
    }])
    pred = predictor.predict_proba(test_data)
    print(f"Test prediction (P(Like)): {pred.iloc[0].to_dict()}")

if __name__ == "__main__":
    train_autogluon_model()
