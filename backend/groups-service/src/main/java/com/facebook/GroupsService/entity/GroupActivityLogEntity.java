package com.facebook.GroupsService.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "group_activity_logs")
public class GroupActivityLogEntity {
    @Id
    private String id;

    @Column(name = "group_id", nullable = false)
    private String groupId;

    @Column(name = "actor_id", nullable = false)
    private String actorId;

    @Column(name = "actor_name", nullable = false)
    private String actorName;

    @Column(name = "description", nullable = false, length = 1000)
    private String description;

    @Column(name = "note", length = 1000)
    private String note;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public GroupActivityLogEntity() {
    }

    public GroupActivityLogEntity(String id, String groupId, String actorId, String actorName, String description, String note, LocalDateTime createdAt) {
        this.id = id;
        this.groupId = groupId;
        this.actorId = actorId;
        this.actorName = actorName;
        this.description = description;
        this.note = note;
        this.createdAt = createdAt;
    }

    public static GroupActivityLogEntityBuilder builder() {
        return new GroupActivityLogEntityBuilder();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getGroupId() { return groupId; }
    public void setGroupId(String groupId) { this.groupId = groupId; }

    public String getActorId() { return actorId; }
    public void setActorId(String actorId) { this.actorId = actorId; }

    public String getActorName() { return actorName; }
    public void setActorName(String actorName) { this.actorName = actorName; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public static class GroupActivityLogEntityBuilder {
        private String id;
        private String groupId;
        private String actorId;
        private String actorName;
        private String description;
        private String note;
        private LocalDateTime createdAt;

        public GroupActivityLogEntityBuilder id(String id) { this.id = id; return this; }
        public GroupActivityLogEntityBuilder groupId(String groupId) { this.groupId = groupId; return this; }
        public GroupActivityLogEntityBuilder actorId(String actorId) { this.actorId = actorId; return this; }
        public GroupActivityLogEntityBuilder actorName(String actorName) { this.actorName = actorName; return this; }
        public GroupActivityLogEntityBuilder description(String description) { this.description = description; return this; }
        public GroupActivityLogEntityBuilder note(String note) { this.note = note; return this; }
        public GroupActivityLogEntityBuilder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }

        public GroupActivityLogEntity build() {
            return new GroupActivityLogEntity(id, groupId, actorId, actorName, description, note, createdAt);
        }
    }
}
