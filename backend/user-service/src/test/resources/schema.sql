CREATE TABLE IF NOT EXISTS posts (
    id VARCHAR(255) PRIMARY KEY,
    author_id VARCHAR(255),
    timestamp BIGINT
);
