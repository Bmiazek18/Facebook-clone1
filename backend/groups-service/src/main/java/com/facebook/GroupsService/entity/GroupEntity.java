package com.facebook.GroupsService.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "groups")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GroupEntity {
    @Id
    private String id;

    private String name;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    private String privacy; // public, private
    private String image;
    private String lastActive;

    @Builder.Default
    private Integer membersCount = 0;

    @Builder.Default
    private Integer newPostsToday = 0;

    @Builder.Default
    private Integer newPostsMonth = 0;

    @Builder.Default
    private String newMembersWeek = "";

    @Builder.Default
    private String createdAge = "";
}
