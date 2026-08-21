package com.facebook.FeedService.repository;

import com.facebook.FeedService.entity.ReactionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReactionRepository extends JpaRepository<ReactionEntity, Long>, ReactionRepositoryCustom {
    Optional<ReactionEntity> findByUserIdAndPostId(String userId, String postId);

    @Query("SELECT r.reactionType, COUNT(r) FROM ReactionEntity r WHERE r.postId = :postId GROUP BY r.reactionType")
    List<Object[]> countReactionsGroupByPostId(@Param("postId") String postId);

    List<ReactionEntity> findByPostId(String postId);
}
