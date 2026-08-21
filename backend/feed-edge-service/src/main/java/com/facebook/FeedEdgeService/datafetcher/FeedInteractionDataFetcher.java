package com.facebook.FeedEdgeService.datafetcher;

import com.facebook.feed.grpc.*;
import com.facebook.FeedEdgeService.codegen.types.AddCommentInput;
import com.facebook.FeedEdgeService.codegen.types.Comment;
import com.facebook.FeedEdgeService.codegen.types.CommentReactionInput;
import com.facebook.FeedEdgeService.codegen.types.PostReactionInput;
import com.facebook.FeedEdgeService.codegen.types.ReactionDetail;
import com.facebook.FeedEdgeService.mapper.FeedMapper;

import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsMutation;
import com.netflix.graphql.dgs.DgsQuery;
import com.netflix.graphql.dgs.InputArgument;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;

@DgsComponent
public class FeedInteractionDataFetcher {

    private static final Logger log = LoggerFactory.getLogger(FeedInteractionDataFetcher.class);

    @GrpcClient("feed-service")
    private FeedGrpcServiceGrpc.FeedGrpcServiceBlockingStub feedGrpcStub;

    @GrpcClient("user-service")
    private com.facebook.user.grpc.UserGrpcServiceGrpc.UserGrpcServiceBlockingStub userGrpcStub;

    private final FeedMapper feedMapper;
    private static final java.util.regex.Pattern MENTION_PATTERN = java.util.regex.Pattern.compile("\\[@([a-zA-Z0-9-]+)\\]");

    public FeedInteractionDataFetcher(FeedMapper feedMapper) {
        this.feedMapper = feedMapper;
    }

    @DgsQuery
    public List<Comment> comments(@InputArgument String postId, @InputArgument Integer limit) {
        log.info("Edge: Fetching comments via gRPC for post: {}, limit: {}", postId, limit);
        try {
            GetCommentsResponse response = feedGrpcStub.getComments(GetCommentsRequest.newBuilder()
                    .setPostId(postId)
                    .setLimit(limit != null ? limit : 0)
                    .build());

            List<Comment> comments = response.getCommentsList().stream()
                    .map(feedMapper::mapToComment)
                    .collect(Collectors.toList());

            // Hurtowe wyciąganie oznaczonych użytkowników (Batch mention enrichment)
            java.util.Set<String> allMentionedIds = new java.util.HashSet<>();
            for (Comment c : comments) {
                if (c.getContent() != null) {
                    java.util.regex.Matcher m = MENTION_PATTERN.matcher(c.getContent());
                    while (m.find()) {
                        allMentionedIds.add(m.group(1));
                    }
                }
            }

            if (!allMentionedIds.isEmpty() && userGrpcStub != null) {
                java.util.Map<String, com.facebook.FeedEdgeService.codegen.types.ReactionUser> userMap = new java.util.HashMap<>();
                for (String uId : allMentionedIds) {
                    try {
                        var userRes = userGrpcStub.getUserById(
                                com.facebook.user.grpc.GetUserByIdRequest.newBuilder().setUserId(uId).build()
                        ).getUser();

                        com.facebook.FeedEdgeService.codegen.types.ReactionUser rUser = new com.facebook.FeedEdgeService.codegen.types.ReactionUser();
                        rUser.setId(userRes.getId());
                        rUser.setFirstName(userRes.getFirstName());
                        rUser.setLastName(userRes.getLastName());
                        rUser.setAvatarId(userRes.getAvatarId());

                        String avatarUrl = "";
                        if (userRes.getAvatarId() != null && !userRes.getAvatarId().isEmpty()) {
                            try {
                                var mediaRes = userGrpcStub.resolveMediaUrl(
                                        com.facebook.user.grpc.ResolveMediaUrlRequest.newBuilder().setReference(userRes.getAvatarId()).build()
                                );
                                avatarUrl = mediaRes.getPresignedUrl();
                            } catch (Exception ignored) {}
                        }
                        rUser.setAvatar(avatarUrl);
                        userMap.put(uId, rUser);
                    } catch (Exception e) {
                        log.warn("Failed to resolve mentioned user {} for comments", uId);
                    }
                }

                for (Comment c : comments) {
                    if (c.getContent() != null) {
                        List<com.facebook.FeedEdgeService.codegen.types.ReactionUser> cUsers = new java.util.ArrayList<>();
                        java.util.regex.Matcher m = MENTION_PATTERN.matcher(c.getContent());
                        while (m.find()) {
                            String targetId = m.group(1);
                            if (userMap.containsKey(targetId)) {
                                cUsers.add(userMap.get(targetId));
                            }
                        }
                        c.setMentionedUsers(cUsers);
                    }
                }
            }

            return comments;
        } catch (Exception e) {
            log.error("Failed to fetch comments", e);
            throw new RuntimeException("Feed service unavailable: " + e.getMessage());
        }
    }

