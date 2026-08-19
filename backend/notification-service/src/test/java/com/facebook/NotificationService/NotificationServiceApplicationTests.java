package com.facebook.NotificationService;

import com.facebook.NotificationService.model.Notification;
import com.facebook.NotificationService.repository.NotificationRepository;
import com.facebook.NotificationService.service.NotificationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;MODE=PostgreSQL",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
    "grpc.server.port=-1",
    "spring.data.redis.repositories.enabled=false",
    "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration,org.springframework.boot.autoconfigure.data.redis.RedisRepositoriesAutoConfiguration"
})
@ActiveProfiles("test")
@Transactional
class NotificationServiceApplicationTests {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private NotificationRepository notificationRepository;

    @org.springframework.test.context.bean.override.mockito.MockitoBean
    private org.springframework.data.redis.core.RedisTemplate<String, String> redisTemplate;

    @org.springframework.test.context.bean.override.mockito.MockitoBean
    private org.springframework.data.redis.listener.ChannelTopic topic;

    @org.springframework.test.context.bean.override.mockito.MockitoBean
    private org.springframework.data.redis.listener.RedisMessageListenerContainer redisContainer;

    @Test
    void contextLoads() {
    }

    @Test
    void testNotificationLifecycle() {
        String testUserId = "user_test_999";
        
        // 1. Create and send notification
        Notification notification = notificationService.createAndSendNotification(
                testUserId,
                "New Connection Request",
                "User John Doe wants to connect with you.",
                null
        );

        assertNotNull(notification);
        assertNotNull(notification.getId());
        assertEquals(testUserId, notification.getUserId());
        assertEquals("New Connection Request", notification.getTitle());
        assertEquals("User John Doe wants to connect with you.", notification.getMessage());
        assertFalse(notification.isRead());
        assertNotNull(notification.getCreatedAt());

        // 2. Retrieve history and assert presence
        java.util.List<Notification> history = notificationService.getNotificationsForUser(testUserId);
        assertFalse(history.isEmpty());
        assertEquals(1, history.size());
        assertEquals(notification.getId(), history.get(0).getId());
        assertFalse(history.get(0).isRead());

        // 3. Mark notification as read
        Notification readNotification = notificationService.markAsRead(notification.getId());
        assertTrue(readNotification.isRead());

        // 4. Verify status updated in history retrieval
        java.util.List<Notification> updatedHistory = notificationService.getNotificationsForUser(testUserId);
        assertEquals(1, updatedHistory.size());
        assertTrue(updatedHistory.get(0).isRead());
    }
}
