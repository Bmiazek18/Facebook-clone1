package com.facebook.FeedService.util;

import com.facebook.notification.grpc.SendNotificationRequest;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@Slf4j
public class MentionHelper {

    @GrpcClient("user-service")
    private com.facebook.user.grpc.UserGrpcServiceGrpc.UserGrpcServiceBlockingStub userGrpcStub;

    @GrpcClient("notification-service")
    private com.facebook.notification.grpc.NotificationGrpcServiceGrpc.NotificationGrpcServiceBlockingStub notificationGrpcStub;

    public List<String> extractMentionedUserIds(String content) {
        List<String> mentionedIds = new ArrayList<>();
        if (content == null || content.isEmpty()) {
            return mentionedIds;
        }
        Pattern pattern = Pattern.compile("\\[@([a-zA-Z0-9-]+)\\]");
        Matcher matcher = pattern.matcher(content);
        while (matcher.find()) {
            String id = matcher.group(1);
            if (!mentionedIds.contains(id)) {
                mentionedIds.add(id);
            }
        }
        return mentionedIds;
    }

    public void sendMentionNotifications(List<String> targetUserIds, String authorId, String contextType) {
        if (targetUserIds == null || targetUserIds.isEmpty()) {
            return;
        }

        String authorName = "Ktoś";
        try {
            if (userGrpcStub != null) {
                com.facebook.user.grpc.GetUserByIdResponse userRes = userGrpcStub.getUserById(
                        com.facebook.user.grpc.GetUserByIdRequest.newBuilder().setUserId(authorId).build()
                );
                var u = userRes.getUser();
                authorName = u.getFirstName() + " " + u.getLastName();
            }
        } catch (Exception e) {
            log.error("Failed to fetch author name from user-service for mention notification: {}", authorId, e);
        }

        for (String targetId : targetUserIds) {
            if (targetId.equals(authorId)) {
                continue;
            }
            try {
                notificationGrpcStub.sendNotification(SendNotificationRequest.newBuilder()
                        .setUserId(targetId)
                        .setTitle("Mention")
                        .setMessage(authorId)
                        .build());
                log.info("Sent mention notification to user {} from {}", targetId, authorName);
            } catch (Exception e) {
                log.error("Failed to send mention notification to user {}", targetId, e);
            }
        }
    }
}
