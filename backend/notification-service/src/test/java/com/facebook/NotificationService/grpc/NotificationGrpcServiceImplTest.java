package com.facebook.NotificationService.grpc;

import com.facebook.NotificationService.model.Notification;
import com.facebook.NotificationService.service.NotificationService;
import com.facebook.notification.grpc.*;
import io.grpc.stub.StreamObserver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationGrpcServiceImplTest {

    @Mock
    private NotificationService notificationService;

    @Mock
    private StreamObserver<GetHistoryResponse> getHistoryResponseObserver;

    @Mock
    private StreamObserver<MarkAsReadResponse> markAsReadResponseObserver;

    @Mock
    private StreamObserver<SendNotificationResponse> sendNotificationResponseObserver;

    private NotificationGrpcServiceImpl notificationGrpcService;

    @BeforeEach
    void setUp() {
        notificationGrpcService = new NotificationGrpcServiceImpl(notificationService);
    }

    @Test
    void testGetHistory_Success() {
        String userId = "user-123";
        GetHistoryRequest request = GetHistoryRequest.newBuilder().setUserId(userId).build();

        Notification n = Notification.builder()
                .id(1L)
                .userId(userId)
                .title("Title")
                .message("Message")
                .isRead(false)
                .build();

        when(notificationService.getNotificationsForUser(userId)).thenReturn(List.of(n));

        notificationGrpcService.getHistory(request, getHistoryResponseObserver);

        verify(getHistoryResponseObserver).onNext(any(GetHistoryResponse.class));
        verify(getHistoryResponseObserver).onCompleted();
        verify(getHistoryResponseObserver, never()).onError(any());
    }

    @Test
    void testGetHistory_ExceptionLogsAndReturnsError() {
        String userId = "user-123";
        GetHistoryRequest request = GetHistoryRequest.newBuilder().setUserId(userId).build();

        when(notificationService.getNotificationsForUser(userId)).thenThrow(new RuntimeException("Database error"));

        notificationGrpcService.getHistory(request, getHistoryResponseObserver);

        verify(getHistoryResponseObserver).onError(any(io.grpc.StatusRuntimeException.class));
        verify(getHistoryResponseObserver, never()).onNext(any());
    }

    @Test
    void testMarkAsRead_Success() {
        MarkAsReadRequest request = MarkAsReadRequest.newBuilder().setId(1L).build();

        notificationGrpcService.markAsRead(request, markAsReadResponseObserver);

        verify(notificationService).markAsRead(1L);
        verify(markAsReadResponseObserver).onNext(any(MarkAsReadResponse.class));
        verify(markAsReadResponseObserver).onCompleted();
    }

    @Test
    void testSendNotification_Success() {
        SendNotificationRequest request = SendNotificationRequest.newBuilder()
                .setUserId("user-123")
                .setTitle("Title")
                .setMessage("Message")
                .setTargetId("target-456")
                .build();

        Notification n = Notification.builder()
                .id(1L)
                .userId("user-123")
                .title("Title")
                .message("Message")
                .isRead(false)
                .targetId("target-456")
                .build();

        when(notificationService.createAndSendNotification("user-123", "Title", "Message", "target-456")).thenReturn(n);

        notificationGrpcService.sendNotification(request, sendNotificationResponseObserver);

        verify(sendNotificationResponseObserver).onNext(any(SendNotificationResponse.class));
        verify(sendNotificationResponseObserver).onCompleted();
    }
}
