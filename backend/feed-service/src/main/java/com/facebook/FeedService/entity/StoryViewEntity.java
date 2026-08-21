package com.facebook.FeedService.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity
@Table(name = "story_views", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"storyId", "viewerId"})
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StoryViewEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String storyId;

    @Column(nullable = false)
    private String viewerId;

    @Column(nullable = false)
    private Instant viewedAt;
}
