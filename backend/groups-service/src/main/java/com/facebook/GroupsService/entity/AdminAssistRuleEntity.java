package com.facebook.GroupsService.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "admin_assist_rules")
public class AdminAssistRuleEntity {
    @Id
    private String id;

    @Column(name = "group_id", nullable = false)
    private String groupId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RuleTarget target;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RuleAction action;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "criteria", columnDefinition = "jsonb")
    private RuleCriteria criteria;

    private boolean enabled = true;

    public AdminAssistRuleEntity() {
    }

    public AdminAssistRuleEntity(String id, String groupId, RuleTarget target, RuleAction action, RuleCriteria criteria, boolean enabled) {
        this.id = id;
        this.groupId = groupId;
        this.target = target;
        this.action = action;
        this.criteria = criteria;
        this.enabled = enabled;
    }

    public static AdminAssistRuleEntityBuilder builder() {
        return new AdminAssistRuleEntityBuilder();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getGroupId() { return groupId; }
    public void setGroupId(String groupId) { this.groupId = groupId; }

    public RuleTarget getTarget() { return target; }
    public void setTarget(RuleTarget target) { this.target = target; }

    public RuleAction getAction() { return action; }
    public void setAction(RuleAction action) { this.action = action; }

    public RuleCriteria getCriteria() { return criteria; }
    public void setCriteria(RuleCriteria criteria) { this.criteria = criteria; }

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }

    public static class AdminAssistRuleEntityBuilder {
        private String id;
        private String groupId;
        private RuleTarget target;
        private RuleAction action;
        private RuleCriteria criteria;
        private boolean enabled = true;

        public AdminAssistRuleEntityBuilder id(String id) { this.id = id; return this; }
        public AdminAssistRuleEntityBuilder groupId(String groupId) { this.groupId = groupId; return this; }
        public AdminAssistRuleEntityBuilder target(RuleTarget target) { this.target = target; return this; }
        public AdminAssistRuleEntityBuilder action(RuleAction action) { this.action = action; return this; }
        public AdminAssistRuleEntityBuilder criteria(RuleCriteria criteria) { this.criteria = criteria; return this; }
        public AdminAssistRuleEntityBuilder enabled(boolean enabled) { this.enabled = enabled; return this; }

        public AdminAssistRuleEntity build() {
            return new AdminAssistRuleEntity(id, groupId, target, action, criteria, enabled);
        }
    }
}
