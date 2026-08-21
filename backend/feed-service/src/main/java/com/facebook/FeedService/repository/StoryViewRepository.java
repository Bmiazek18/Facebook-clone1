package com.facebook.FeedService.repository;

import com.facebook.FeedService.entity.StoryViewEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface StoryViewRepository extends JpaRepository<StoryViewEntity, Long> {
    List<StoryViewEntity> findAllByStoryId(String storyId);
    Optional<StoryViewEntity> findByStoryIdAndViewerId(String storyId, String viewerId);
}
