package com.facebook.GroupsService.repository;

import com.facebook.GroupsService.entity.GroupRuleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface GroupRuleRepository extends JpaRepository<GroupRuleEntity, String> {
    List<GroupRuleEntity> findAllByGroupIdOrderByOrderIndexAsc(String groupId);
}
