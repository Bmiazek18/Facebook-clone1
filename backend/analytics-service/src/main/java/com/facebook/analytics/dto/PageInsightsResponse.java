package com.facebook.analytics.dto;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class PageInsightsResponse {

    private UUID pageId;
    private String period; // "7d", "28d", "90d"

    // Summary statistics
    private Long totalViews;
    private Double viewsGrowthPercent;
    private Long netFollowers;
    private Double followersGrowthPercent;
    private Long totalReactions;
    private Long totalComments;
    private Long totalShares;
    private Long totalProfileVisits;

    // Breakdown for Views Tab
    private Double followerViewsPercent;
    private Double nonFollowerViewsPercent;
    private Map<String, Long> viewsByContentType;

    // Video Telemetry
    private VideoMetrics videoMetrics;

    // Deep Engagement (Outbound links, saves, dark social copy, expands)
    private DeepEngagement deepEngagement;

    // Timeline for charts
    private List<DailyMetricPoint> timeline;

    // Audience Demographics for Audience Tab
    private AudienceSummary audience;

    public PageInsightsResponse() {}

    public PageInsightsResponse(UUID pageId, String period, Long totalViews, Double viewsGrowthPercent,
                                Long netFollowers, Double followersGrowthPercent, Long totalReactions,
                                Long totalComments, Long totalShares, Long totalProfileVisits,
                                Double followerViewsPercent, Double nonFollowerViewsPercent,
                                Map<String, Long> viewsByContentType, VideoMetrics videoMetrics,
                                DeepEngagement deepEngagement, List<DailyMetricPoint> timeline,
                                AudienceSummary audience) {
        this.pageId = pageId;
        this.period = period;
        this.totalViews = totalViews;
        this.viewsGrowthPercent = viewsGrowthPercent;
        this.netFollowers = netFollowers;
        this.followersGrowthPercent = followersGrowthPercent;
        this.totalReactions = totalReactions;
        this.totalComments = totalComments;
        this.totalShares = totalShares;
        this.totalProfileVisits = totalProfileVisits;
        this.followerViewsPercent = followerViewsPercent;
        this.nonFollowerViewsPercent = nonFollowerViewsPercent;
        this.viewsByContentType = viewsByContentType;
        this.videoMetrics = videoMetrics;
        this.deepEngagement = deepEngagement;
        this.timeline = timeline;
        this.audience = audience;
    }

    public UUID getPageId() { return pageId; }
    public void setPageId(UUID pageId) { this.pageId = pageId; }

    public String getPeriod() { return period; }
    public void setPeriod(String period) { this.period = period; }

    public Long getTotalViews() { return totalViews; }
    public void setTotalViews(Long totalViews) { this.totalViews = totalViews; }

    public Double getViewsGrowthPercent() { return viewsGrowthPercent; }
    public void setViewsGrowthPercent(Double viewsGrowthPercent) { this.viewsGrowthPercent = viewsGrowthPercent; }

    public Long getNetFollowers() { return netFollowers; }
    public void setNetFollowers(Long netFollowers) { this.netFollowers = netFollowers; }

    public Double getFollowersGrowthPercent() { return followersGrowthPercent; }
    public void setFollowersGrowthPercent(Double followersGrowthPercent) { this.followersGrowthPercent = followersGrowthPercent; }

    public Long getTotalReactions() { return totalReactions; }
    public void setTotalReactions(Long totalReactions) { this.totalReactions = totalReactions; }

    public Long getTotalComments() { return totalComments; }
    public void setTotalComments(Long totalComments) { this.totalComments = totalComments; }

    public Long getTotalShares() { return totalShares; }
    public void setTotalShares(Long totalShares) { this.totalShares = totalShares; }

    public Long getTotalProfileVisits() { return totalProfileVisits; }
    public void setTotalProfileVisits(Long totalProfileVisits) { this.totalProfileVisits = totalProfileVisits; }

    public Double getFollowerViewsPercent() { return followerViewsPercent; }
    public void setFollowerViewsPercent(Double followerViewsPercent) { this.followerViewsPercent = followerViewsPercent; }

    public Double getNonFollowerViewsPercent() { return nonFollowerViewsPercent; }
    public void setNonFollowerViewsPercent(Double nonFollowerViewsPercent) { this.nonFollowerViewsPercent = nonFollowerViewsPercent; }

    public Map<String, Long> getViewsByContentType() { return viewsByContentType; }
    public void setViewsByContentType(Map<String, Long> viewsByContentType) { this.viewsByContentType = viewsByContentType; }

    public VideoMetrics getVideoMetrics() { return videoMetrics; }
    public void setVideoMetrics(VideoMetrics videoMetrics) { this.videoMetrics = videoMetrics; }

    public DeepEngagement getDeepEngagement() { return deepEngagement; }
    public void setDeepEngagement(DeepEngagement deepEngagement) { this.deepEngagement = deepEngagement; }

    public List<DailyMetricPoint> getTimeline() { return timeline; }
    public void setTimeline(List<DailyMetricPoint> timeline) { this.timeline = timeline; }

    public AudienceSummary getAudience() { return audience; }
    public void setAudience(AudienceSummary audience) { this.audience = audience; }

    public static class VideoMetrics {
        private Long completionsCount;
        private Long loopsCount;
        private Long unmutesCount;
        private Double avgCompletionPercent;

        public VideoMetrics() {}
        public VideoMetrics(Long completionsCount, Long loopsCount, Long unmutesCount, Double avgCompletionPercent) {
            this.completionsCount = completionsCount;
            this.loopsCount = loopsCount;
            this.unmutesCount = unmutesCount;
            this.avgCompletionPercent = avgCompletionPercent;
        }

        public Long getCompletionsCount() { return completionsCount; }
        public void setCompletionsCount(Long completionsCount) { this.completionsCount = completionsCount; }

        public Long getLoopsCount() { return loopsCount; }
        public void setLoopsCount(Long loopsCount) { this.loopsCount = loopsCount; }

        public Long getUnmutesCount() { return unmutesCount; }
        public void setUnmutesCount(Long unmutesCount) { this.unmutesCount = unmutesCount; }

        public Double getAvgCompletionPercent() { return avgCompletionPercent; }
        public void setAvgCompletionPercent(Double avgCompletionPercent) { this.avgCompletionPercent = avgCompletionPercent; }
    }

    public static class DeepEngagement {
        private Long expandTextCount;
        private Long lightboxOpensCount;
        private Long linkClicksCount;
        private Long copyLinkCount;
        private Long savesCount;

        public DeepEngagement() {}
        public DeepEngagement(Long expandTextCount, Long lightboxOpensCount, Long linkClicksCount, Long copyLinkCount, Long savesCount) {
            this.expandTextCount = expandTextCount;
            this.lightboxOpensCount = lightboxOpensCount;
            this.linkClicksCount = linkClicksCount;
            this.copyLinkCount = copyLinkCount;
            this.savesCount = savesCount;
        }

        public Long getExpandTextCount() { return expandTextCount; }
        public void setExpandTextCount(Long expandTextCount) { this.expandTextCount = expandTextCount; }

        public Long getLightboxOpensCount() { return lightboxOpensCount; }
        public void setLightboxOpensCount(Long lightboxOpensCount) { this.lightboxOpensCount = lightboxOpensCount; }

        public Long getLinkClicksCount() { return linkClicksCount; }
        public void setLinkClicksCount(Long linkClicksCount) { this.linkClicksCount = linkClicksCount; }

        public Long getCopyLinkCount() { return copyLinkCount; }
        public void setCopyLinkCount(Long copyLinkCount) { this.copyLinkCount = copyLinkCount; }

        public Long getSavesCount() { return savesCount; }
        public void setSavesCount(Long savesCount) { this.savesCount = savesCount; }
    }

    public static class DailyMetricPoint {
        private String date;
        private Long views;
        private Long reactions;
        private Long comments;
        private Long visits;
        private Long followers;

        public DailyMetricPoint() {}
        public DailyMetricPoint(String date, Long views, Long reactions, Long comments, Long visits, Long followers) {
            this.date = date;
            this.views = views;
            this.reactions = reactions;
            this.comments = comments;
            this.visits = visits;
            this.followers = followers;
        }

        public String getDate() { return date; }
        public void setDate(String date) { this.date = date; }

        public Long getViews() { return views; }
        public void setViews(Long views) { this.views = views; }

        public Long getReactions() { return reactions; }
        public void setReactions(Long reactions) { this.reactions = reactions; }

        public Long getComments() { return comments; }
        public void setComments(Long comments) { this.comments = comments; }

        public Long getVisits() { return visits; }
        public void setVisits(Long visits) { this.visits = visits; }

        public Long getFollowers() { return followers; }
        public void setFollowers(Long followers) { this.followers = followers; }
    }

    public static class AudienceSummary {
        private Double womenPercent;
        private Double menPercent;
        private Double otherPercent;
        private Map<String, Double> ageGroups;
        private List<TopLocation> topCities;
        private List<TopLocation> topCountries;

        public AudienceSummary() {}
        public AudienceSummary(Double womenPercent, Double menPercent, Double otherPercent,
                               Map<String, Double> ageGroups, List<TopLocation> topCities,
                               List<TopLocation> topCountries) {
            this.womenPercent = womenPercent;
            this.menPercent = menPercent;
            this.otherPercent = otherPercent;
            this.ageGroups = ageGroups;
            this.topCities = topCities;
            this.topCountries = topCountries;
        }

        public Double getWomenPercent() { return womenPercent; }
        public void setWomenPercent(Double womenPercent) { this.womenPercent = womenPercent; }

        public Double getMenPercent() { return menPercent; }
        public void setMenPercent(Double menPercent) { this.menPercent = menPercent; }

        public Double getOtherPercent() { return otherPercent; }
        public void setOtherPercent(Double otherPercent) { this.otherPercent = otherPercent; }

        public Map<String, Double> getAgeGroups() { return ageGroups; }
        public void setAgeGroups(Map<String, Double> ageGroups) { this.ageGroups = ageGroups; }

        public List<TopLocation> getTopCities() { return topCities; }
        public void setTopCities(List<TopLocation> topCities) { this.topCities = topCities; }

        public List<TopLocation> getTopCountries() { return topCountries; }
        public void setTopCountries(List<TopLocation> topCountries) { this.topCountries = topCountries; }
    }

    public static class TopLocation {
        private String name;
        private Double percent;

        public TopLocation() {}
        public TopLocation(String name, Double percent) {
            this.name = name;
            this.percent = percent;
        }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public Double getPercent() { return percent; }
        public void setPercent(Double percent) { this.percent = percent; }
    }
}
