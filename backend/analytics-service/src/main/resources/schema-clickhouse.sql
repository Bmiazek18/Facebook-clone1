CREATE TABLE IF NOT EXISTS page_daily_metrics (
    id UUID,
    page_id UUID,
    metric_date Date,
    total_views Int64 DEFAULT 0,
    follower_views Int64 DEFAULT 0,
    non_follower_views Int64 DEFAULT 0,
    text_views Int64 DEFAULT 0,
    photo_views Int64 DEFAULT 0,
    video_views Int64 DEFAULT 0,
    reactions_count Int64 DEFAULT 0,
    comments_count Int64 DEFAULT 0,
    shares_count Int64 DEFAULT 0,
    net_followers Int64 DEFAULT 0,
    profile_visits Int64 DEFAULT 0,
    video_completions_count Int64 DEFAULT 0,
    video_loops_count Int64 DEFAULT 0,
    audio_unmutes_count Int64 DEFAULT 0,
    expand_text_count Int64 DEFAULT 0,
    lightbox_opens_count Int64 DEFAULT 0,
    link_clicks_count Int64 DEFAULT 0,
    copy_link_count Int64 DEFAULT 0,
    saves_count Int64 DEFAULT 0,
    updated_at DateTime
) ENGINE = MergeTree()
ORDER BY (page_id, metric_date);

CREATE TABLE IF NOT EXISTS user_item_interactions (
    id UUID,
    user_id String,
    target_id String,
    target_type Nullable(String),
    category Nullable(String),
    total_dwell_time_ms Int64 DEFAULT 0,
    interaction_count Int64 DEFAULT 0,
    video_completion_percent Float64 DEFAULT 0.0,
    video_loop_count Int32 DEFAULT 0,
    has_reaction UInt8 DEFAULT 0,
    has_comment UInt8 DEFAULT 0,
    has_share UInt8 DEFAULT 0,
    is_saved UInt8 DEFAULT 0,
    is_skipped UInt8 DEFAULT 0,
    is_hidden UInt8 DEFAULT 0,
    first_interacted_at DateTime,
    last_interacted_at DateTime
) ENGINE = MergeTree()
ORDER BY (user_id, target_id);

CREATE TABLE IF NOT EXISTS detected_bots (
    user_id String,
    reason String,
    event_count Int64,
    detected_at DateTime
) ENGINE = ReplacingMergeTree()
ORDER BY user_id;
