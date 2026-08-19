package com.facebook.NotificationService.redis;

import com.facebook.NotificationService.model.Notification;
import com.facebook.NotificationService.service.NotificationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
@Slf4j
public class NotificationSubscriber implements MessageListener {

    private final NotificationService notificationService;
    private final ObjectMapper objectMapper;

    // Using @Lazy injection to completely prevent any potential circular dependencies
    public NotificationSubscriber(@Lazy NotificationService notificationService, ObjectMapper objectMapper) {
        this.notificationService = notificationService;
        this.objectMapper = objectMapper;
    }

    @Override
    public void onMessage(Message message, byte[] pattern) {
        String body = new String(message.getBody(), StandardCharsets.UTF_8);
        log.info("Received notification payload from Redis Pub/Sub: {}", body);
        try {
            Notification notification = objectMapper.readValue(body, Notification.class);
            notificationService.dispatchNotification(notification);
        } catch (IOException e) {
            log.error("Failed to parse Redis message to Notification class instance", e);
        }
    }
}