    @DgsQuery
    public List<ReactionDetail> postReactions(@InputArgument String postId) {
        log.info("Edge: Fetching reactions via gRPC for post: {}", postId);
        try {
            GetPostReactionsResponse response = feedGrpcStub.getPostReactions(GetPostReactionsRequest.newBuilder()
                    .setPostId(postId)
                    .build());

            return response.getReactionsList().stream()
                    .map(feedMapper::mapToReactionDetail)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Failed to fetch post reactions", e);
            throw new RuntimeException("Feed service unavailable: " + e.getMessage());
        }
    }

    @DgsMutation
    public Comment addComment(@InputArgument AddCommentInput input) {
        log.info("Edge: Adding comment via gRPC for post: {} by user: {}", input.getPostId(), input.getUserId());
        try {
            AddCommentRequest.Builder reqBuilder = AddCommentRequest.newBuilder()
                    .setPostId(input.getPostId())
                    .setUserId(input.getUserId())
                    .setContent(input.getContent() != null ? input.getContent() : "");

            if (input.getMediaUrl() != null) {
                reqBuilder.setMediaUrl(input.getMediaUrl());
            }
            if (input.getParentId() != null && !input.getParentId().isEmpty()) {
                reqBuilder.setParentId(input.getParentId());
            }

            AddCommentResponse response = feedGrpcStub.addComment(reqBuilder.build());
            return feedMapper.mapToComment(response.getComment());
        } catch (Exception e) {
            log.error("Failed to add comment", e);
            throw new RuntimeException("Feed service unavailable: " + e.getMessage());
        }
    }

    @DgsMutation
    public boolean reactToPost(@InputArgument PostReactionInput input) {
        log.info("Edge: Reacting to post via gRPC: {}, type: {}", input.getPostId(), input.getReactionType());
        try {
            ReactToPostRequest.Builder reqBuilder = ReactToPostRequest.newBuilder()
                    .setPostId(input.getPostId())
                    .setUserId(input.getUserId());

            if (input.getReactionType() != null) {
                reqBuilder.setReactionType(input.getReactionType().toUpperCase());
            }
            if (input.getPreviousReactionType() != null) {
                reqBuilder.setPreviousReactionType(input.getPreviousReactionType().toUpperCase());
            }

            feedGrpcStub.reactToPost(reqBuilder.build());
            return true;
        } catch (Exception e) {
            log.error("Failed to react to post", e);
            throw new RuntimeException("Feed service unavailable: " + e.getMessage());
        }
    }

    @DgsMutation
    public boolean reactToComment(@InputArgument CommentReactionInput input) {
        log.info("Edge: Reacting to comment via gRPC: {}, type: {}", input.getCommentId(), input.getReactionType());
        try {
            ReactToCommentRequest.Builder reqBuilder = ReactToCommentRequest.newBuilder()
                    .setCommentId(input.getCommentId())
                    .setUserId(input.getUserId());

            if (input.getReactionType() != null) {
                reqBuilder.setReactionType(input.getReactionType().toUpperCase());
            }

            feedGrpcStub.reactToComment(reqBuilder.build());
            return true;
        } catch (Exception e) {
            log.error("Failed to react to comment", e);
            throw new RuntimeException("Feed service unavailable: " + e.getMessage());
        }
    }
}