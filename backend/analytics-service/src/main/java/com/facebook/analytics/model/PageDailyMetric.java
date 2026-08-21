package com.facebook.analytics.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "page_daily_metrics", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"page_id", "metric_date"})
})
public class PageDailyMetric {

    @Id
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "page_id", nullable = false)
    private UUID pageId;

    @Column(name = "metric_date", nullable = false)
    private LocalDate metricDate;

    @Column(name = "total_views")
    private Long totalViews = 0L;

    @Column(name = "follower_views")
    private Long followerViews = 0L;

    @Column(name = "non_follower_views")
    private Long nonFollowerViews = 0L;

    @Column(name = "text_views")
    private Long textViews = 0L;

    @Column(name = "photo_views")
    private Long photoViews = 0L;

    @Column(name = "video_views")
    private Long videoViews = 0L;

    @Column(name = "reactions_count")
    private Long reactionsCount = 0L;

    @Column(name = "comments_count")
    private Long commentsCount = 0L;

    @Column(name = "shares_count")
    private Long sharesCount = 0L;

    @Column(name = "net_followers")
    private Long netFollowers = 0L;

    @Column(name = "profile_visits")
    private Long profileVisits = 0L;

    // Video Telemetry
    @Column(name = "video_completions_count")
    private Long videoCompletionsCount = 0L;

    @Column(name = "video_loops_count")
    private Long videoLoopsCount = 0L;

    @Column(name = "audio_unmutes_count")
    private Long audioUnmutesCount = 0L;

    // Deep Content Intent
    @Column(name = "expand_text_count")
    private Long expandTextCount = 0L;

    @Column(name = "lightbox_opens_count")
    private Long lightboxOpensCount = 0L;

    @Column(name = "link_clicks_count")
    private Long linkClicksCount = 0L;

    @Column(name = "copy_link_count")
    private Long copyLinkCount = 0L;

    @Column(name = "saves_count")
    private Long savesCount = 0L;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public PageDailyMetric() {
        this.totalViews = 0L;
        this.followerViews = 0L;
        this.nonFollowerViews = 0L;
        this.textViews = 0L;
        this.photoViews = 0L;
        this.videoViews = 0L;
        this.reactionsCount = 0L;
        this.commentsCount = 0L;
        this.sharesCount = 0L;
        this.netFollowers = 0L;
        this.profileVisits = 0L;
        this.videoCompletionsCount = 0L;
        this.videoLoopsCount = 0L;
        this.audioUnmutesCount = 0L;
        this.expandTextCount = 0L;
        this.lightboxOpensCount = 0L;
        this.linkClicksCount = 0L;
        this.copyLinkCount = 0L;
        this.savesCount = 0L;
    }

    public PageDailyMetric(UUID id, UUID pageId, LocalDate metricDate, Long totalViews, Long followerViews,
                           Long nonFollowerViews, Long textViews, Long photoViews, Long videoViews,
                           Long reactionsCount, Long commentsCount, Long sharesCount, Long netFollowers,
                           Long profileVisits, Long videoCompletionsCount, Long videoLoopsCount,
                           Long audioUnmutesCount, Long expandTextCount, Long lightboxOpensCount,
                           Long linkClicksCount, Long copyLinkCount, Long savesCount, LocalDateTime updatedAt) {
        this.id = id;
        this.pageId = pageId;
        this.metricDate = metricDate;
        this.totalViews = totalViews != null ? totalViews : 0L;
        this.followerViews = followerViews != null ? followerViews : 0L;
        this.nonFollowerViews = nonFollowerViews != null ? nonFollowerViews : 0L;
        this.textViews = textViews != null ? textViews : 0L;
        this.photoViews = photoViews != null ? photoViews : 0L;
        this.videoViews = videoViews != null ? videoViews : 0L;
        this.reactionsCount = reactionsCount != null ? reactionsCount : 0L;
        this.commentsCount = commentsCount != null ? commentsCount : 0L;
        this.sharesCount = sharesCount != null ? sharesCount : 0L;
        this.netFollowers = netFollowers != null ? netFollowers : 0L;
        this.profileVisits = profileVisits != null ? profileVisits : 0L;
        this.videoCompletionsCount = videoCompletionsCount != null ? videoCompletionsCount : 0L;
        this.videoLoopsCount = videoLoopsCount != null ? videoLoopsCount : 0L;
        this.audioUnmutesCount = audioUnmutesCount != null ? audioUnmutesCount : 0L;
        this.expandTextCount = expandTextCount != null ? expandTextCount : 0L;
        this.lightboxOpensCount = lightboxOpensCount != null ? lightboxOpensCount : 0L;
        this.linkClicksCount = linkClicksCount != null ? linkClicksCount : 0L;
        this.copyLinkCount = copyLinkCount != null ? copyLinkCount : 0L;
        this.savesCount = savesCount != null ? savesCount : 0L;
        this.updatedAt = updatedAt != null ? updatedAt : LocalDateTime.now();
    }

    @PrePersist
    protected void onCreate() {
        if (this.id == null) {
            this.id = UUID.randomUUID();
        }
        if (this.updatedAt == null) {
            this.updatedAt = LocalDateTime.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getPageId() { return pageId; }
    public void setPageId(UUID pageId) { this.pageId = pageId; }

    public LocalDate getMetricDate() { return metricDate; }
    public void setMetricDate(LocalDate metricDate) { this.metricDate = metricDate; }

    public Long getTotalViews() { return totalViews != null ? totalViews : 0L; }
    public void setTotalViews(Long totalViews) { this.totalViews = totalViews; }

    public Long getFollowerViews() { return followerViews != null ? followerViews : 0L; }
    public void setFollowerViews(Long followerViews) { this.followerViews = followerViews; }

    public Long getNonFollowerViews() { return nonFollowerViews != null ? nonFollowerViews : 0L; }
    public void setNonFollowerViews(Long nonFollowerViews) { this.nonFollowerViews = nonFollowerViews; }

    public Long getTextViews() { return textViews != null ? textViews : 0L; }
    public void setTextViews(Long textViews) { this.textViews = textViews; }

    public Long getPhotoViews() { return photoViews != null ? photoViews : 0L; }
    public void setPhotoViews(Long photoViews) { this.photoViews = photoViews; }

    public Long getVideoViews() { return videoViews != null ? videoViews : 0L; }
    public void setVideoViews(Long videoViews) { this.videoViews = videoViews; }

    public Long getReactionsCount() { return reactionsCount != null ? reactionsCount : 0L; }
    public void setReactionsCount(Long reactionsCount) { this.reactionsCount = reactionsCount; }

    public Long getCommentsCount() { return commentsCount != null ? commentsCount : 0L; }
    public void setCommentsCount(Long commentsCount) { this.commentsCount = commentsCount; }

    public Long getSharesCount() { return sharesCount != null ? sharesCount : 0L; }
    public void setSharesCount(Long sharesCount) { this.sharesCount = sharesCount; }

    public Long getNetFollowers() { return netFollowers != null ? netFollowers : 0L; }
    public void setNetFollowers(Long netFollowers) { this.netFollowers = netFollowers; }

    public Long getProfileVisits() { return profileVisits != null ? profileVisits : 0L; }
    public void setProfileVisits(Long profileVisits) { this.profileVisits = profileVisits; }

    public Long getVideoCompletionsCount() { return videoCompletionsCount != null ? videoCompletionsCount : 0L; }
    public void setVideoCompletionsCount(Long videoCompletionsCount) { this.videoCompletionsCount = videoCompletionsCount; }

    public Long getVideoLoopsCount() { return videoLoopsCount != null ? videoLoopsCount : 0L; }
    public void setVideoLoopsCount(Long videoLoopsCount) { this.videoLoopsCount = videoLoopsCount; }

    public Long getAudioUnmutesCount() { return audioUnmutesCount != null ? audioUnmutesCount : 0L; }
    public void setAudioUnmutesCount(Long audioUnmutesCount) { this.audioUnmutesCount = audioUnmutesCount; }

    public Long getExpandTextCount() { return expandTextCount != null ? expandTextCount : 0L; }
    public void setExpandTextCount(Long expandTextCount) { this.expandTextCount = expandTextCount; }

    public Long getLightboxOpensCount() { return lightboxOpensCount != null ? lightboxOpensCount : 0L; }
    public void setLightboxOpensCount(Long lightboxOpensCount) { this.lightboxOpensCount = lightboxOpensCount; }

    public Long getLinkClicksCount() { return linkClicksCount != null ? linkClicksCount : 0L; }
    public void setLinkClicksCount(Long linkClicksCount) { this.linkClicksCount = linkClicksCount; }

    public Long getCopyLinkCount() { return copyLinkCount != null ? copyLinkCount : 0L; }
    public void setCopyLinkCount(Long copyLinkCount) { this.copyLinkCount = copyLinkCount; }

    public Long getSavesCount() { return savesCount != null ? savesCount : 0L; }
    public void setSavesCount(Long savesCount) { this.savesCount = savesCount; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public static PageDailyMetricBuilder builder() {
        return new PageDailyMetricBuilder();
    }

    public static class PageDailyMetricBuilder {
        private UUID id;
        private UUID pageId;
        private LocalDate metricDate;
        private Long totalViews = 0L;
        private Long followerViews = 0L;
        private Long nonFollowerViews = 0L;
        private Long textViews = 0L;
        private Long photoViews = 0L;
        private Long videoViews = 0L;
        private Long reactionsCount = 0L;
        private Long commentsCount = 0L;
        private Long sharesCount = 0L;
        private Long netFollowers = 0L;
        private Long profileVisits = 0L;
        private Long videoCompletionsCount = 0L;
        private Long videoLoopsCount = 0L;
        private Long audioUnmutesCount = 0L;
        private Long expandTextCount = 0L;
        private Long lightboxOpensCount = 0L;
        private Long linkClicksCount = 0L;
        private Long copyLinkCount = 0L;
        private Long savesCount = 0L;
        private LocalDateTime updatedAt;

        public PageDailyMetricBuilder id(UUID id) { this.id = id; return this; }
        public PageDailyMetricBuilder pageId(UUID pageId) { this.pageId = pageId; return this; }
        public PageDailyMetricBuilder metricDate(LocalDate metricDate) { this.metricDate = metricDate; return this; }
        public PageDailyMetricBuilder totalViews(Long totalViews) { this.totalViews = totalViews; return this; }
        public PageDailyMetricBuilder followerViews(Long followerViews) { this.followerViews = followerViews; return this; }
        public PageDailyMetricBuilder nonFollowerViews(Long nonFollowerViews) { this.nonFollowerViews = nonFollowerViews; return this; }
        public PageDailyMetricBuilder textViews(Long textViews) { this.textViews = textViews; return this; }
        public PageDailyMetricBuilder photoViews(Long photoViews) { this.photoViews = photoViews; return this; }
        public PageDailyMetricBuilder videoViews(Long videoViews) { this.videoViews = videoViews; return this; }
        public PageDailyMetricBuilder reactionsCount(Long reactionsCount) { this.reactionsCount = reactionsCount; return this; }
        public PageDailyMetricBuilder commentsCount(Long commentsCount) { this.commentsCount = commentsCount; return this; }
        public PageDailyMetricBuilder sharesCount(Long sharesCount) { this.sharesCount = sharesCount; return this; }
        public PageDailyMetricBuilder netFollowers(Long netFollowers) { this.netFollowers = netFollowers; return this; }
        public PageDailyMetricBuilder profileVisits(Long profileVisits) { this.profileVisits = profileVisits; return this; }
        public PageDailyMetricBuilder videoCompletionsCount(Long videoCompletionsCount) { this.videoCompletionsCount = videoCompletionsCount; return this; }
        public PageDailyMetricBuilder videoLoopsCount(Long videoLoopsCount) { this.videoLoopsCount = videoLoopsCount; return this; }
        public PageDailyMetricBuilder audioUnmutesCount(Long audioUnmutesCount) { this.audioUnmutesCount = audioUnmutesCount; return this; }
        public PageDailyMetricBuilder expandTextCount(Long expandTextCount) { this.expandTextCount = expandTextCount; return this; }
        public PageDailyMetricBuilder lightboxOpensCount(Long lightboxOpensCount) { this.lightboxOpensCount = lightboxOpensCount; return this; }
        public PageDailyMetricBuilder linkClicksCount(Long linkClicksCount) { this.linkClicksCount = linkClicksCount; return this; }
        public PageDailyMetricBuilder copyLinkCount(Long copyLinkCount) { this.copyLinkCount = copyLinkCount; return this; }
        public PageDailyMetricBuilder savesCount(Long savesCount) { this.savesCount = savesCount; return this; }
        public PageDailyMetricBuilder updatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; return this; }

        public PageDailyMetric build() {
            return new PageDailyMetric(id, pageId, metricDate, totalViews, followerViews, nonFollowerViews,
                    textViews, photoViews, videoViews, reactionsCount, commentsCount, sharesCount,
                    netFollowers, profileVisits, videoCompletionsCount, videoLoopsCount, audioUnmutesCount,
                    expandTextCount, lightboxOpensCount, linkClicksCount, copyLinkCount, savesCount, updatedAt);
        }
    }
}
