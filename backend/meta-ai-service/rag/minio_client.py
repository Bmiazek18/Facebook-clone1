import os
import io

MINIO_ENDPOINT = os.getenv("MINIO_ENDPOINT", os.getenv("MINIO_URL", "minio.apps.svc.cluster.local:9000"))
MINIO_HOST = MINIO_ENDPOINT.replace("http://", "").replace("https://", "").rstrip("/")

MINIO_ACCESS_KEY = os.getenv("MINIO_ACCESS_KEY", os.getenv("MINIO_ROOT_USER", "change-me"))
MINIO_SECRET_KEY = os.getenv("MINIO_SECRET_KEY", os.getenv("MINIO_ROOT_PASSWORD", "change-me"))
MINIO_SECURE = os.getenv("MINIO_SECURE", "false").lower() == "true"
BUCKET_NAME = os.getenv("MINIO_CHARTS_BUCKET", "generated-charts")

_minio_client = None

def get_minio_client():
    global _minio_client
    if _minio_client is None:
        try:
            from minio import Minio
            _minio_client = Minio(
                MINIO_HOST,
                access_key=MINIO_ACCESS_KEY,
                secret_key=MINIO_SECRET_KEY,
                secure=MINIO_SECURE
            )
            # Ensure bucket exists
            if not _minio_client.bucket_exists(BUCKET_NAME):
                _minio_client.make_bucket(BUCKET_NAME)
                policy = f'''{{
                    "Version": "2012-10-17",
                    "Statement": [
                        {{
                            "Effect": "Allow",
                            "Principal": {{ "AWS": ["*"] }},
                            "Action": ["s3:GetObject"],
                            "Resource": ["arn:aws:s3:::{BUCKET_NAME}/*"]
                        }}
                    ]
                }}'''
                try:
                    _minio_client.set_bucket_policy(BUCKET_NAME, policy)
                except Exception as pe:
                    print(f"[MinIO] Warning setting bucket policy: {pe}")
            print(f"[MinIO] Successfully connected to {MINIO_HOST}, bucket '{BUCKET_NAME}' ready.")
        except Exception as e:
            print(f"[MinIO] Initialization warning: {e}")
            try:
                from minio import Minio
                _minio_client = Minio(
                    MINIO_HOST,
                    access_key="minioadmin",
                    secret_key="minioadmin",
                    secure=MINIO_SECURE
                )
                if not _minio_client.bucket_exists(BUCKET_NAME):
                    _minio_client.make_bucket(BUCKET_NAME)
            except Exception as e2:
                print(f"[MinIO] Secondary init fallback error: {e2}")
    return _minio_client


def upload_chart_to_minio(filename: str, image_bytes: bytes, content_type: str = "image/png") -> str:
    """Uploads chart image bytes to MinIO and returns filename."""
    client = get_minio_client()
    if client is None:
        raise RuntimeError("MinIO client is not available")
    
    stream = io.BytesIO(image_bytes)
    client.put_object(
        bucket_name=BUCKET_NAME,
        object_name=filename,
        data=stream,
        length=len(image_bytes),
        content_type=content_type
    )
    print(f"[MinIO] Chart '{filename}' ({len(image_bytes)} bytes) stored in bucket '{BUCKET_NAME}'.")
    return filename


def get_chart_from_minio(filename: str):
    """Retrieves chart image stream from MinIO."""
    client = get_minio_client()
    if client is None:
        raise RuntimeError("MinIO client is not available")
    return client.get_object(BUCKET_NAME, filename)
