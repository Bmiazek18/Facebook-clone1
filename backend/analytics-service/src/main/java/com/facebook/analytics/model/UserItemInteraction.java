package com.facebook.analytics.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "user_item_interactions", indexes = {
        @Index(name = "idx_user_interaction_user_id", columnList = "user_id"),
        @Index(name = "idx_user_interaction_target_id", columnList = "target_id"),
        @Index(name = "idx_user_interaction_category", columnList = "category")
}, uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "target_id"})
})
public class UserItemInteraction {

    @Id
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "target_id", nullable = false)
    private String targetId;

    @Column(name = "target_type")
    private String targetType; // "POST", "PAGE"

    @Column(name = "category")
    private String category;

    @Column(name = "total_dwell_time_ms")
    private Long totalDwellTimeMs = 0L;

    @Column(name = "interaction_count")
    private Long interactionCount = 0L;

    @Column(name = "video_completion_percent")
    private Double videoCompletionPercent = 0.0;

    @Column(name = "video_loop_count")
    private Integer videoLoopCount = 0;

    @Column(name = "has_reaction")
    private Boolean hasReaction = false;

    @Column(name = "has_comment")
    private Boolean hasComment = false;

    @Column(name = "has_share")
    private Boolean hasShare = false;

    @Column(name = "is_saved")
    private Boolean isSaved = false;

    @Column(name = "is_skipped")
    private Boolean isSkipped = false;

    @Column(name = "is_hidden")
    private Boolean isHidden = false;

    @Column(name = "first_interacted_at")
    private LocalDateTime firstInteractedAt;

    @Column(name = "last_interacted_at")
    private LocalDateTime lastInteractedAt;

    public UserItemInteraction() {
        this.totalDwellTimeMs = 0L;
        this.interactionCount = 0L;
        this.videoCompletionPercent = 0.0;
        this.videoLoopCount = 0;
        this.hasReaction = false;
        this.hasComment = false;
        this.hasShare = false;
        this.isSaved = false;
        this.isSkipped = false;
        this.isHidden = false;
    }

    @PrePersist
    protected void onCreate() {
        if (this.id == null) {
            this.id = UUID.randomUUID();
        }
        if (this.firstInteractedAt == null) {
            this.firstInteractedAt = LocalDateTime.now();
        }
        if (this.lastInteractedAt == null) {
            this.lastInteractedAt = LocalDateTime.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.lastInteractedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getTargetId() { return targetId; }
    public void setTargetId(String targetId) { this.targetId = targetId; }

    public String getTargetType() { return targetType; }
    public void setTargetType(String targetType) { this.targetType = targetType; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public Long getTotalDwellTimeMs() { return totalDwellTimeMs != null ? totalDwellTimeMs : 0L; }
    public void setTotalDwellTimeMs(Long totalDwellTimeMs) { this.totalDwellTimeMs = totalDwellTimeMs; }

    public Long getInteractionCount() { return interactionCount != null ? interactionCount : 0L; }
    public void setInteractionCount(Long interactionCount) { this.interactionCount = interactionCount; }

    public Double getVideoCompletionPercent() { return videoCompletionPercent != null ? videoCompletionPercent : 0.0; }
    public void setVideoCompletionPercent(Double videoCompletionPercent) { this.videoCompletionPercent = videoCompletionPercent; }

    public Integer getVideoLoopCount() { return videoLoopCount != null ? videoLoopCount : 0; }
    public void setVideoLoopCount(Integer videoLoopCount) { this.videoLoopCount = videoLoopCount; }

    public Boolean getHasReaction() { return Boolean.TRUE.equals(hasReaction); }
    public void setHasReaction(Boolean hasReaction) { this.hasReaction = hasReaction; }

    public Boolean getHasComment() { return Boolean.TRUE.equals(hasComment); }
    public void setHasComment(Boolean hasComment) { this.hasComment = hasComment; }

    public Boolean getHasShare() { return Boolean.TRUE.equals(hasShare); }
    public void setHasShare(Boolean hasShare) { this.hasShare = hasShare; }

    public Boolean getIsSaved() { return Boolean.TRUE.equals(isSaved); }
    public void setIsSaved(Boolean isSaved) { this.isSaved = isSaved; }

    public Boolean getIsSkipped() { return Boolean.TRUE.equals(isSkipped); }
    public void setIsSkipped(Boolean isSkipped) { this.isSkipped = isSkipped; }

    public Boolean getIsHidden() { return Boolean.TRUE.equals(isHidden); }
    public void setIsHidden(Boolean isHidden) { this.isHidden = isHidden; }

    public LocalDateTime getFirstInteractedAt() { return firstInteractedAt; }
    public void setFirstInteractedAt(LocalDateTime firstInteractedAt) { this.firstInteractedAt = firstInteractedAt; }

    public LocalDateTime getLastInteractedAt() { return lastInteractedAt; }
    public void setLastInteractedAt(LocalDateTime lastInteractedAt) { this.lastInteractedAt = lastInteractedAt; }
}
