package com.facebook.NotificationService.grpc;

import com.facebook.NotificationService.model.Notification;
import com.facebook.NotificationService.service.NotificationService;
import com.facebook.notification.grpc.*;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;

@GrpcService
@RequiredArgsConstructor
@Slf4j
public class NotificationGrpcServiceImpl extends NotificationGrpcServiceGrpc.NotificationGrpcServiceImplBase {

    private final NotificationService notificationService;

    @Override
    public void getHistory(GetHistoryRequest request, StreamObserver<GetHistoryResponse> responseObserver) {
        try {
            GetHistoryResponse.Builder builder = GetHistoryResponse.newBuilder();
            for (Notification notification : notificationService.getNotificationsForUser(request.getUserId())) {
                builder.addNotifications(toProto(notification));
            }
            responseObserver.onNext(builder.build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("Failed to fetch notifications for user: {}", request.getUserId(), e);
            responseObserver.onError(io.grpc.Status.INTERNAL
                    .withDescription("Failed to fetch notifications: " + e.getMessage())
                    .asRuntimeException());
        }
    }

    @Override
    public void markAsRead(MarkAsReadRequest request, StreamObserver<MarkAsReadResponse> responseObserver) {
        try {
            notificationService.markAsRead(request.getId());
            responseObserver.onNext(MarkAsReadResponse.newBuilder().setSuccess(true).build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("Failed to mark notification as read for ID: {}", request.getId(), e);
            responseObserver.onError(io.grpc.Status.INTERNAL
                    .withDescription("Failed to mark notification as read: " + e.getMessage())
                    .asRuntimeException());
        }
    }

    @Override
    public void sendNotification(SendNotificationRequest request, StreamObserver<SendNotificationResponse> responseObserver) {
        try {
            Notification saved = notificationService.createAndSendNotification(
                    request.getUserId(),
                    request.getTitle(),
                    request.getMessage(),
                    request.getTargetId().isEmpty() ? null : request.getTargetId()
            );
            responseObserver.onNext(SendNotificationResponse.newBuilder()
                    .setNotification(toProto(saved))
                    .build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("Failed to send notification to user: {} with title: {}", request.getUserId(), request.getTitle(), e);
            responseObserver.onError(io.grpc.Status.INTERNAL
                    .withDescription("Failed to send notification: " + e.getMessage())
                    .asRuntimeException());
        }
    }

    @Override
    public void deleteNotification(DeleteNotificationRequest request, StreamObserver<DeleteNotificationResponse> responseObserver) {
        try {
            notificationService.deleteNotification(request.getUserId(), request.getTitle(), request.getTargetId());
            responseObserver.onNext(DeleteNotificationResponse.newBuilder().setSuccess(true).build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("Failed to delete notification for user: {} with title: {}", request.getUserId(), request.getTitle(), e);
            responseObserver.onError(io.grpc.Status.INTERNAL
                    .withDescription("Failed to delete notification: " + e.getMessage())
                    .asRuntimeException());
        }
    }

    private com.facebook.notification.grpc.NotificationDto toProto(Notification notification) {
        com.facebook.notification.grpc.NotificationDto.Builder builder =
                com.facebook.notification.grpc.NotificationDto.newBuilder()
                        .setId(notification.getId())
                        .setUserId(notification.getUserId())
                        .setTitle(notification.getTitle())
                        .setMessage(notification.getMessage())
                        .setRead(notification.isRead());

        if (notification.getCreatedAt() != null) {
            builder.setCreatedAt(notification.getCreatedAt().toString());
        }
        if (notification.getTargetId() != null) {
            builder.setTargetId(notification.getTargetId());
        }
        return builder.build();
    }
}
