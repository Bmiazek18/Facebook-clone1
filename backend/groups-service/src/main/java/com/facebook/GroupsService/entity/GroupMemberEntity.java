package com.facebook.GroupsService.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(
    name = "group_members",
    uniqueConstraints = @UniqueConstraint(columnNames = {"groupId", "userId"})
)
public class GroupMemberEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String groupId;
    private String userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "role")
    private GroupRole role; // MEMBER, ADMIN, PENDING, MODERATOR

    @Column(name = "joined_at")
    private Instant joinedAt;

    public GroupMemberEntity() {
    }

    public GroupMemberEntity(Long id, String groupId, String userId, GroupRole role, Instant joinedAt) {
        this.id = id;
        this.groupId = groupId;
        this.userId = userId;
        this.role = role;
        this.joinedAt = joinedAt != null ? joinedAt : Instant.now();
    }

    public static GroupMemberEntityBuilder builder() {
        return new GroupMemberEntityBuilder();
    }

    @PrePersist
    protected void onCreate() {
        if (this.joinedAt == null) {
            this.joinedAt = Instant.now();
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getGroupId() { return groupId; }
    public void setGroupId(String groupId) { this.groupId = groupId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public GroupRole getRole() { return role; }
    public void setRole(GroupRole role) { this.role = role; }

    public Instant getJoinedAt() { return joinedAt; }
    public void setJoinedAt(Instant joinedAt) { this.joinedAt = joinedAt; }

    public static class GroupMemberEntityBuilder {
        private Long id;
        private String groupId;
        private String userId;
        private GroupRole role;
        private Instant joinedAt;

        public GroupMemberEntityBuilder id(Long id) { this.id = id; return this; }
        public GroupMemberEntityBuilder groupId(String groupId) { this.groupId = groupId; return this; }
        public GroupMemberEntityBuilder userId(String userId) { this.userId = userId; return this; }
        public GroupMemberEntityBuilder role(GroupRole role) { this.role = role; return this; }
        public GroupMemberEntityBuilder joinedAt(Instant joinedAt) { this.joinedAt = joinedAt; return this; }

        public GroupMemberEntity build() {
            return new GroupMemberEntity(id, groupId, userId, role, joinedAt);
        }
    }
}
