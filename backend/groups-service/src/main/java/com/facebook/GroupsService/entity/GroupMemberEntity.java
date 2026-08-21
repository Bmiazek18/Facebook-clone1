package com.facebook.GroupsService.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity
@Table(
    name = "group_members",
    uniqueConstraints = @UniqueConstraint(columnNames = {"groupId", "userId"})
)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
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

    @PrePersist
    protected void onCreate() {
        if (this.joinedAt == null) {
            this.joinedAt = Instant.now();
        }
    }
}
