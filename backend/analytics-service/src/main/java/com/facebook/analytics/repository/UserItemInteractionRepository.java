package com.facebook.analytics.repository;

import com.facebook.analytics.model.UserItemInteraction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserItemInteractionRepository extends JpaRepository<UserItemInteraction, UUID> {
    Optional<UserItemInteraction> findByUserIdAndTargetId(String userId, String targetId);
    List<UserItemInteraction> findByUserIdOrderByLastInteractedAtDesc(String userId);
    List<UserItemInteraction> findByUserIdAndIsHiddenFalseOrderByLastInteractedAtDesc(String userId);
    List<UserItemInteraction> findByTargetId(String targetId);
}
