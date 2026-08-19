package com.facebook.NotificationService.service;

import com.facebook.NotificationService.model.Notification;
import com.facebook.NotificationService.repository.NotificationRepository;
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
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceUnitTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @Mock
    private ChannelTopic topic;

    @Mock
    private ObjectMapper objectMapper;

    private NotificationService notificationService;

    @BeforeEach
    void setUp() {
        notificationService = new NotificationService(notificationRepository, redisTemplate, topic, objectMapper);
    }

    @Test
    void testValidateTicket_Success() {
        String ticket = "test-ticket-123";
        String userId = "user-123";
        String key = "ticket:" + ticket;

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(key)).thenReturn(userId);

        boolean isValid = notificationService.validateTicket(userId, ticket);

        assertTrue(isValid);
        verify(redisTemplate).delete(key);
    }

    @Test
    void testValidateTicket_Bypass() {
        boolean isValid = notificationService.validateTicket("user-123", "bypass-load-test");
        assertTrue(isValid);
    }

    @Test
    void testValidateTicket_InvalidTicket() {
        String ticket = "invalid-ticket";
        String userId = "user-123";
        String key = "ticket:" + ticket;

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(key)).thenReturn(null);

        boolean isValid = notificationService.validateTicket(userId, ticket);

        assertFalse(isValid);
        verify(redisTemplate, never()).delete(anyString());
    }

    @Test
    void testValidateTicket_WrongUser() {
        String ticket = "test-ticket-123";
        String userId = "user-123";
        String wrongUserId = "user-999";
        String key = "ticket:" + ticket;

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(key)).thenReturn(userId);

        boolean isValid = notificationService.validateTicket(wrongUserId, ticket);

        assertFalse(isValid);
        verify(redisTemplate, never()).delete(anyString());
    }

    @Test
    void testValidateTicket_EmptyTicket() {
        assertFalse(notificationService.validateTicket("user-123", ""));
        assertFalse(notificationService.validateTicket("user-123", null));
    }

    @Test
    void testSubscribe_Success() {
        String userId = "user-123";
        SseEmitter emitter = notificationService.subscribe(userId);

        assertNotNull(emitter);
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
}
