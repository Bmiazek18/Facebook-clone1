package com.facebook.analytics.dto;

import java.util.List;
import java.util.Map;

public class UserFeatureVectorDto {
    private String userId;
    private Long totalInteractions;
    private Double avgDwellTimeMs;
    private Long totalFastSkips;
    private Long totalHides;
    private List<String> topInteractedTargetIds;
    private Map<String, Long> categoryInteractionCounts;
    private List<RawInteractionItem> recentInteractions;

    public UserFeatureVectorDto() {}

    public UserFeatureVectorDto(String userId, Long totalInteractions, Double avgDwellTimeMs,
                                Long totalFastSkips, Long totalHides, List<String> topInteractedTargetIds,
                                Map<String, Long> categoryInteractionCounts, List<RawInteractionItem> recentInteractions) {
        this.userId = userId;
        this.totalInteractions = totalInteractions;
        this.avgDwellTimeMs = avgDwellTimeMs;
        this.totalFastSkips = totalFastSkips;
        this.totalHides = totalHides;
        this.topInteractedTargetIds = topInteractedTargetIds;
        this.categoryInteractionCounts = categoryInteractionCounts;
        this.recentInteractions = recentInteractions;
    }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public Long getTotalInteractions() { return totalInteractions; }
    public void setTotalInteractions(Long totalInteractions) { this.totalInteractions = totalInteractions; }

    public Double getAvgDwellTimeMs() { return avgDwellTimeMs; }
    public void setAvgDwellTimeMs(Double avgDwellTimeMs) { this.avgDwellTimeMs = avgDwellTimeMs; }

    public Long getTotalFastSkips() { return totalFastSkips; }
    public void setTotalFastSkips(Long totalFastSkips) { this.totalFastSkips = totalFastSkips; }

    public Long getTotalHides() { return totalHides; }
    public void setTotalHides(Long totalHides) { this.totalHides = totalHides; }

    public List<String> getTopInteractedTargetIds() { return topInteractedTargetIds; }
    public void setTopInteractedTargetIds(List<String> topInteractedTargetIds) { this.topInteractedTargetIds = topInteractedTargetIds; }

    public Map<String, Long> getCategoryInteractionCounts() { return categoryInteractionCounts; }
    public void setCategoryInteractionCounts(Map<String, Long> categoryInteractionCounts) { this.categoryInteractionCounts = categoryInteractionCounts; }

    public List<RawInteractionItem> getRecentInteractions() { return recentInteractions; }
    public void setRecentInteractions(List<RawInteractionItem> recentInteractions) { this.recentInteractions = recentInteractions; }

    public static class RawInteractionItem {
        private String targetId;
        private String targetType;
        private String category;
        private Long dwellTimeMs;
        private Double videoCompletionPercent;
        private Integer videoLoopCount;
        private Boolean hasReaction;
        private Boolean hasComment;
        private Boolean hasShare;
        private Boolean isSaved;
        private Boolean isSkipped;
        private Boolean isHidden;
        private String lastInteractedAt;

        public RawInteractionItem() {}

        public RawInteractionItem(String targetId, String targetType, String category, Long dwellTimeMs,
                                  Double videoCompletionPercent, Integer videoLoopCount, Boolean hasReaction,
                                  Boolean hasComment, Boolean hasShare, Boolean isSaved, Boolean isSkipped,
                                  Boolean isHidden, String lastInteractedAt) {
            this.targetId = targetId;
            this.targetType = targetType;
            this.category = category;
            this.dwellTimeMs = dwellTimeMs;
            this.videoCompletionPercent = videoCompletionPercent;
            this.videoLoopCount = videoLoopCount;
            this.hasReaction = hasReaction;
            this.hasComment = hasComment;
            this.hasShare = hasShare;
            this.isSaved = isSaved;
            this.isSkipped = isSkipped;
            this.isHidden = isHidden;
            this.lastInteractedAt = lastInteractedAt;
        }

        public String getTargetId() { return targetId; }
        public void setTargetId(String targetId) { this.targetId = targetId; }

        public String getTargetType() { return targetType; }
        public void setTargetType(String targetType) { this.targetType = targetType; }

        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }

        public Long getDwellTimeMs() { return dwellTimeMs; }
        public void setDwellTimeMs(Long dwellTimeMs) { this.dwellTimeMs = dwellTimeMs; }

        public Double getVideoCompletionPercent() { return videoCompletionPercent; }
        public void setVideoCompletionPercent(Double videoCompletionPercent) { this.videoCompletionPercent = videoCompletionPercent; }

        public Integer getVideoLoopCount() { return videoLoopCount; }
        public void setVideoLoopCount(Integer videoLoopCount) { this.videoLoopCount = videoLoopCount; }

        public Boolean getHasReaction() { return hasReaction; }
        public void setHasReaction(Boolean hasReaction) { this.hasReaction = hasReaction; }

        public Boolean getHasComment() { return hasComment; }
        public void setHasComment(Boolean hasComment) { this.hasComment = hasComment; }

        public Boolean getHasShare() { return hasShare; }
        public void setHasShare(Boolean hasShare) { this.hasShare = hasShare; }

        public Boolean getIsSaved() { return isSaved; }
        public void setIsSaved(Boolean isSaved) { this.isSaved = isSaved; }

        public Boolean getIsSkipped() { return isSkipped; }
        public void setIsSkipped(Boolean isSkipped) { this.isSkipped = isSkipped; }

        public Boolean getIsHidden() { return isHidden; }
        public void setIsHidden(Boolean isHidden) { this.isHidden = isHidden; }

        public String getLastInteractedAt() { return lastInteractedAt; }
        public void setLastInteractedAt(String lastInteractedAt) { this.lastInteractedAt = lastInteractedAt; }
    }
}
