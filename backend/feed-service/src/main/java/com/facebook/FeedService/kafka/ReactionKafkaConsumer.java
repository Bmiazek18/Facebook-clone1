package com.facebook.FeedService.kafka;

import com.facebook.FeedService.dto.ReactionEvent;
import com.facebook.FeedService.entity.ReactionEntity;
import com.facebook.FeedService.repository.ReactionRepository;
import com.facebook.FeedService.repository.PostRepository;
import com.facebook.notification.grpc.DeleteNotificationRequest;
import com.facebook.notification.grpc.SendNotificationRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReactionKafkaConsumer {

    private final ReactionRepository reactionRepository;
    private final PostRepository postRepository;

    @GrpcClient("user-service")
    private com.facebook.user.grpc.UserGrpcServiceGrpc.UserGrpcServiceBlockingStub userGrpcStub;

    @GrpcClient("notification-service")
    private com.facebook.notification.grpc.NotificationGrpcServiceGrpc.NotificationGrpcServiceBlockingStub notificationGrpcStub;

    @KafkaListener(
            topics = "reactions-topic",
            groupId = "db-sync-worker-group", // Dedicated consumer group for Database Sync
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consume(List<ConsumerRecord<String, ReactionEvent>> records, Acknowledgment ack) {
        log.info("Received batch of {} reaction events from Kafka", records.size());
        
        try {
            java.util.List<ReactionEntity> entitiesToUpsert = new java.util.ArrayList<>();
            java.util.List<ReactionEntity> entitiesToDelete = new java.util.ArrayList<>();

            for (var record : records) {
                ReactionEvent event = record.value();
                if (event.getReactionType() == null || event.getReactionType().isEmpty() || "null".equalsIgnoreCase(event.getReactionType())) {
                    entitiesToDelete.add(ReactionEntity.builder()
                            .userId(event.getUserId())
                            .postId(event.getPostId())
                            .build());
                } else {
                    entitiesToUpsert.add(ReactionEntity.builder()
                            .userId(event.getUserId())
                            .postId(event.getPostId())
                            .reactionType(event.getReactionType())
                            .createdAt(Instant.ofEpochMilli(event.getTimestamp()))
                            .build());
                }
            }

            if (!entitiesToUpsert.isEmpty()) {
                reactionRepository.batchUpsert(entitiesToUpsert);
            }

            if (!entitiesToDelete.isEmpty()) {
                for (ReactionEntity ent : entitiesToDelete) {
                    reactionRepository.findByUserIdAndPostId(ent.getUserId(), ent.getPostId())
                            .ifPresent(reactionRepository::delete);
                }
            }
            
            // Commit offsets manually only after successful database persistence to prevent data loss
            ack.acknowledge();
            log.info("Successfully persisted batch of {} reaction events and committed offsets", records.size());

            // Process grouped notifications for affected posts
            try {
                java.util.Set<String> affectedPostIds = new java.util.HashSet<>();
                for (var record : records) {
                    affectedPostIds.add(record.value().getPostId());
                }

                for (String postId : affectedPostIds) {
                    postRepository.findById(postId).ifPresent(post -> {
                        String authorId = post.getAuthorId();
                        List<ReactionEntity> reactions = reactionRepository.findByPostId(postId);
                        List<String> likerIds = reactions.stream()
                                .map(ReactionEntity::getUserId)
                                .filter(uid -> !uid.equals(authorId))
                                .collect(Collectors.toList());
                        updateNotification(authorId, postId, likerIds);
                    });
                }
            } catch (Exception ex) {
                log.error("Failed to update reaction notifications", ex);
            }

        } catch (Exception e) {
            log.error("Failed to process batch of reaction events. Kafka offset will not be committed.", e);
            // In a production system, we would write to a Dead Letter Queue (DLQ) or retry with backoff.
            throw e; 
        }
    }

    private void updateNotification(String authorId, String postId, List<String> userIds) {
        if (userIds.isEmpty()) {
            try {
                notificationGrpcStub.deleteNotification(DeleteNotificationRequest.newBuilder()
                        .setUserId(authorId)
                        .setTitle("Polubienie")
                        .setTargetId(postId)
                        .build());
                log.info("Sent delete notification request for post: {}", postId);
            } catch (Exception e) {
                log.error("Failed to send delete notification request for post {}", postId, e);
            }
        } else {
            try {
                String message = formatReactionsMessage(userIds, authorId);
                notificationGrpcStub.sendNotification(SendNotificationRequest.newBuilder()
                        .setUserId(authorId)
                        .setTitle("Polubienie")
                        .setTargetId(postId)
                        .setMessage(message)
                        .build());
                log.info("Sent reaction notification request for post: {}, message: {}", postId, message);
            } catch (Exception e) {
                log.error("Failed to send reaction notification request for post {}", postId, e);
            }
        }
    }

    private String formatReactionsMessage(List<String> userIds, String authorId) {
        List<String> names = new ArrayList<>();
        // Fetch up to 2 names to display
        for (int i = 0; i < Math.min(2, userIds.size()); i++) {
            names.add(getUserName(userIds.get(i)));
        }

        int totalCount = userIds.size();
        if (totalCount == 1) {
            return names.get(0) + " polubił Twój post.";
        } else if (totalCount == 2) {
            return names.get(0) + " i " + names.get(1) + " polubili Twój post.";
        } else {
            int otherCount = totalCount - 2;
            String suffix;
            if (otherCount == 1) {
                suffix = "1 inna osoba";
            } else {
                int lastDigit = otherCount % 10;
                int lastTwoDigits = otherCount % 100;
                if (lastDigit >= 2 && lastDigit <= 4 && (lastTwoDigits < 12 || lastTwoDigits > 14)) {
                    suffix = otherCount + " inne osoby";
                } else {
                    suffix = otherCount + " innych osób";
                }
            }
            return names.get(0) + ", " + names.get(1) + " i " + suffix + " polubili Twój post.";
        }
    }

    private String getUserName(String userId) {
        try {
            if (userGrpcStub != null) {
                com.facebook.user.grpc.GetUserByIdResponse userRes = userGrpcStub.getUserById(
                        com.facebook.user.grpc.GetUserByIdRequest.newBuilder().setUserId(userId).build()
                );
                var u = userRes.getUser();
                return u.getFirstName() + " " + u.getLastName();
            }
        } catch (Exception e) {
            log.error("Failed to fetch user name via gRPC for notification: {}", userId, e);
        }
        return "Ktoś";
    }
}
