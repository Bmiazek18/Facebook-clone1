package com.facebook.GroupsService.repository;

import com.facebook.GroupsService.entity.GroupActivityLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface GroupActivityLogRepository extends JpaRepository<GroupActivityLogEntity, String> {
    List<GroupActivityLogEntity> findAllByGroupIdOrderByCreatedAtDesc(String groupId);
}
