package com.facebook.GroupsService.rule;

import com.facebook.GroupsService.entity.RuleCriteria;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Component
public class ProfileAgeEvaluator implements RuleEvaluator {

    @Override
    public boolean supports(RuleCriteria criteria) {
        return criteria.getMinimumAccountAgeDays() != null;
    }

    @Override
    public boolean evaluate(RuleCriteria criteria, RuleContext context) {
        if (context.getUserDetails() == null || context.getUserDetails().getCreatedAt() == null || context.getUserDetails().getCreatedAt().isEmpty()) {
            return false;
        }

        try {
            String createdAtStr = context.getUserDetails().getCreatedAt();
            LocalDateTime createdAt;
            if (createdAtStr.contains("T")) {
                createdAt = LocalDateTime.parse(createdAtStr);
            } else {
                createdAt = LocalDateTime.ofInstant(java.time.Instant.parse(createdAtStr), java.time.ZoneId.systemDefault());
            }

            long daysBetween = ChronoUnit.DAYS.between(createdAt, LocalDateTime.now());
            // Triggers (returns true) if the profile age is LESS than the required threshold
            return daysBetween < criteria.getMinimumAccountAgeDays();
        } catch (Exception e) {
            return false;
        }
    }
}
