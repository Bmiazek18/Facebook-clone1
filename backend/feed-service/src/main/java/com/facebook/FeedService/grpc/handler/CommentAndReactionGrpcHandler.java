package com.facebook.FeedService.grpc.handler;

import com.facebook.FeedService.dto.ReactionRequest;
import com.facebook.FeedService.service.CommentService;
import com.facebook.FeedService.service.ReactionService;
import com.facebook.feed.grpc.*;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class CommentAndReactionGrpcHandler {

    private final CommentService commentService;
    private final ReactionService reactionService;

    public void getComments(GetCommentsRequest request, StreamObserver<GetCommentsResponse> responseObserver) {
        try {
            Integer limit = request.getLimit() > 0 ? request.getLimit() : null;
            List<CommentService.CommentResponse> comments = commentService.getCommentsForPost(request.getPostId(), limit);
            GetCommentsResponse.Builder builder = GetCommentsResponse.newBuilder();
            for (CommentService.CommentResponse c : comments) {
                builder.addComments(mapToProtoComment(c));
            }
            responseObserver.onNext(builder.build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("Failed to get comments via gRPC", e);
            responseObserver.onError(io.grpc.Status.INTERNAL
                    .withDescription("Internal error: " + e.getMessage())
                    .asRuntimeException());
        }
    }

    public void addComment(AddCommentRequest request, StreamObserver<AddCommentResponse> responseObserver) {
        try {
            Long parentId = null;
            if (request.getParentId() != null && !request.getParentId().isEmpty()) {
                parentId = Long.parseLong(request.getParentId());
            }
            var entity = commentService.addComment(
                    request.getPostId(),
                    request.getUserId(),
                    parentId,
                    request.getContent(),
                    request.getMediaUrl());
            CommentService.CommentResponse response = new CommentService.CommentResponse(entity, Map.of());
            responseObserver.onNext(AddCommentResponse.newBuilder()
                    .setComment(mapToProtoComment(response))
                    .build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("Failed to add comment via gRPC", e);
            responseObserver.onError(io.grpc.Status.INTERNAL
                    .withDescription("Internal error: " + e.getMessage())
                    .asRuntimeException());
        }
    }

    public void getPostReactions(GetPostReactionsRequest request, StreamObserver<GetPostReactionsResponse> responseObserver) {
        try {
            Map<String, List<String>> details = reactionService.getReactionDetails(request.getPostId());
            GetPostReactionsResponse.Builder builder = GetPostReactionsResponse.newBuilder();
            details.forEach((type, userIds) -> builder.addReactions(
                    ReactionDetail.newBuilder().setReactionType(type).addAllUserIds(userIds).build()));
            responseObserver.onNext(builder.build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("Failed to get post reactions via gRPC", e);
            responseObserver.onError(io.grpc.Status.INTERNAL
                    .withDescription("Internal error: " + e.getMessage())
                    .asRuntimeException());
        }
    }

    public void reactToPost(ReactToPostRequest request, StreamObserver<ReactToPostResponse> responseObserver) {
        try {
            ReactionRequest reactionRequest = ReactionRequest.builder()
                    .userId(request.getUserId())
                    .reactionType(request.getReactionType().isEmpty() ? null : request.getReactionType())
                    .previousReactionType(request.getPreviousReactionType().isEmpty() ? null : request.getPreviousReactionType())
                    .build();
            reactionService.react(request.getPostId(), reactionRequest);
            responseObserver.onNext(ReactToPostResponse.newBuilder().setSuccess(true).build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("Failed to react to post via gRPC", e);
            responseObserver.onError(io.grpc.Status.INTERNAL
                    .withDescription("Internal error: " + e.getMessage())
                    .asRuntimeException());
        }
    }

    public void reactToComment(ReactToCommentRequest request, StreamObserver<ReactToCommentResponse> responseObserver) {
        try {
            Long commentId = Long.parseLong(request.getCommentId());
            String reactionType = request.getReactionType().isEmpty() ? null : request.getReactionType();
            commentService.reactToComment(commentId, request.getUserId(), reactionType);
            responseObserver.onNext(ReactToCommentResponse.newBuilder().setSuccess(true).build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("Failed to react to comment via gRPC", e);
            responseObserver.onError(io.grpc.Status.INTERNAL
                    .withDescription("Internal error: " + e.getMessage())
                    .asRuntimeException());
        }
    }

    private Comment mapToProtoComment(CommentService.CommentResponse c) {
        Comment.Builder builder = Comment.newBuilder()
                .setId(String.valueOf(c.getId()))
                .setUserId(c.getUserId())
                .setPostId(c.getPostId())
                .setContent(c.getContent() != null ? c.getContent() : "")
                .setCreatedAt(c.getCreatedAt() != null ? c.getCreatedAt().toString() : "");
        if (c.getParentId() != null) {
            builder.setParentId(String.valueOf(c.getParentId()));
        }
        if (c.getMediaUrl() != null) {
            builder.setMediaUrl(c.getMediaUrl());
        }
        if (c.getReactions() != null) {
            c.getReactions().forEach((type, userIds) -> builder.addReactions(
                    ReactionDetail.newBuilder().setReactionType(type).addAllUserIds(userIds).build()));
        }
        return builder.build();
    }
}
