package com.facebook.GroupsService.scheduler;

import com.facebook.feed.grpc.CreatePostRequest;
import com.facebook.feed.grpc.FeedGrpcServiceGrpc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.stereotype.Component;

@Component
public class WelcomePostJob implements Job {

    private static final Logger log = LoggerFactory.getLogger(WelcomePostJob.class);

    @org.springframework.beans.factory.annotation.Autowired
    private com.facebook.GroupsService.service.WelcomePostPublisherService welcomePostPublisher;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        JobDataMap dataMap = context.getMergedJobDataMap();
        String groupId = dataMap.getString("groupId");
        String welcomeMessage = dataMap.getString("welcomeMessage");
        String creatorId = dataMap.getString("creatorId");

        log.info("WelcomePostJob: Executing welcome post for group {} by creator {}", groupId, creatorId);

        try {
            welcomePostPublisher.publishWelcomePost(groupId, welcomeMessage, creatorId);
        } catch (Exception e) {
            log.error("WelcomePostJob: Failed to post welcome message for group {}", groupId, e);
            throw new JobExecutionException(e);
        }
    }
}
