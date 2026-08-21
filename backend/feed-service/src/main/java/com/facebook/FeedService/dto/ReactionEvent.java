package com.facebook.FeedService.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReactionEvent implements Serializable {
    private String userId;
    private String postId;
    private String reactionType;
    private Long timestamp;
}
