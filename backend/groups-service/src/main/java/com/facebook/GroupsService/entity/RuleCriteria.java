package com.facebook.GroupsService.entity;

import java.util.List;

public class RuleCriteria {
    // For profile age check (e.g. account created less than X days ago)
    private Integer minimumAccountAgeDays;
    
    // For profile picture check
    private Boolean requireProfilePicture;
    
    // For spam/reports
    private Integer reportCountThreshold;
    private Integer timeWindowMinutes;
    
    // For welcome posts
    private String welcomeMessage;
    private String cronExpression;
    
    // For keywords
    private List<String> bannedKeywords;

    public RuleCriteria() {
    }

    public RuleCriteria(Integer minimumAccountAgeDays, Boolean requireProfilePicture, Integer reportCountThreshold, Integer timeWindowMinutes, String welcomeMessage, String cronExpression, List<String> bannedKeywords) {
        this.minimumAccountAgeDays = minimumAccountAgeDays;
        this.requireProfilePicture = requireProfilePicture;
        this.reportCountThreshold = reportCountThreshold;
        this.timeWindowMinutes = timeWindowMinutes;
        this.welcomeMessage = welcomeMessage;
        this.cronExpression = cronExpression;
        this.bannedKeywords = bannedKeywords;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Integer minimumAccountAgeDays;
        private Boolean requireProfilePicture;
        private Integer reportCountThreshold;
        private Integer timeWindowMinutes;
        private String welcomeMessage;
        private String cronExpression;
        private List<String> bannedKeywords;

        public Builder minimumAccountAgeDays(Integer minimumAccountAgeDays) {
            this.minimumAccountAgeDays = minimumAccountAgeDays;
            return this;
        }

        public Builder requireProfilePicture(Boolean requireProfilePicture) {
            this.requireProfilePicture = requireProfilePicture;
            return this;
        }

        public Builder reportCountThreshold(Integer reportCountThreshold) {
            this.reportCountThreshold = reportCountThreshold;
            return this;
        }

        public Builder timeWindowMinutes(Integer timeWindowMinutes) {
            this.timeWindowMinutes = timeWindowMinutes;
            return this;
        }

        public Builder welcomeMessage(String welcomeMessage) {
            this.welcomeMessage = welcomeMessage;
            return this;
        }

        public Builder cronExpression(String cronExpression) {
            this.cronExpression = cronExpression;
            return this;
        }

        public Builder bannedKeywords(List<String> bannedKeywords) {
            this.bannedKeywords = bannedKeywords;
            return this;
        }

        public RuleCriteria build() {
            return new RuleCriteria(minimumAccountAgeDays, requireProfilePicture, reportCountThreshold, timeWindowMinutes, welcomeMessage, cronExpression, bannedKeywords);
        }
    }

    public Integer getMinimumAccountAgeDays() {
        return minimumAccountAgeDays;
    }

    public void setMinimumAccountAgeDays(Integer minimumAccountAgeDays) {
        this.minimumAccountAgeDays = minimumAccountAgeDays;
    }

    public Boolean getRequireProfilePicture() {
        return requireProfilePicture;
    }

    public void setRequireProfilePicture(Boolean requireProfilePicture) {
        this.requireProfilePicture = requireProfilePicture;
    }

    public Integer getReportCountThreshold() {
        return reportCountThreshold;
    }

    public void setReportCountThreshold(Integer reportCountThreshold) {
        this.reportCountThreshold = reportCountThreshold;
    }

    public Integer getTimeWindowMinutes() {
        return timeWindowMinutes;
    }

    public void setTimeWindowMinutes(Integer timeWindowMinutes) {
        this.timeWindowMinutes = timeWindowMinutes;
    }

    public String getWelcomeMessage() {
        return welcomeMessage;
    }

    public void setWelcomeMessage(String welcomeMessage) {
        this.welcomeMessage = welcomeMessage;
    }

    public String getCronExpression() {
        return cronExpression;
    }

    public void setCronExpression(String cronExpression) {
        this.cronExpression = cronExpression;
    }

    public List<String> getBannedKeywords() {
        return bannedKeywords;
    }

    public void setBannedKeywords(List<String> bannedKeywords) {
        this.bannedKeywords = bannedKeywords;
    }
}
