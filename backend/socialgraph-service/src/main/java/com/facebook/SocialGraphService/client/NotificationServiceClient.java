package com.facebook.SocialGraphService.client;

import com.facebook.notification.grpc.NotificationGrpcServiceGrpc;
import com.facebook.notification.grpc.SendNotificationRequest;
import com.facebook.notification.grpc.SendNotificationResponse;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Component;

@Component
public class NotificationServiceClient {

    @GrpcClient("notification-service")
    private NotificationGrpcServiceGrpc.NotificationGrpcServiceBlockingStub notificationGrpcStub;

    @CircuitBreaker(name = "notificationService", fallbackMethod = "sendNotificationFallback")
    @Retry(name = "notificationService")
    public SendNotificationResponse sendNotification(String targetUserId, String title, String message) {
        return notificationGrpcStub.sendNotification(SendNotificationRequest.newBuilder()
                .setUserId(targetUserId)
                .setTitle(title)
                .setMessage(message)
                .build());
    }

    public SendNotificationResponse sendNotificationFallback(String targetUserId, String title, String message, Throwable throwable) {
        System.err.println("Warning: socialgraph-service NotificationServiceClient circuit breaker triggered! Reason: " + throwable.getMessage());
        return SendNotificationResponse.newBuilder().build();
    }
}
