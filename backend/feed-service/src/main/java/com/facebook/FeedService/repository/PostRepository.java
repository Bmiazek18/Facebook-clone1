package com.facebook.FeedService.repository;

import com.facebook.FeedService.entity.PostEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostRepository extends JpaRepository<PostEntity, String> {
    List<PostEntity> findAllByOrderByTimestampDesc();
    List<PostEntity> findAllByTargetTypeAndTargetIdOrderByTimestampDesc(String targetType, String targetId);

    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.transaction.annotation.Transactional
    @org.springframework.data.jpa.repository.Query("UPDATE PostEntity p SET p.commentCount = COALESCE(p.commentCount, 0) + 1 WHERE p.id = :postId")
    void incrementCommentCount(String postId);

    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.transaction.annotation.Transactional
    @org.springframework.data.jpa.repository.Query("UPDATE PostEntity p SET p.shareCount = COALESCE(p.shareCount, 0) + 1 WHERE p.id = :postId")
    void incrementShareCount(String postId);
    @org.springframework.data.jpa.repository.Query("SELECT p FROM PostEntity p WHERE p.mediaJson LIKE %:fileId%")
    List<PostEntity> findByMediaFileId(String fileId);

    @org.springframework.data.jpa.repository.Query("SELECT p FROM PostEntity p JOIN p.hashtags h WHERE h = :hashtag ORDER BY p.timestamp DESC")
    List<PostEntity> findByHashtag(String hashtag);

    @org.springframework.data.jpa.repository.Query("SELECT p FROM PostEntity p JOIN p.taggedUserIds t WHERE t = :userId ORDER BY p.timestamp DESC")
    List<PostEntity> findByTaggedUserId(@org.springframework.data.repository.query.Param("userId") String userId);

    List<PostEntity> findByAuthorIdOrderByTimestampDesc(String authorId);
    List<PostEntity> findByStatusAndTargetTypeAndTargetIdOrderByScheduledPublishTimeAsc(String status, String targetType, String targetId);
    List<PostEntity> findByStatusAndAuthorIdOrderByScheduledPublishTimeAsc(String status, String authorId);
}
