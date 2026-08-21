package com.facebook.GroupsService.service;

import com.facebook.GroupsService.entity.AdminAssistRuleEntity;
import com.facebook.GroupsService.repository.AdminAssistRuleRepository;
import com.facebook.GroupsService.scheduler.WelcomePostSchedulerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AdminAssistRuleService {

    private final AdminAssistRuleRepository ruleRepository;
    private final WelcomePostSchedulerService welcomePostSchedulerService;

    /**
     * Saves a rule and dynamically schedules/unschedules the Quartz job if it's a welcome post.
     */
    @Transactional
    public AdminAssistRuleEntity saveRule(AdminAssistRuleEntity rule, String creatorId) {
        if (rule.getId() == null || rule.getId().isEmpty()) {
            rule.setId(UUID.randomUUID().toString());
        }

        AdminAssistRuleEntity saved = ruleRepository.save(rule);

        // If the rule is a welcome post, dynamically schedule it with Quartz
        if (com.facebook.GroupsService.entity.RuleTarget.WELCOME_POST == saved.getTarget() 
                && com.facebook.GroupsService.entity.RuleAction.PUBLISH == saved.getAction()) {
            if (saved.isEnabled() && saved.getCriteria() != null 
                    && saved.getCriteria().getCronExpression() != null 
                    && saved.getCriteria().getWelcomeMessage() != null) {
                
                String cronExpression = saved.getCriteria().getCronExpression();
                if (!org.quartz.CronExpression.isValidExpression(cronExpression)) {
                    throw new IllegalArgumentException("Niepoprawne wyrażenie Cron dla posta powitalnego: " + cronExpression);
                }
                
                welcomePostSchedulerService.scheduleWelcomePost(
                        saved.getGroupId(),
                        saved.getCriteria().getWelcomeMessage(),
                        cronExpression,
                        creatorId
                );
            } else {
                welcomePostSchedulerService.unscheduleWelcomePost(saved.getGroupId());
            }
        }

        return saved;
    }

    /**
     * Deletes a rule and unschedules the Quartz job if it was a welcome post.
     */
    @Transactional
    public void deleteRule(String ruleId) {
        ruleRepository.findById(ruleId).ifPresent(rule -> {
            ruleRepository.delete(rule);
            if (com.facebook.GroupsService.entity.RuleTarget.WELCOME_POST == rule.getTarget()) {
                welcomePostSchedulerService.unscheduleWelcomePost(rule.getGroupId());
            }
        });
    }
}
