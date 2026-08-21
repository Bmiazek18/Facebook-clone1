package com.facebook.FeedService.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity
@Table(name = "user_media", indexes = {
    @Index(name = "idx_user_media_user_created", columnList = "userId, createdAt DESC"),
    @Index(name = "idx_user_media_user_type", columnList = "userId, mediaType, createdAt DESC"),
    @Index(name = "idx_user_media_album", columnList = "userId, albumName, createdAt DESC"),
    @Index(name = "idx_user_media_post", columnList = "postId")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserMediaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String userId;

    @Column(nullable = false)
    private String postId;

    @Column(nullable = false, length = 2048)
    private String mediaUrl;

    @Column(nullable = false, length = 32)
    @Builder.Default
    private String mediaType = "IMAGE"; // "IMAGE", "VIDEO"

    @Column(length = 128)
    @Builder.Default
    private String albumName = "Oś czasu";

    @Column(columnDefinition = "TEXT")
    private String altText;

    private Long timestamp;

    @Builder.Default
    private Instant createdAt = Instant.now();
}
