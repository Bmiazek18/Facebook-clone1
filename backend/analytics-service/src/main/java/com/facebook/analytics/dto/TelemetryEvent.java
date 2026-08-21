package com.facebook.analytics.dto;

import java.io.Serializable;

public class TelemetryEvent implements Serializable {

    private String eventType; // IMPRESSION, VIDEO_PROGRESS, VIDEO_LOOP, AUDIO_UNMUTE, EXPAND_TEXT, LIGHTBOX_OPEN, LINK_CLICK, COPY_LINK, SAVE_POST
    private String postId;
    private String pageId;
    private String userId;
    private Double completionPercent; // 25, 50, 75, 100
    private Integer loopCount;
    private String linkUrl;
    private Long dwellTimeMs;
    private String contentType; // text, photo, video, reel
    private Boolean isFollower;
    private String source;
    private Long timestamp;

    public TelemetryEvent() {}

    public TelemetryEvent(String eventType, String postId, String pageId, String userId,
                          Double completionPercent, Integer loopCount, String linkUrl,
                          Long dwellTimeMs, String contentType, Boolean isFollower,
                          String source, Long timestamp) {
        this.eventType = eventType;
        this.postId = postId;
        this.pageId = pageId;
        this.userId = userId;
        this.completionPercent = completionPercent;
        this.loopCount = loopCount;
        this.linkUrl = linkUrl;
        this.dwellTimeMs = dwellTimeMs;
        this.contentType = contentType;
        this.isFollower = isFollower;
        this.source = source;
        this.timestamp = timestamp != null ? timestamp : System.currentTimeMillis();
    }

    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }

    public String getPostId() { return postId; }
    public void setPostId(String postId) { this.postId = postId; }

    public String getPageId() { return pageId; }
    public void setPageId(String pageId) { this.pageId = pageId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public Double getCompletionPercent() { return completionPercent; }
    public void setCompletionPercent(Double completionPercent) { this.completionPercent = completionPercent; }

    public Integer getLoopCount() { return loopCount; }
    public void setLoopCount(Integer loopCount) { this.loopCount = loopCount; }

    public String getLinkUrl() { return linkUrl; }
    public void setLinkUrl(String linkUrl) { this.linkUrl = linkUrl; }

    public Long getDwellTimeMs() { return dwellTimeMs; }
    public void setDwellTimeMs(Long dwellTimeMs) { this.dwellTimeMs = dwellTimeMs; }

    public String getContentType() { return contentType; }
    public void setContentType(String contentType) { this.contentType = contentType; }

    public Boolean getIsFollower() { return isFollower; }
    public void setIsFollower(Boolean isFollower) { this.isFollower = isFollower; }

    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }

    public Long getTimestamp() { return timestamp; }
    public void setTimestamp(Long timestamp) { this.timestamp = timestamp; }

    public static TelemetryEventBuilder builder() { return new TelemetryEventBuilder(); }

    public static class TelemetryEventBuilder {
        private String eventType;
        private String postId;
        private String pageId;
        private String userId;
        private Double completionPercent;
        private Integer loopCount;
        private String linkUrl;
        private Long dwellTimeMs;
        private String contentType;
        private Boolean isFollower;
        private String source;
        private Long timestamp;

        public TelemetryEventBuilder eventType(String eventType) { this.eventType = eventType; return this; }
        public TelemetryEventBuilder postId(String postId) { this.postId = postId; return this; }
        public TelemetryEventBuilder pageId(String pageId) { this.pageId = pageId; return this; }
        public TelemetryEventBuilder userId(String userId) { this.userId = userId; return this; }
        public TelemetryEventBuilder completionPercent(Double completionPercent) { this.completionPercent = completionPercent; return this; }
        public TelemetryEventBuilder loopCount(Integer loopCount) { this.loopCount = loopCount; return this; }
        public TelemetryEventBuilder linkUrl(String linkUrl) { this.linkUrl = linkUrl; return this; }
        public TelemetryEventBuilder dwellTimeMs(Long dwellTimeMs) { this.dwellTimeMs = dwellTimeMs; return this; }
        public TelemetryEventBuilder contentType(String contentType) { this.contentType = contentType; return this; }
        public TelemetryEventBuilder isFollower(Boolean isFollower) { this.isFollower = isFollower; return this; }
        public TelemetryEventBuilder source(String source) { this.source = source; return this; }
        public TelemetryEventBuilder timestamp(Long timestamp) { this.timestamp = timestamp; return this; }

        public TelemetryEvent build() {
            return new TelemetryEvent(eventType, postId, pageId, userId, completionPercent, loopCount, linkUrl, dwellTimeMs, contentType, isFollower, source, timestamp);
        }
    }
}
