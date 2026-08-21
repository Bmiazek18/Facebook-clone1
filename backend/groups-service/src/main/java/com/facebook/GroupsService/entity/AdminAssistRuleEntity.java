package com.facebook.GroupsService.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "admin_assist_rules")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
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

    @Builder.Default
    private boolean enabled = true;
}
