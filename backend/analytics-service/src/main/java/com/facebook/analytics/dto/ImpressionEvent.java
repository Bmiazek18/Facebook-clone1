package com.facebook.analytics.dto;

public class ImpressionEvent {
    private String postId;
    private String pageId;
    private String viewerId;
    private Boolean isFollower;
    private String contentType; // text, photo, video, reel, story
    private Long dwellTimeMs;
    private String source; // feed, profile, search
    private Long timestamp;

    public ImpressionEvent() {}

    public ImpressionEvent(String postId, String pageId, String viewerId, Boolean isFollower,
                           String contentType, Long dwellTimeMs, String source, Long timestamp) {
        this.postId = postId;
        this.pageId = pageId;
        this.viewerId = viewerId;
        this.isFollower = isFollower;
        this.contentType = contentType;
        this.dwellTimeMs = dwellTimeMs;
        this.source = source;
        this.timestamp = timestamp;
    }

    public String getPostId() { return postId; }
    public void setPostId(String postId) { this.postId = postId; }

    public String getPageId() { return pageId; }
    public void setPageId(String pageId) { this.pageId = pageId; }

    public String getViewerId() { return viewerId; }
    public void setViewerId(String viewerId) { this.viewerId = viewerId; }

    public Boolean getIsFollower() { return isFollower; }
    public boolean isFollower() { return Boolean.TRUE.equals(isFollower); }
    public void setIsFollower(Boolean isFollower) { this.isFollower = isFollower; }

    public String getContentType() { return contentType; }
    public void setContentType(String contentType) { this.contentType = contentType; }

    public Long getDwellTimeMs() { return dwellTimeMs; }
    public void setDwellTimeMs(Long dwellTimeMs) { this.dwellTimeMs = dwellTimeMs; }

    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }

    public Long getTimestamp() { return timestamp; }
    public void setTimestamp(Long timestamp) { this.timestamp = timestamp; }

    public static ImpressionEventBuilder builder() {
        return new ImpressionEventBuilder();
    }

    public static class ImpressionEventBuilder {
        private String postId;
        private String pageId;
        private String viewerId;
        private Boolean isFollower;
        private String contentType;
        private Long dwellTimeMs;
        private String source;
        private Long timestamp;

        public ImpressionEventBuilder postId(String postId) { this.postId = postId; return this; }
        public ImpressionEventBuilder pageId(String pageId) { this.pageId = pageId; return this; }
        public ImpressionEventBuilder viewerId(String viewerId) { this.viewerId = viewerId; return this; }
        public ImpressionEventBuilder isFollower(Boolean isFollower) { this.isFollower = isFollower; return this; }
        public ImpressionEventBuilder contentType(String contentType) { this.contentType = contentType; return this; }
        public ImpressionEventBuilder dwellTimeMs(Long dwellTimeMs) { this.dwellTimeMs = dwellTimeMs; return this; }
        public ImpressionEventBuilder source(String source) { this.source = source; return this; }
        public ImpressionEventBuilder timestamp(Long timestamp) { this.timestamp = timestamp; return this; }

        public ImpressionEvent build() {
            return new ImpressionEvent(postId, pageId, viewerId, isFollower, contentType, dwellTimeMs, source, timestamp);
        }
    }
}
