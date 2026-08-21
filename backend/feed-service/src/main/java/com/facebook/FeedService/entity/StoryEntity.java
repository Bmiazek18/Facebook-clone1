package com.facebook.FeedService.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity
@Table(name = "stories")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StoryEntity {
    @Id
    private String id;

    private String authorId;
    
    private String mediaUrl;
    private String thumbMediaUrl;
    private String mediaType; // "IMAGE" or "VIDEO"
    
    private String text; // Optional text overlay
    
    private Instant createdAt;
    private Instant expiresAt;
}
