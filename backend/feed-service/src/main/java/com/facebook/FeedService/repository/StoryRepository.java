package com.facebook.FeedService.repository;

import com.facebook.FeedService.entity.StoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.Instant;
import java.util.List;

public interface StoryRepository extends JpaRepository<StoryEntity, String> {
    List<StoryEntity> findAllByExpiresAtAfterOrderByCreatedAtDesc(Instant now);
    List<StoryEntity> findAllByAuthorIdAndExpiresAtAfterOrderByCreatedAtDesc(String authorId, Instant now);
}
