package com.facebook.analytics.dto;

import java.time.LocalDateTime;

public class BotDto {
    private String userId;
    private String reason;
    private Long eventCount;
    private LocalDateTime detectedAt;

    public BotDto() {}

    public BotDto(String userId, String reason, Long eventCount, LocalDateTime detectedAt) {
        this.userId = userId;
        this.reason = reason;
        this.eventCount = eventCount;
        this.detectedAt = detectedAt;
    }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public Long getEventCount() { return eventCount; }
    public void setEventCount(Long eventCount) { this.eventCount = eventCount; }

    public LocalDateTime getDetectedAt() { return detectedAt; }
    public void setDetectedAt(LocalDateTime detectedAt) { this.detectedAt = detectedAt; }
}
