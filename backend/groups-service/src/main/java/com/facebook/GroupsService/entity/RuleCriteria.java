package com.facebook.GroupsService.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
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
}
