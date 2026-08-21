FROM python:3.11-slim

WORKDIR /app

# Install system dependencies for ClickHouse and Feast
RUN apt-get update && apt-get install -y \
    build-essential \
    curl \
    && rm -rf /var/lib/apt/lists/*

# Copy requirements and install
COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

# Copy Feast configuration and Python scripts
COPY feature_store.yaml .
COPY features.py .
COPY main.py .

EXPOSE 8096

# Set environment variables
ENV PYTHONUNBUFFERED=1

# Command to run uvicorn server
CMD ["uvicorn", "main:app", "--host", "0.0.0.0", "--port", "8096"]
