package com.facebook.FeedService.service;

import com.facebook.FeedService.entity.CommentEntity;
import com.facebook.FeedService.entity.CommentReactionEntity;
import com.facebook.FeedService.repository.CommentReactionRepository;
import com.facebook.FeedService.repository.CommentRepository;
import com.facebook.FeedService.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final CommentReactionRepository commentReactionRepository;
    private final PostRepository postRepository;
    private final com.facebook.FeedService.util.MentionHelper mentionHelper;

    @Transactional
    public CommentEntity addComment(String postId, String userId, Long parentId, String content, String mediaUrl) {
        CommentEntity comment = CommentEntity.builder()
                .postId(postId)
                .userId(userId)
                .parentId(parentId)
                .content(content)
                .mediaUrl(mediaUrl)
                .createdAt(Instant.now())
                .mentionedUserIds(mentionHelper.extractMentionedUserIds(content))
                .build();
        CommentEntity saved = commentRepository.save(comment);
        postRepository.incrementCommentCount(postId);

        try {
            mentionHelper.sendMentionNotifications(saved.getMentionedUserIds(), saved.getUserId(), "komentarzu");
        } catch (Exception ex) {
            org.slf4j.LoggerFactory.getLogger(CommentService.class)
                    .error("Failed to send mention notifications for comment " + saved.getId(), ex);
        }

        return saved;
    }

    public List<CommentResponse> getCommentsForPost(String postId) {
        return getCommentsForPost(postId, null);
    }

    public List<CommentResponse> getCommentsForPost(String postId, Integer limit) {
        List<CommentEntity> comments = commentRepository.findByPostId(postId);
        if (comments.isEmpty()) {
            return Collections.emptyList();
        }

        if (limit != null && limit > 0 && limit < comments.size()) {
            comments = comments.subList(0, limit);
        }

        List<Long> commentIds = comments.stream().map(CommentEntity::getId).collect(Collectors.toList());
        List<CommentReactionEntity> reactions = commentReactionRepository.findByCommentIdIn(commentIds);

        // Group reactions by commentId, and then by reactionType
        Map<Long, Map<String, List<String>>> reactionsMap = new HashMap<>();
        for (CommentReactionEntity r : reactions) {
            reactionsMap.computeIfAbsent(r.getCommentId(), k -> new HashMap<>())
                    .computeIfAbsent(r.getReactionType(), k -> new ArrayList<>())
                    .add(r.getUserId());
        }

        return comments.stream().map(c -> {
            Map<String, List<String>> cReactions = reactionsMap.getOrDefault(c.getId(), Collections.emptyMap());
            return new CommentResponse(c, cReactions);
        }).collect(Collectors.toList());
    }

    @Transactional
    public void reactToComment(Long commentId, String userId, String reactionType) {
        if (reactionType == null) {
            // Delete reaction
            commentReactionRepository.findByCommentId(commentId).stream()
                    .filter(r -> r.getUserId().equals(userId))
                    .forEach(commentReactionRepository::delete);
            return;
        }

        // Upsert reaction
        Optional<CommentReactionEntity> existing = commentReactionRepository.findByCommentId(commentId).stream()
                .filter(r -> r.getUserId().equals(userId))
                .findFirst();

        if (existing.isPresent()) {
            CommentReactionEntity r = existing.get();
            r.setReactionType(reactionType);
            commentReactionRepository.save(r);
        } else {
            CommentReactionEntity r = CommentReactionEntity.builder()
                    .commentId(commentId)
                    .userId(userId)
                    .reactionType(reactionType)
                    .createdAt(Instant.now())
                    .build();
            commentReactionRepository.save(r);
        }
    }

    @lombok.Data
    public static class CommentResponse {
        private Long id;
        private String postId;
        private String userId;
        private Long parentId;
        private String content;
        private String mediaUrl;
        private Instant createdAt;
        private Map<String, List<String>> reactions;

        public CommentResponse(CommentEntity entity, Map<String, List<String>> reactions) {
            this.id = entity.getId();
            this.postId = entity.getPostId();
            this.userId = entity.getUserId();
            this.parentId = entity.getParentId();
            this.content = entity.getContent();
            this.mediaUrl = entity.getMediaUrl();
            this.createdAt = entity.getCreatedAt();
            this.reactions = reactions;
        }
    }
}
