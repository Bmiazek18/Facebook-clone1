package com.facebook.analytics.dto;

import java.io.Serializable;

public class ReactionEvent implements Serializable {
    private String userId;
    private String postId;
    private String reactionType;
    private Long timestamp;

    public ReactionEvent() {}

    public ReactionEvent(String userId, String postId, String reactionType, Long timestamp) {
        this.userId = userId;
        this.postId = postId;
        this.reactionType = reactionType;
        this.timestamp = timestamp;
    }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getPostId() { return postId; }
    public void setPostId(String postId) { this.postId = postId; }

    public String getReactionType() { return reactionType; }
    public void setReactionType(String reactionType) { this.reactionType = reactionType; }

    public Long getTimestamp() { return timestamp; }
    public void setTimestamp(Long timestamp) { this.timestamp = timestamp; }

    public static ReactionEventBuilder builder() {
        return new ReactionEventBuilder();
    }

    public static class ReactionEventBuilder {
        private String userId;
        private String postId;
        private String reactionType;
        private Long timestamp;

        public ReactionEventBuilder userId(String userId) { this.userId = userId; return this; }
        public ReactionEventBuilder postId(String postId) { this.postId = postId; return this; }
        public ReactionEventBuilder reactionType(String reactionType) { this.reactionType = reactionType; return this; }
        public ReactionEventBuilder timestamp(Long timestamp) { this.timestamp = timestamp; return this; }

        public ReactionEvent build() {
            return new ReactionEvent(userId, postId, reactionType, timestamp);
        }
    }
}
