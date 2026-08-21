package com.facebook.GroupsService.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "group_rules")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GroupRuleEntity {
    @Id
    private String id;

    @Column(name = "group_id", nullable = false)
    private String groupId;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "order_index", nullable = false)
    private int orderIndex;
}
