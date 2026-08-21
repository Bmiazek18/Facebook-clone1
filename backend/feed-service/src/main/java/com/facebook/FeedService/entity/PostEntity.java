package com.facebook.FeedService.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "posts")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostEntity {
    @Id
    private String id;

    private String authorId;
    
    @Column(columnDefinition = "TEXT")
    private String content;
    
    private String date;
    private Long timestamp;
    private Boolean isAnonymous;
    private String targetId;
    private String targetType;

    @Column(columnDefinition = "TEXT")
    private String mediaJson;

    @Column(columnDefinition = "TEXT")
    private String contextJson;

    @Builder.Default
    private Integer commentCount = 0;

    @Builder.Default
    private Integer shareCount = 0;

    @Builder.Default
    private String visibility = "PUBLIC";

    @Builder.Default
    private String status = "ACTIVE";

    private Long scheduledPublishTime;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "post_allowed_users", joinColumns = @JoinColumn(name = "post_id"))
    @Column(name = "user_id")
    @Builder.Default
    private java.util.List<String> allowedUserIds = new java.util.ArrayList<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "post_mentions", joinColumns = @JoinColumn(name = "post_id"))
    @Column(name = "user_id")
    @Builder.Default
    private java.util.List<String> mentionedUserIds = new java.util.ArrayList<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "post_tagged_users", joinColumns = @JoinColumn(name = "post_id"))
    @Column(name = "user_id")
    @Builder.Default
    private java.util.List<String> taggedUserIds = new java.util.ArrayList<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "post_hashtags", joinColumns = @JoinColumn(name = "post_id"))
    @Column(name = "hashtag")
    @Builder.Default
    private java.util.List<String> hashtags = new java.util.ArrayList<>();
}
