package com.facebook.NotificationService.service;

import com.facebook.NotificationService.model.Notification;
import com.facebook.NotificationService.model.WebPushSubscription;
import com.facebook.NotificationService.repository.NotificationRepository;
import com.facebook.NotificationService.repository.WebPushSubscriptionRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.listener.ChannelTopic;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceUnitTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private WebPushSubscriptionRepository webPushSubscriptionRepository;

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @Mock
    private ChannelTopic topic;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private MqttNotificationPublisher mqttNotificationPublisher;

    @Mock
    private WebPushService webPushService;

    private NotificationService notificationService;

    @BeforeEach
    void setUp() {
        notificationService = new NotificationService(
                notificationRepository,
                webPushSubscriptionRepository,
                redisTemplate,
                topic,
                objectMapper,
                mqttNotificationPublisher,
                webPushService
        );
    }

    @Test
    void testCreateAndSendNotification_NewNotification() throws JsonProcessingException {
        String userId = "user-123";
        String title = "New Friend Request";
        String message = "John Doe added you";
        String targetId = "target-456";

        Notification savedNotification = Notification.builder()
                .id(1L)
                .userId(userId)
                .title(title)
                .message(message)
                .isRead(false)
                .targetId(targetId)
                .build();

        when(notificationRepository.findByUserIdAndTitleAndTargetId(userId, title, targetId)).thenReturn(Optional.empty());
        when(notificationRepository.save(any(Notification.class))).thenReturn(savedNotification);
        when(topic.getTopic()).thenReturn("notifications-channel");
        when(objectMapper.writeValueAsString(savedNotification)).thenReturn("json-payload");

        Notification result = notificationService.createAndSendNotification(userId, title, message, targetId);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(notificationRepository).save(any(Notification.class));
        verify(redisTemplate).convertAndSend("notifications-channel", "json-payload");
    }

    @Test
    void testCreateAndSendNotification_UpdateExisting() throws JsonProcessingException {
        String userId = "user-123";
        String title = "Message";
        String message = "Updated message text";
        String targetId = "target-456";

        Notification existingNotification = Notification.builder()
                .id(2L)
                .userId(userId)
                .title(title)
                .message("Old message text")
                .isRead(true)
                .targetId(targetId)
                .build();

        when(notificationRepository.findByUserIdAndTitleAndTargetId(userId, title, targetId)).thenReturn(Optional.of(existingNotification));
        when(notificationRepository.save(existingNotification)).thenReturn(existingNotification);
        when(topic.getTopic()).thenReturn("notifications-channel");
        when(objectMapper.writeValueAsString(existingNotification)).thenReturn("json-payload");

        Notification result = notificationService.createAndSendNotification(userId, title, message, targetId);

        assertNotNull(result);
        assertEquals(2L, result.getId());
        assertEquals(message, result.getMessage());
        assertFalse(result.isRead());
        verify(notificationRepository).save(existingNotification);
    }

    @Test
    void testRegisterSubscription() {
        String userId = "user-123";
        String endpoint = "https://fcm.googleapis.com/fcm/send/test";
        String p256dh = "key-p256dh";
        String auth = "key-auth";

        when(webPushSubscriptionRepository.findByEndpoint(endpoint)).thenReturn(Optional.empty());
        when(webPushSubscriptionRepository.save(any(WebPushSubscription.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        WebPushSubscription sub = notificationService.registerSubscription(userId, endpoint, p256dh, auth);

        assertNotNull(sub);
        assertEquals(userId, sub.getUserId());
        assertEquals(endpoint, sub.getEndpoint());
        verify(webPushSubscriptionRepository).save(any(WebPushSubscription.class));
    }

    @Test
    void testUnsubscribe() {
        String endpoint = "https://fcm.googleapis.com/fcm/send/test";
        notificationService.unsubscribe(endpoint);
        verify(webPushSubscriptionRepository).deleteByEndpoint(endpoint);
    }

    @Test
    void testGetNotificationsForUser() {
        String userId = "user-123";
        when(notificationRepository.findByUserIdOrderByCreatedAtDesc(userId)).thenReturn(List.of());
        List<Notification> list = notificationService.getNotificationsForUser(userId);
        assertNotNull(list);
        verify(notificationRepository).findByUserIdOrderByCreatedAtDesc(userId);
    }
}
