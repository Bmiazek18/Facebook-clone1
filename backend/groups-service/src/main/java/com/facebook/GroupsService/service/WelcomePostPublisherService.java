package com.facebook.GroupsService.service;

import com.facebook.feed.grpc.CreatePostRequest;
import com.facebook.feed.grpc.FeedGrpcServiceGrpc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;

@Service
public class WelcomePostPublisherService {

    private static final Logger log = LoggerFactory.getLogger(WelcomePostPublisherService.class);

    @GrpcClient("feed-service")
    private FeedGrpcServiceGrpc.FeedGrpcServiceBlockingStub feedGrpcStub;

    /**
     * Publishes a welcome post via gRPC to feed-service.
     */
    public void publishWelcomePost(String groupId, String welcomeMessage, String creatorId) {
        log.info("WelcomePostPublisherService: Publishing welcome post to group {}", groupId);
        try {
            if (feedGrpcStub != null) {
                CreatePostRequest postRequest = CreatePostRequest.newBuilder()
                        .setContent(welcomeMessage)
                        .setAuthorId(creatorId != null ? creatorId : "system")
                        .setTargetId(groupId)
                        .setTargetType("Group")
                        .setVisibility("PUBLIC")
                        .build();

                feedGrpcStub.createPost(postRequest);
                log.info("WelcomePostPublisherService: Successfully posted welcome message to group {}", groupId);
            } else {
                log.warn("WelcomePostPublisherService: feedGrpcStub is null, cannot publish");
            }
        } catch (Exception e) {
            log.error("WelcomePostPublisherService: Error posting welcome message to group {}", groupId, e);
            throw new RuntimeException("Feed Service gRPC call failed", e);
        }
    }
}
