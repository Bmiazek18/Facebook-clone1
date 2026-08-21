FROM python:3.11-slim

WORKDIR /app
COPY linkguard-service/requirements.txt ./requirements.txt
RUN pip install --no-cache-dir -r requirements.txt
COPY linkguard-service/ ./
COPY shared-proto /shared-proto

RUN python -m grpc_tools.protoc \
    -I /shared-proto/src/main/proto \
    --python_out=/app \
    --grpc_python_out=/app \
    /shared-proto/src/main/proto/feed.proto

EXPOSE 8086
CMD ["uvicorn", "main:app", "--host", "0.0.0.0", "--port", "8086"]
