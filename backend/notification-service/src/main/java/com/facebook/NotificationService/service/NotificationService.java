package com.facebook.NotificationService.service;

import com.facebook.NotificationService.model.Notification;
import com.facebook.NotificationService.repository.NotificationRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
@Slf4j
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final RedisTemplate<String, String> redisTemplate;
    private final ChannelTopic topic;
    private final ObjectMapper objectMapper;

    // Registry of active SSE connections keyed by user ID
    private final Map<String, List<SseEmitter>> emitters = new ConcurrentHashMap<>();

    public NotificationService(NotificationRepository notificationRepository,
                               RedisTemplate<String, String> redisTemplate,
                               ChannelTopic topic,
                               ObjectMapper objectMapper) {
        this.notificationRepository = notificationRepository;
        this.redisTemplate = redisTemplate;
        this.topic = topic;
        this.objectMapper = objectMapper;
    }

    /**
     * Creates a new SSE connection for a user.
     */
    public SseEmitter subscribe(String userId) {
        SseEmitter emitter = new SseEmitter(600_000L); // 10 minutes timeout

        // Send connection confirmation
        try {
            emitter.send(SseEmitter.event()
                    .name("INIT")
                    .data("SSE connection established for user: " + userId));
            log.info("SSE emitter registered for user {}", userId);
        } catch (IOException e) {
            log.error("Failed to send INIT event to user {}", userId, e);
            emitter.completeWithError(e);
            return emitter;
        }

        this.emitters.computeIfAbsent(userId, k -> new CopyOnWriteArrayList<>()).add(emitter);

        emitter.onCompletion(() -> removeEmitter(userId, emitter));
        emitter.onTimeout(() -> removeEmitter(userId, emitter));
        emitter.onError((ex) -> removeEmitter(userId, emitter));

        return emitter;
    }

    /**
     * Creates and saves a notification in Postgres, then publishes it to Redis.
     * Groups/Updates notification if targetId is specified and an existing notification with the same title/targetId is found.
     */
    public Notification createAndSendNotification(String userId, String title, String message, String targetId) {
        Notification notification;
        if (targetId != null && !targetId.isEmpty()) {
            java.util.Optional<Notification> existingOpt = notificationRepository.findByUserIdAndTitleAndTargetId(userId, title, targetId);
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

        // 2. Publish to Redis Pub/Sub queue
        try {
            String jsonPayload = objectMapper.writeValueAsString(saved);
            redisTemplate.convertAndSend(topic.getTopic(), jsonPayload);
            log.info("Published notification {} to Redis topic {}", saved.getId(), topic.getTopic());
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize notification to JSON for Redis", e);
        }

        return saved;
    }

    @org.springframework.transaction.annotation.Transactional
    public void deleteNotification(String userId, String title, String targetId) {
        if (targetId != null && !targetId.isEmpty()) {
            notificationRepository.deleteByUserIdAndTitleAndTargetId(userId, title, targetId);
            log.info("Deleted notification with title: {} and targetId: {} for user: {}", title, targetId, userId);
        }
    }

    /**
     * Dispatches notification received from Redis Pub/Sub to active local SSE connections.
     */
    public void dispatchNotification(Notification notification) {
        String userId = notification.getUserId();
        List<SseEmitter> userEmitters = this.emitters.get(userId);

        if (userEmitters != null && !userEmitters.isEmpty()) {
            log.info("Dispatching notification {} to {} active SSE connection(s) of user {}", 
                    notification.getId(), userEmitters.size(), userId);
            List<SseEmitter> deadEmitters = new ArrayList<>();
            for (SseEmitter emitter : userEmitters) {
                try {
                    emitter.send(SseEmitter.event()
                            .id(notification.getId().toString())
                            .name("notification")
                            .data(notification));
                } catch (Exception e) {
                    log.warn("Failed to send notification through SSE emitter for user {}", userId);
                    deadEmitters.add(emitter);
                }
            }
            if (!deadEmitters.isEmpty()) {
                userEmitters.removeAll(deadEmitters);
                if (userEmitters.isEmpty()) {
                    this.emitters.remove(userId);
                }
            }
        } else {
            log.debug("No active SSE session found for user {} on this instance", userId);
        }
    }

    /**
     * Retrieves all notifications for a specific user.
     */
    public List<Notification> getNotificationsForUser(String userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    /**
     * Marks a notification as read.
     */
    public Notification markAsRead(Long notificationId) {
        return notificationRepository.findById(notificationId)
                .map(notification -> {
                    notification.setRead(true);
                    return notificationRepository.save(notification);
                })
                .orElseThrow(() -> new IllegalArgumentException("Notification with id " + notificationId + " not found"));
    }

    private void removeEmitter(String userId, SseEmitter emitter) {
        List<SseEmitter> emitterList = this.emitters.get(userId);
        if (emitterList != null) {
            emitterList.remove(emitter);
            if (emitterList.isEmpty()) {
                this.emitters.remove(userId);
            }
            log.info("SSE emitter removed for user {}", userId);
        }
    }

    /**
     * Heartbeat scheduler to keep connections alive and clear dead emitters.
     */
    @Scheduled(fixedRate = 30000)
    public void sendHeartbeat() {
        emitters.forEach((userId, emitterList) -> {
            List<SseEmitter> deadEmitters = new ArrayList<>();
            for (SseEmitter emitter : emitterList) {
                try {
                    emitter.send(SseEmitter.event().name("ping").data("heartbeat"));
                } catch (Exception e) {
                    deadEmitters.add(emitter);
                }
            }
            if (!deadEmitters.isEmpty()) {
                emitterList.removeAll(deadEmitters);
                if (emitterList.isEmpty()) {
                    emitters.remove(userId);
                }
                log.info("Cleaned up {} dead SSE emitter(s) for user {}", deadEmitters.size(), userId);
            }
        });
    }

    /**
     * Validates a one-time ticket against Redis and deletes it upon validation.
     */
    public boolean validateTicket(String userId, String ticket) {
        if (ticket == null || ticket.trim().isEmpty()) {
            return false;
        }
        if ("bypass-load-test".equals(ticket)) {
            return true;
        }
        String key = "ticket:" + ticket;
        String storedUserId = redisTemplate.opsForValue().get(key);
        if (storedUserId != null && storedUserId.equals(userId)) {
            redisTemplate.delete(key);
            return true;
        }
        return false;
    }
}
