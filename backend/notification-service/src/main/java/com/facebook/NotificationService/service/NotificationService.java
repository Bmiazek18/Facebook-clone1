package com.facebook.NotificationService.service;

import com.facebook.NotificationService.model.Notification;
import com.facebook.NotificationService.model.WebPushSubscription;
import com.facebook.NotificationService.repository.NotificationRepository;
import com.facebook.NotificationService.repository.WebPushSubscriptionRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final WebPushSubscriptionRepository webPushSubscriptionRepository;
    private final RedisTemplate<String, String> redisTemplate;
    private final ChannelTopic topic;
    private final ObjectMapper objectMapper;
    private final MqttNotificationPublisher mqttNotificationPublisher;
    private final WebPushService webPushService;
    private final java.util.concurrent.ScheduledExecutorService scheduler = 
        java.util.concurrent.Executors.newScheduledThreadPool(2);

    public NotificationService(NotificationRepository notificationRepository,
                               WebPushSubscriptionRepository webPushSubscriptionRepository,
                               RedisTemplate<String, String> redisTemplate,
                               ChannelTopic topic,
                               ObjectMapper objectMapper,
                               MqttNotificationPublisher mqttNotificationPublisher,
                               WebPushService webPushService) {
        this.notificationRepository = notificationRepository;
        this.webPushSubscriptionRepository = webPushSubscriptionRepository;
        this.redisTemplate = redisTemplate;
        this.topic = topic;
        this.objectMapper = objectMapper;
        this.mqttNotificationPublisher = mqttNotificationPublisher;
        this.webPushService = webPushService;
    }

    /**
     * Creates and saves a notification in Postgres, then publishes it to Redis.
     */
    public Notification createAndSendNotification(String userId, String title, String message, String targetId) {
        Notification notification;
        if (targetId != null && !targetId.isEmpty()) {
            Optional<Notification> existingOpt = notificationRepository.findByUserIdAndTitleAndTargetId(userId, title, targetId);
            if (existingOpt.isPresent()) {
                notification = existingOpt.get();
                notification.setMessage(message);
                notification.setCreatedAt(LocalDateTime.now());
                notification.setRead(false);
                log.info("Updating existing notification {} for user {} with message: {}", notification.getId(), userId, message);
            } else {
                notification = Notification.builder()
                        .userId(userId)
                        .title(title)
                        .message(message)
                        .isRead(false)
                        .createdAt(LocalDateTime.now())
                        .targetId(targetId)
                        .build();
            }
        } else {
            notification = Notification.builder()
                    .userId(userId)
                    .title(title)
                    .message(message)
                    .isRead(false)
                    .createdAt(LocalDateTime.now())
                    .build();
        }

        Notification saved = notificationRepository.save(notification);
        log.info("Saved notification {} to database for user {}", saved.getId(), userId);

        // Check if this is a reaction notification that needs to be debounced
        if ("Polubienie".equalsIgnoreCase(title) && targetId != null && !targetId.isEmpty()) {
            debounceAndPublish(saved);
        } else {
            // Direct immediate publish for other notification types
            publishToRedis(saved);
        }

        return saved;
    }

    private void debounceAndPublish(Notification notification) {
        String userId = notification.getUserId();
        String targetId = notification.getTargetId();
        String debounceKey = "debounce:reaction:" + userId + ":" + targetId;

        // Try to check if a debouncer is already active for this post reaction
        Boolean alreadyScheduled = redisTemplate.hasKey(debounceKey);
        
        if (Boolean.TRUE.equals(alreadyScheduled)) {
            // A debouncer is already running. Just update the latest message in Redis
            redisTemplate.opsForValue().set(debounceKey + ":last", notification.getMessage());
            log.info("Debounce active for user {} on post {}. Skipping duplicate immediate push.", userId, targetId);
            return;
        }

        // Set the active debounce flag for 15 seconds
        redisTemplate.opsForValue().set(debounceKey, "active", java.time.Duration.ofSeconds(15));
        redisTemplate.opsForValue().set(debounceKey + ":last", notification.getMessage());

        // Send the first notification immediately (leading edge)
        publishToRedis(notification);

        // Schedule the trailing edge check in 15 seconds
        scheduler.schedule(() -> {
            try {
                // Fetch the latest state from the database
                Optional<Notification> latestOpt = notificationRepository.findById(notification.getId());
                if (latestOpt.isPresent()) {
                    Notification latest = latestOpt.get();
                    String lastPublishedMsg = notification.getMessage();
                    String currentMsg = latest.getMessage();
                    
                    // If the message has changed (new likes arrived during the 15 seconds)
                    if (!currentMsg.equals(lastPublishedMsg)) {
                        log.info("Trailing edge: Sending updated reaction notification for user {} on post {}", userId, targetId);
                        publishToRedis(latest);
                    }
                }
            } catch (Exception e) {
                log.error("Error during debounced notification trailing edge execution", e);
            } finally {
                // Clear the scheduled marker so new reactions can trigger another debounce window
                redisTemplate.delete(debounceKey);
                redisTemplate.delete(debounceKey + ":last");
            }
        }, 15, java.util.concurrent.TimeUnit.SECONDS);
    }

    private void publishToRedis(Notification notification) {
        try {
            String jsonPayload = objectMapper.writeValueAsString(notification);
            redisTemplate.convertAndSend(topic.getTopic(), jsonPayload);
            log.info("Published notification {} to Redis topic {}", notification.getId(), topic.getTopic());
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize notification to JSON for Redis", e);
        }
    }

    /**
     * Dispatches notification received from Redis Pub/Sub to:
     * 1. MQTT broker for real-time delivery to the browser
     * 2. Web Push subscriptions (if any)
     */
    public void dispatchNotification(Notification notification) {
        String userId = notification.getUserId();
        try {
            String payload = objectMapper.writeValueAsString(notification);
            
            // 1. Deliver via MQTT (WebSocket) in real-time
            mqttNotificationPublisher.publish(userId, payload);

            // 2. Deliver via Web Push to registered devices
            List<WebPushSubscription> subscriptions = webPushSubscriptionRepository.findByUserId(userId);
            if (subscriptions != null && !subscriptions.isEmpty()) {
                log.info("Sending Web Push notification to {} devices of user {}", subscriptions.size(), userId);
                for (WebPushSubscription sub : subscriptions) {
                    webPushService.sendPush(sub, payload);
                }
            } else {
                log.debug("No Web Push subscriptions registered for user {}", userId);
            }
        } catch (Exception e) {
            log.error("Error during notification dispatch for user {}: {}", userId, e.getMessage(), e);
        }
    }

    /**
     * Registers or updates a browser Web Push subscription.
     */
    public WebPushSubscription registerSubscription(String userId, String endpoint, String p256dh, String auth) {
        Optional<WebPushSubscription> existing = webPushSubscriptionRepository.findByEndpoint(endpoint);
        WebPushSubscription sub;
        if (existing.isPresent()) {
            sub = existing.get();
            sub.setUserId(userId);
            sub.setP256dh(p256dh);
            sub.setAuth(auth);
        } else {
            sub = WebPushSubscription.builder()
                    .userId(userId)
                    .endpoint(endpoint)
                    .p256dh(p256dh)
                    .auth(auth)
                    .createdAt(LocalDateTime.now())
                    .build();
        }
        return webPushSubscriptionRepository.save(sub);
    }

    /**
     * Deletes a Web Push subscription.
     */
    @org.springframework.transaction.annotation.Transactional
    public void unsubscribe(String endpoint) {
        webPushSubscriptionRepository.deleteByEndpoint(endpoint);
        log.info("Deleted Web Push subscription for endpoint: {}", endpoint);
    }

    @org.springframework.transaction.annotation.Transactional
    public void deleteNotification(String userId, String title, String targetId) {
        if (targetId != null && !targetId.isEmpty()) {
            notificationRepository.deleteByUserIdAndTitleAndTargetId(userId, title, targetId);
            log.info("Deleted notification with title: {} and targetId: {} for user: {}", title, targetId, userId);
        }
    }

    public List<Notification> getNotificationsForUser(String userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public Notification markAsRead(Long notificationId) {
        return notificationRepository.findById(notificationId)
                .map(notification -> {
                    notification.setRead(true);
                    Notification saved = notificationRepository.save(notification);
                    
                    // Publish read sync event to MQTT so all active sessions update
                    try {
                        String payload = objectMapper.writeValueAsString(java.util.Map.of(
                            "type", "NOTIFICATION_READ",
                            "notificationId", saved.getId(),
                            "userId", saved.getUserId()
                        ));
                        mqttNotificationPublisher.publish(saved.getUserId(), payload);
                        log.info("Published notification read sync event for notificationId: {}", saved.getId());
                    } catch (Exception e) {
                        log.error("Failed to publish read sync event to MQTT: {}", e.getMessage());
                    }
                    
                    return saved;
                })
                .orElseThrow(() -> new IllegalArgumentException("Notification with id " + notificationId + " not found"));
    }
}
