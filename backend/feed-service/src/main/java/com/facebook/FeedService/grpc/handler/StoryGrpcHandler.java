package com.facebook.FeedService.grpc.handler;

import com.facebook.FeedService.entity.StoryEntity;
import com.facebook.FeedService.entity.StoryViewEntity;
import com.facebook.FeedService.repository.StoryRepository;
import com.facebook.FeedService.repository.StoryViewRepository;
import com.facebook.abr.grpc.AbrGrpcServiceGrpc;
import com.facebook.abr.grpc.ProcessStoryImageRequest;
import com.facebook.feed.grpc.*;
import com.facebook.socialgraph.grpc.SocialGraphGrpcServiceGrpc;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class StoryGrpcHandler {

    private final StoryRepository storyRepository;
    private final StoryViewRepository storyViewRepository;
    private final MediaUrlSigner mediaUrlSigner;

    @GrpcClient("social-graph-service")
    private SocialGraphGrpcServiceGrpc.SocialGraphGrpcServiceBlockingStub socialGraphGrpcStub;

    @GrpcClient("abr-service")
    private AbrGrpcServiceGrpc.AbrGrpcServiceBlockingStub abrGrpcStub;

    public void getActiveStories(GetActiveStoriesRequest request, StreamObserver<GetActiveStoriesResponse> responseObserver) {
        log.info("gRPC: Fetching active stories for user: {}", request.getCurrentUserId());
        try {
            String currentUserId = request.getCurrentUserId();
            Set<String> allowedAuthors = new HashSet<>();
            allowedAuthors.add(currentUserId);

            try {
                if (socialGraphGrpcStub != null) {
                    var friendsResponse = socialGraphGrpcStub.getFriends(
                            com.facebook.socialgraph.grpc.GetFriendsRequest.newBuilder()
                                    .setUserId(currentUserId)
                                    .build()
                    );
                    if (friendsResponse != null && friendsResponse.getFriendIdsList() != null) {
                        allowedAuthors.addAll(friendsResponse.getFriendIdsList());
                    }
                }
            } catch (Exception e) {
                log.warn("Failed to fetch friends for user {} via gRPC from socialgraph-service: {}", currentUserId, e.getMessage());
            }

            List<StoryEntity> entities = storyRepository.findAllByExpiresAtAfterOrderByCreatedAtDesc(Instant.now());
            List<Story> protoStories = entities.stream()
                    .filter(story -> story.getAuthorId() != null && allowedAuthors.contains(story.getAuthorId()))
                    .filter(story -> story.getMediaUrl() != null && !story.getMediaUrl().isBlank())
                    .map(this::mapToProtoStory)
                    .collect(Collectors.toList());

            GetActiveStoriesResponse response = GetActiveStoriesResponse.newBuilder()
                    .addAllStories(protoStories)
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("Failed to get active stories via gRPC", e);
            responseObserver.onError(io.grpc.Status.INTERNAL
                    .withDescription("Internal error: " + e.getMessage())
                    .asRuntimeException());
        }
    }

    public void createStory(CreateStoryRequest request, StreamObserver<CreateStoryResponse> responseObserver) {
        log.info("gRPC: Creating story for user: {}", request.getAuthorId());
        try {
            String fileId = mediaUrlSigner.extractFileId(request.getMediaUrl());
            Instant now = Instant.now();
            Instant expires = now.plus(24, java.time.temporal.ChronoUnit.HOURS);

            StoryEntity entity = StoryEntity.builder()
                    .id(UUID.randomUUID().toString())
                    .authorId(request.getAuthorId())
                    .mediaUrl(fileId)
                    .mediaType(request.getMediaType())
                    .text(request.getText())
                    .createdAt(now)
                    .expiresAt(expires)
                    .build();

            StoryEntity saved = storyRepository.save(entity);

            if ("IMAGE".equalsIgnoreCase(request.getMediaType()) && abrGrpcStub != null) {
                try {
                    var abrResponse = abrGrpcStub.processStoryImage(
                            ProcessStoryImageRequest.newBuilder().setFileId(fileId).build());
                    if (abrResponse.getSuccess()) {
                        for (var variant : abrResponse.getVariantsList()) {
                            if (variant.getWidth() == 1080 && variant.getHeight() == 1920) {
                                saved.setMediaUrl(variant.getPath());
                            } else if (variant.getWidth() == 227 && variant.getHeight() == 403) {
                                saved.setThumbMediaUrl(variant.getPath());
                            }
                        }
                        saved = storyRepository.save(saved);
                    } else {
                        log.warn("ABR story processing failed for {}: {}", fileId, abrResponse.getError());
                    }
                } catch (Exception e) {
                    log.warn("Failed to process story image via ABR gRPC for {}: {}", fileId, e.getMessage());
                }
            }

            Story protoStory = mapToProtoStory(saved);
            CreateStoryResponse response = CreateStoryResponse.newBuilder()
                    .setStory(protoStory)
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("Failed to create story via gRPC", e);
            responseObserver.onError(io.grpc.Status.INTERNAL
                    .withDescription("Internal error: " + e.getMessage())
                    .asRuntimeException());
        }
    }

    public void markStoryAsViewed(MarkStoryAsViewedRequest request, StreamObserver<MarkStoryAsViewedResponse> responseObserver) {
        log.info("gRPC: Marking story {} as viewed by {}", request.getStoryId(), request.getViewerId());
        try {
            String storyId = request.getStoryId();
            String viewerId = request.getViewerId();

            Optional<StoryViewEntity> existing = storyViewRepository.findByStoryIdAndViewerId(storyId, viewerId);
            if (existing.isEmpty()) {
                StoryViewEntity view = StoryViewEntity.builder()
                        .storyId(storyId)
                        .viewerId(viewerId)
                        .viewedAt(Instant.now())
                        .build();
                storyViewRepository.save(view);
            }

            responseObserver.onNext(MarkStoryAsViewedResponse.newBuilder().setSuccess(true).build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("Failed to mark story as viewed", e);
            responseObserver.onError(io.grpc.Status.INTERNAL
                    .withDescription("Internal error: " + e.getMessage())
                    .asRuntimeException());
        }
    }

    public Story mapToProtoStory(StoryEntity entity) {
        List<String> viewerIds = storyViewRepository.findAllByStoryId(entity.getId()).stream()
                .map(StoryViewEntity::getViewerId)
                .collect(Collectors.toList());

        Story.Builder builder = Story.newBuilder()
                .setId(entity.getId())
                .setAuthorId(entity.getAuthorId())
                .setMediaUrl(mediaUrlSigner.reconstructStoryUrl(entity.getMediaUrl()))
                .setMediaType(entity.getMediaType())
                .setText(entity.getText() != null ? entity.getText() : "")
                .setCreatedAt(entity.getCreatedAt().toString())
                .setExpiresAt(entity.getExpiresAt().toString())
                .addAllViewedByUserIds(viewerIds);
        if (entity.getThumbMediaUrl() != null && !entity.getThumbMediaUrl().isEmpty()) {
            builder.setThumbMediaUrl(mediaUrlSigner.reconstructStoryUrl(entity.getThumbMediaUrl()));
        }
        return builder.build();
    }
}
