package com.facebook.FeedService.repository;

import com.facebook.FeedService.entity.CommentReactionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CommentReactionRepository extends JpaRepository<CommentReactionEntity, Long> {
    List<CommentReactionEntity> findByCommentId(Long commentId);
    List<CommentReactionEntity> findByCommentIdIn(List<Long> commentIds);
}
