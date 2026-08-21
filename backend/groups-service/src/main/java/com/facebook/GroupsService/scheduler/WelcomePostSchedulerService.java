package com.facebook.GroupsService.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class WelcomePostSchedulerService {

    private final Scheduler scheduler;

    /**
     * Dynamically schedules or reschedules a welcome post job for a group.
     */
    public void scheduleWelcomePost(String groupId, String welcomeMessage, String cronExpression, String creatorId) {
        log.info("Scheduling welcome post for group {} with cron: {}", groupId, cronExpression);
        
        JobKey jobKey = JobKey.jobKey("welcome-post-" + groupId, "welcome-posts");
        TriggerKey triggerKey = TriggerKey.triggerKey("welcome-trigger-" + groupId, "welcome-posts");

        try {
            JobDetail jobDetail = JobBuilder.newJob(WelcomePostJob.class)
                    .withIdentity(jobKey)
                    .usingJobData("groupId", groupId)
                    .usingJobData("welcomeMessage", welcomeMessage)
                    .usingJobData("creatorId", creatorId)
                    .storeDurably()
                    .build();

            CronTrigger trigger = TriggerBuilder.newTrigger()
                    .withIdentity(triggerKey)
                    .withSchedule(CronScheduleBuilder.cronSchedule(cronExpression)
                            .withMisfireHandlingInstructionDoNothing()) // prevents back-firing missed jobs immediately on startup
                    .build();

            if (scheduler.checkExists(jobKey)) {
                log.info("Job already exists. Rescheduling...");
                scheduler.rescheduleJob(triggerKey, trigger);
                // Re-store the job details to update any welcomeMessage changes in JobDataMap
                scheduler.addJob(jobDetail, true);
            } else {
                scheduler.scheduleJob(jobDetail, trigger);
            }
            
            log.info("Successfully scheduled welcome post job for group {}", groupId);
        } catch (Exception e) {
            log.error("Failed to schedule welcome post for group {}", groupId, e);
            throw new RuntimeException("Quartz scheduling failed: " + e.getMessage(), e);
        }
    }

    /**
     * Cancels and deletes the welcome post job for a group.
     */
    public void unscheduleWelcomePost(String groupId) {
        log.info("Unscheduling welcome post for group {}", groupId);
        JobKey jobKey = JobKey.jobKey("welcome-post-" + groupId, "welcome-posts");
        try {
            if (scheduler.checkExists(jobKey)) {
                scheduler.deleteJob(jobKey);
                log.info("Successfully deleted welcome post job for group {}", groupId);
            }
        } catch (Exception e) {
            log.error("Failed to delete welcome post job for group {}", groupId, e);
            throw new RuntimeException("Quartz unscheduling failed: " + e.getMessage(), e);
        }
    }
}
