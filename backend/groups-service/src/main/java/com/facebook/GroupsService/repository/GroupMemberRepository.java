package com.facebook.GroupsService.repository;

import com.facebook.GroupsService.entity.GroupMemberEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface GroupMemberRepository extends JpaRepository<GroupMemberEntity, Long> {
    List<GroupMemberEntity> findAllByGroupId(String groupId);
    List<GroupMemberEntity> findAllByUserId(String userId);
    Optional<GroupMemberEntity> findByGroupIdAndUserId(String groupId, String userId);
    boolean existsByGroupIdAndUserId(String groupId, String userId);
    void deleteByGroupIdAndUserId(String groupId, String userId);
}
