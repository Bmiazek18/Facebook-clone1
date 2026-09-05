package com.facebook.GroupsService.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "group_rules")
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

    public GroupRuleEntity() {
    }

    public GroupRuleEntity(String id, String groupId, String title, String description, int orderIndex) {
        this.id = id;
        this.groupId = groupId;
        this.title = title;
        this.description = description;
        this.orderIndex = orderIndex;
    }

    public static GroupRuleEntityBuilder builder() {
        return new GroupRuleEntityBuilder();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getGroupId() { return groupId; }
    public void setGroupId(String groupId) { this.groupId = groupId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public int getOrderIndex() { return orderIndex; }
    public void setOrderIndex(int orderIndex) { this.orderIndex = orderIndex; }

    public static class GroupRuleEntityBuilder {
        private String id;
        private String groupId;
        private String title;
        private String description;
        private int orderIndex;

        public GroupRuleEntityBuilder id(String id) { this.id = id; return this; }
        public GroupRuleEntityBuilder groupId(String groupId) { this.groupId = groupId; return this; }
        public GroupRuleEntityBuilder title(String title) { this.title = title; return this; }
        public GroupRuleEntityBuilder description(String description) { this.description = description; return this; }
        public GroupRuleEntityBuilder orderIndex(int orderIndex) { this.orderIndex = orderIndex; return this; }

        public GroupRuleEntity build() {
            return new GroupRuleEntity(id, groupId, title, description, orderIndex);
        }
    }
}
