package com.facebook.GroupsService.repository;

import com.facebook.GroupsService.entity.GroupEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface GroupRepository extends JpaRepository<GroupEntity, String> {
    @Transactional
    @Modifying
    @Query("UPDATE GroupEntity g SET g.membersCount = g.membersCount + 1 WHERE g.id = :groupId")
    void incrementMembersCount(@Param("groupId") String groupId);

    @Transactional
    @Modifying
    @Query("UPDATE GroupEntity g SET g.membersCount = GREATEST(0, g.membersCount - 1) WHERE g.id = :groupId")
    void decrementMembersCount(@Param("groupId") String groupId);
}
