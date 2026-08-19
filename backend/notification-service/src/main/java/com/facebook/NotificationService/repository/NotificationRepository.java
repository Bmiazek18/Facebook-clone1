package com.facebook.NotificationService.repository;

import com.facebook.NotificationService.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByUserIdOrderByCreatedAtDesc(String userId);
    List<Notification> findByUserIdAndIsReadOrderByCreatedAtDesc(String userId, boolean isRead);
    java.util.Optional<Notification> findByUserIdAndTitleAndTargetId(String userId, String title, String targetId);
    void deleteByUserIdAndTitleAndTargetId(String userId, String title, String targetId);
}
