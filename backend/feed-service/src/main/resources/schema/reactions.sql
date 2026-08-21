-- SQL Script for creating reactions table
CREATE TABLE IF NOT EXISTS reactions (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    post_id VARCHAR(255) NOT NULL,
    reaction_type VARCHAR(32) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_reactions_post FOREIGN KEY (post_id) REFERENCES posts(id) ON DELETE CASCADE,
    CONSTRAINT uq_reactions_user_post UNIQUE (user_id, post_id)
);

-- Index for optimization of retrieval per post
CREATE INDEX IF NOT EXISTS idx_reactions_post_id ON reactions(post_id);
