package com.facebook.GroupsService.repository;

import com.facebook.GroupsService.entity.AdminAssistRuleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AdminAssistRuleRepository extends JpaRepository<AdminAssistRuleEntity, String> {
    List<AdminAssistRuleEntity> findAllByGroupId(String groupId);
    List<AdminAssistRuleEntity> findAllByGroupIdAndEnabledTrue(String groupId);
    List<AdminAssistRuleEntity> findAllByTargetAndEnabledTrue(com.facebook.GroupsService.entity.RuleTarget target);
}
