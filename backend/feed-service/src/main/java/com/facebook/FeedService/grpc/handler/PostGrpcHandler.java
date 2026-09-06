package com.facebook.FeedService.grpc.handler;

import com.facebook.FeedService.entity.PostEntity;
import com.facebook.FeedService.repository.PostRepository;
import com.facebook.FeedService.scheduler.ScheduledPostPublisher;
import com.facebook.FeedService.service.ImageColorService;
import com.facebook.FeedService.service.PostStatusService;
import com.facebook.FeedService.service.ReactionService;
import com.facebook.FeedService.service.UserMediaService;
import com.facebook.FeedService.util.MentionHelper;
import com.facebook.abr.grpc.AbrGrpcServiceGrpc;
import com.facebook.abr.grpc.GetVideoInfoRequest;
import com.facebook.feed.grpc.*;
import com.facebook.socialgraph.grpc.SocialGraphGrpcServiceGrpc;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class PostGrpcHandler {

    private final PostRepository postRepository;
    private final ReactionService reactionService;
    private final PostStatusService postStatusService;
    private final ImageColorService imageColorService;
    private final UserMediaService userMediaService;
    private final ScheduledPostPublisher scheduledPostPublisher;
    private final MentionHelper mentionHelper;
    private final MediaUrlSigner mediaUrlSigner;

    @GrpcClient("social-graph-service")
    private SocialGraphGrpcServiceGrpc.SocialGraphGrpcServiceBlockingStub socialGraphGrpcStub;

    @GrpcClient("abr-service")
    private AbrGrpcServiceGrpc.AbrGrpcServiceBlockingStub abrGrpcStub;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(2))
            .build();

    public void getFeed(GetFeedRequest request, StreamObserver<GetFeedResponse> responseObserver) {
        log.info("gRPC: Fetching feed for user: {}, limit: {}, offset: {}",
                request.getCurrentUserId(), request.getLimit(), request.getOffset());

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

            List<PostEntity> candidates;
            boolean isHashtagSearch = request.getHashtag() != null && !request.getHashtag().isEmpty();

            if (isHashtagSearch) {
                String cleanTag = request.getHashtag().toLowerCase().replace("#", "");
                candidates = postRepository.findByHashtag(cleanTag);
            } else {
                List<String> candidateIds = fetchCandidatesFromRecommendService(currentUserId);
                if (candidateIds != null && !candidateIds.isEmpty()) {
                    List<PostEntity> fetched = postRepository.findAllById(candidateIds);
                    Map<String, PostEntity> postMap = fetched.stream()
                            .collect(Collectors.toMap(PostEntity::getId, p -> p));
                    List<PostEntity> orderedCandidates = new ArrayList<>();
                    for (String cid : candidateIds) {
                        if (postMap.containsKey(cid)) {
                            orderedCandidates.add(postMap.get(cid));
                        }
                    }
                    candidates = orderedCandidates;
                } else {
                    log.info("No candidates returned from recommend-service, falling back to chronological feed");
                    candidates = postRepository.findAllByOrderByTimestampDesc();
                }
            }

            List<PostEntity> filteredEntities = candidates.stream()
                    .filter(post -> {
                        if (post.getAuthorId() == null) return false;

                        if ("Group".equalsIgnoreCase(post.getTargetType())) {
                            return false;
                        }

                        String status = post.getStatus() != null ? post.getStatus() : "ACTIVE";
                        if ("REJECTED".equalsIgnoreCase(status) ||
                            "HIDDEN".equalsIgnoreCase(status) ||
                            "NOT_VISIBLE".equalsIgnoreCase(status) ||
                            "RENDERING".equalsIgnoreCase(status) ||
                            "SCHEDULED".equalsIgnoreCase(status)) {
                            return false;
                        }

                        String authorId = post.getAuthorId();
                        if (currentUserId.equals(authorId)) {
                            return true;
                        }

                        String vis = post.getVisibility() != null ? post.getVisibility() : "PUBLIC";
                        if ("PUBLIC".equalsIgnoreCase(vis)) {
                            return true;
                        }
                        if ("ONLY_ME".equalsIgnoreCase(vis)) {
                            return false;
                        }
                        if ("SELECT_USERS".equalsIgnoreCase(vis) || "SPECIFIC_FRIENDS".equalsIgnoreCase(vis)) {
                            return post.getAllowedUserIds() != null && post.getAllowedUserIds().contains(currentUserId);
                        }
                        if ("FRIENDS_ONLY".equalsIgnoreCase(vis) || "FRIENDS".equalsIgnoreCase(vis)) {
                            return allowedAuthors.contains(authorId);
                        }
                        return allowedAuthors.contains(authorId);
                    })
                    .collect(Collectors.toList());

            List<Post> posts;
            if (request.getCurrentUserId() != null && !request.getCurrentUserId().isEmpty() && !isHashtagSearch) {
                posts = rankPostsMultiObjective(filteredEntities, request.getCurrentUserId());
            } else {
                posts = filteredEntities.stream()
                        .map(this::mapToProtoPost)
                        .collect(Collectors.toList());
            }

            posts = diversifyFeed(posts);
            posts = applyEpsilonGreedyExploration(posts, candidates);

            int start = request.getOffset();
            if (start > posts.size()) {
                posts = List.of();
            } else {
                int end = posts.size();
                if (request.getLimit() > 0 && start + request.getLimit() < posts.size()) {
                    end = start + request.getLimit();
                }
                posts = posts.subList(start, end);
            }

            GetFeedResponse response = GetFeedResponse.newBuilder()
                    .addAllPosts(posts)
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("Failed to get feed via gRPC", e);
            responseObserver.onError(io.grpc.Status.INTERNAL
                    .withDescription("Internal error: " + e.getMessage())
                    .asRuntimeException());
        }
    }

    public void createPost(CreatePostRequest request, StreamObserver<CreatePostResponse> responseObserver) {
        log.info("gRPC: Creating post for user: {}", request.getAuthorId());

        try {
            List<PostMedia> processedMedia = new ArrayList<>();
            for (com.facebook.feed.grpc.PostMedia m : request.getMediaList()) {
                String fileId = mediaUrlSigner.extractFileId(m.getSrc());
                String bgColor = m.getBackgroundColor();
                if ((bgColor == null || bgColor.isBlank()) && request.getMediaCount() == 1) {
                    bgColor = imageColorService.extractDominantColor(m.getSrc());
                }
                var mediaBuilder = PostMedia.newBuilder()
                        .setSrc(fileId)
                        .setAltText(m.getAltText() != null ? m.getAltText() : "")
                        .setBackgroundColor(bgColor != null ? bgColor : "");
                for (com.facebook.feed.grpc.ImageTag tag : m.getTagsList()) {
                    mediaBuilder.addTags(tag);
                }
                processedMedia.add(mediaBuilder.build());
            }

            String mediaJson = objectMapper.writeValueAsString(processedMedia.stream()
                    .map(m -> {
                        MediaItem item = new MediaItem();
                        item.setSrc(m.getSrc());
                        item.setAltText(m.getAltText());
                        item.setBackgroundColor(m.getBackgroundColor());
                        List<ImageTagItem> tagItems = new ArrayList<>();
                        for (com.facebook.feed.grpc.ImageTag t : m.getTagsList()) {
                            ImageTagItem tagItem = new ImageTagItem();
                            tagItem.setId(t.getId());
                            tagItem.setX(t.getX());
                            tagItem.setY(t.getY());
                            tagItem.setUserId(t.getUserId());
                            tagItems.add(tagItem);
                        }
                        item.setTags(tagItems);
                        return item;
                    })
                    .collect(Collectors.toList()));

            boolean containsVideo = false;
            for (PostMedia m : processedMedia) {
                String fileId = m.getSrc();
                try {
                    if (abrGrpcStub != null) {
                        var info = abrGrpcStub.getVideoInfo(
                                GetVideoInfoRequest.newBuilder().setFileId(fileId).build());
                        if (info.getIsVideo()) {
                            containsVideo = true;
                            break;
                        }
                    }
                } catch (Exception e) {
                    log.warn("Failed to check if media {} is a video via gRPC: {}", fileId, e.getMessage());
                }
            }

            boolean isScheduled = request.getScheduledPublishTime() > System.currentTimeMillis();
            String status = isScheduled ? "SCHEDULED" : (containsVideo ? "RENDERING" : "ACTIVE");

            PostEntity entity = PostEntity.builder()
                    .id(UUID.randomUUID().toString())
                    .authorId(request.getAuthorId())
                    .content(request.getContent())
                    .date(isScheduled ? Instant.ofEpochMilli(request.getScheduledPublishTime()).toString() : Instant.now().toString())
                    .timestamp(isScheduled ? request.getScheduledPublishTime() : System.currentTimeMillis())
                    .isAnonymous(request.getIsAnonymous())
                    .targetId(request.getTargetId())
                    .targetType(request.getTargetType())
                    .mediaJson(mediaJson)
                    .contextJson(request.getContextJson())
                    .visibility(request.getVisibility() != null && !request.getVisibility().isEmpty() ? request.getVisibility() : "PUBLIC")
                    .status(status)
                    .scheduledPublishTime(isScheduled ? request.getScheduledPublishTime() : null)
                    .allowedUserIds(request.getAllowedUserIdsList() != null ? new ArrayList<>(request.getAllowedUserIdsList()) : new ArrayList<>())
                    .taggedUserIds(request.getTaggedUserIdsList() != null ? new ArrayList<>(request.getTaggedUserIdsList()) : new ArrayList<>())
                    .mentionedUserIds(mentionHelper.extractMentionedUserIds(request.getContent()))
                    .hashtags(extractHashtags(request.getContent()))
                    .build();

            PostEntity saved = postRepository.save(entity);

            if (isScheduled) {
                scheduledPostPublisher.enqueue(saved.getId(), request.getScheduledPublishTime());
                log.info("Post {} successfully scheduled for future publishing at {}", saved.getId(), request.getScheduledPublishTime());
            } else {
                userMediaService.indexPostMedia(saved);
                notifyRecommendServiceOfNewPost(saved);

                try {
                    mentionHelper.sendMentionNotifications(saved.getMentionedUserIds(), saved.getAuthorId(), "swoim poście");
                } catch (Exception ex) {
                    log.error("Failed to send mention notifications for post " + saved.getId(), ex);
                }

                if ("post".equalsIgnoreCase(saved.getTargetType()) && saved.getTargetId() != null && !saved.getTargetId().isEmpty()) {
                    try {
                        postRepository.incrementShareCount(saved.getTargetId());
                        log.info("Atomically incremented share count for original post: {}", saved.getTargetId());
                    } catch (Exception ex) {
                        log.error("Failed to increment share count for target post: {}", saved.getTargetId(), ex);
                    }
                }

                enqueueModeration(saved);
            }

            Post protoPost = mapToProtoPost(saved);
            CreatePostResponse response = CreatePostResponse.newBuilder()
                    .setPost(protoPost)
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("Failed to create post via gRPC", e);
            responseObserver.onError(io.grpc.Status.INTERNAL
                    .withDescription("Internal error: " + e.getMessage())
                    .asRuntimeException());
        }
    }

    public void getPostById(GetPostByIdRequest request, StreamObserver<GetPostByIdResponse> responseObserver) {
        log.info("gRPC: Fetching post by id: {}", request.getPostId());
        try {
            Optional<PostEntity> entityOpt = postRepository.findById(request.getPostId());
            if (entityOpt.isPresent()) {
                Post protoPost = mapToProtoPost(entityOpt.get());
                responseObserver.onNext(GetPostByIdResponse.newBuilder().setPost(protoPost).build());
            } else {
                responseObserver.onNext(GetPostByIdResponse.newBuilder().build());
            }
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("Failed to get post by id", e);
            responseObserver.onError(io.grpc.Status.INTERNAL
                    .withDescription("Internal error: " + e.getMessage())
                    .asRuntimeException());
        }
    }

    public void updatePostStatus(UpdatePostStatusRequest request, StreamObserver<UpdatePostStatusResponse> responseObserver) {
        try {
            boolean success = postStatusService.updatePostStatus(request.getPostId(), request.getStatus());
            responseObserver.onNext(UpdatePostStatusResponse.newBuilder().setSuccess(success).build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("Failed to update post status via gRPC", e);
            responseObserver.onError(io.grpc.Status.INTERNAL
                    .withDescription("Internal error: " + e.getMessage())
                    .asRuntimeException());
        }
    }

    public void updatePostMediaStatus(UpdatePostMediaStatusRequest request, StreamObserver<UpdatePostMediaStatusResponse> responseObserver) {
        try {
            int updated = postStatusService.updatePostStatusByMedia(request.getFileId(), request.getStatus());
            responseObserver.onNext(UpdatePostMediaStatusResponse.newBuilder()
                    .setSuccess(updated > 0)
                    .setUpdatedCount(updated)
                    .build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("Failed to update post media status via gRPC", e);
            responseObserver.onError(io.grpc.Status.INTERNAL
                    .withDescription("Internal error: " + e.getMessage())
                    .asRuntimeException());
        }
    }

    @SuppressWarnings("unchecked")
    public void voteOnPoll(VoteOnPollRequest request, StreamObserver<VoteOnPollResponse> responseObserver) {
        log.info("gRPC: Voting on poll for post: {}, option: {}, user: {}",
                request.getPostId(), request.getOptionId(), request.getUserId());
        try {
            Optional<PostEntity> entityOpt = postRepository.findById(request.getPostId());
            if (entityOpt.isEmpty()) {
                responseObserver.onError(io.grpc.Status.NOT_FOUND
                        .withDescription("Post not found with ID: " + request.getPostId())
                        .asRuntimeException());
                return;
            }

            PostEntity entity = entityOpt.get();
            String contextJson = entity.getContextJson();
            if (contextJson == null || contextJson.isEmpty()) {
                contextJson = "{}";
            }

            Map<String, Object> context = objectMapper.readValue(contextJson, new TypeReference<Map<String, Object>>() {});
            Map<String, Object> poll = (Map<String, Object>) context.get("poll");
            if (poll != null) {
                List<Map<String, Object>> options = (List<Map<String, Object>>) poll.get("options");
                if (options != null) {
                    for (Map<String, Object> opt : options) {
                        if (request.getOptionId().equals(String.valueOf(opt.get("id")))) {
                            List<String> votes = (List<String>) opt.get("votes");
                            if (votes == null) {
                                votes = new ArrayList<>();
                            } else {
                                votes = new ArrayList<>(votes);
                            }
                            String uId = request.getUserId();
                            if (votes.contains(uId)) {
                                votes.remove(uId);
                            } else {
                                votes.add(uId);
                            }
                            opt.put("votes", votes);
                            break;
                        }
                    }
                }
            }

            entity.setContextJson(objectMapper.writeValueAsString(context));
            PostEntity saved = postRepository.save(entity);

            Post protoPost = mapToProtoPost(saved);
            responseObserver.onNext(VoteOnPollResponse.newBuilder().setPost(protoPost).build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("Failed to vote on poll via gRPC", e);
            responseObserver.onError(io.grpc.Status.INTERNAL
                    .withDescription("Internal error: " + e.getMessage())
                    .asRuntimeException());
        }
    }

    public void getGroupFeed(GetGroupFeedRequest request, StreamObserver<GetGroupFeedResponse> responseObserver) {
        log.info("gRPC: Fetching group feed for group: {}", request.getGroupId());
        try {
            List<PostEntity> entities = postRepository.findAllByTargetTypeAndTargetIdOrderByTimestampDesc("Group", request.getGroupId());
            List<Post> posts = entities.stream()
                    .filter(p -> "ACTIVE".equalsIgnoreCase(p.getStatus() != null ? p.getStatus() : "ACTIVE"))
                    .map(this::mapToProtoPost)
                    .collect(Collectors.toList());
            responseObserver.onNext(GetGroupFeedResponse.newBuilder().addAllPosts(posts).build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("Failed to fetch group feed via gRPC", e);
            responseObserver.onError(io.grpc.Status.INTERNAL
                    .withDescription("Internal error: " + e.getMessage())
                    .asRuntimeException());
        }
    }

    public void getScheduledPosts(GetScheduledPostsRequest request, StreamObserver<GetScheduledPostsResponse> responseObserver) {
        log.info("gRPC: Fetching scheduled posts for targetType: {}, targetId: {}, authorId: {}",
                request.getTargetType(), request.getTargetId(), request.getAuthorId());
        try {
            List<PostEntity> entities;
            if (request.getTargetType() != null && !request.getTargetType().isEmpty()
                    && request.getTargetId() != null && !request.getTargetId().isEmpty()) {
                entities = postRepository.findByStatusAndTargetTypeAndTargetIdOrderByScheduledPublishTimeAsc(
                        "SCHEDULED", request.getTargetType(), request.getTargetId());
            } else if (request.getAuthorId() != null && !request.getAuthorId().isEmpty()) {
                entities = postRepository.findByStatusAndAuthorIdOrderByScheduledPublishTimeAsc(
                        "SCHEDULED", request.getAuthorId());
            } else {
                entities = postRepository.findAll().stream()
                        .filter(p -> "SCHEDULED".equalsIgnoreCase(p.getStatus()))
                        .collect(Collectors.toList());
            }

            List<Post> protoPosts = entities.stream()
                    .map(this::mapToProtoPost)
                    .collect(Collectors.toList());

            responseObserver.onNext(GetScheduledPostsResponse.newBuilder()
                    .addAllPosts(protoPosts)
                    .build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("Failed to fetch scheduled posts via gRPC", e);
            responseObserver.onError(io.grpc.Status.INTERNAL
                    .withDescription("Internal error: " + e.getMessage())
                    .asRuntimeException());
        }
    }

    public Post mapToProtoPost(PostEntity entity) {
        List<PostMedia> protoMediaList = new ArrayList<>();
        if (entity.getMediaJson() != null && !entity.getMediaJson().isEmpty()) {
            try {
                List<MediaItem> rawList = objectMapper.readValue(entity.getMediaJson(), new TypeReference<List<MediaItem>>() {});
                for (MediaItem item : rawList) {
                    String bgColor = item.getBackgroundColor();
                    if ((bgColor == null || bgColor.isBlank()) && rawList.size() == 1) {
                        bgColor = imageColorService.extractDominantColor(item.getSrc());
                    }
                    var mediaBuilder = PostMedia.newBuilder()
                            .setSrc(mediaUrlSigner.reconstructUrl(item.getSrc()))
                            .setAltText(item.getAltText() != null ? item.getAltText() : "")
                            .setBackgroundColor(bgColor != null ? bgColor : "");
                    if (item.getTags() != null) {
                        for (ImageTagItem t : item.getTags()) {
                            mediaBuilder.addTags(com.facebook.feed.grpc.ImageTag.newBuilder()
                                    .setId(t.getId() != null ? t.getId() : "")
                                    .setX(t.getX())
                                    .setY(t.getY())
                                    .setUserId(t.getUserId() != null ? t.getUserId() : "")
                                    .build());
                        }
                    }
                    protoMediaList.add(mediaBuilder.build());
                }
            } catch (Exception e) {
                log.error("Failed to deserialize post media JSON", e);
            }
        }

        Map<String, List<String>> reactionsMap = reactionService.getReactionDetails(entity.getId());
        List<ReactionDetail> protoReactions = new ArrayList<>();
        if (reactionsMap != null) {
            reactionsMap.forEach((type, userIds) -> {
                protoReactions.add(ReactionDetail.newBuilder()
                        .setReactionType(type)
                        .addAllUserIds(userIds)
                        .build());
            });
        }

        return Post.newBuilder()
                .setId(entity.getId())
                .setAuthorId(entity.getAuthorId())
                .setContent(entity.getContent())
                .setDate(entity.getDate())
                .setTimestamp(entity.getTimestamp())
                .addAllMedia(protoMediaList)
                .setIsAnonymous(entity.getIsAnonymous() != null ? entity.getIsAnonymous() : false)
                .setTargetId(entity.getTargetId() != null ? entity.getTargetId() : "")
                .setTargetType(entity.getTargetType() != null ? entity.getTargetType() : "")
                .addAllReactions(protoReactions)
                .setCommentCount(entity.getCommentCount() != null ? entity.getCommentCount() : 0)
                .setShareCount(entity.getShareCount() != null ? entity.getShareCount() : 0)
                .setVisibility(entity.getVisibility() != null ? entity.getVisibility() : "PUBLIC")
                .addAllAllowedUserIds(entity.getAllowedUserIds() != null ? entity.getAllowedUserIds() : List.of())
                .addAllTaggedUserIds(entity.getTaggedUserIds() != null ? entity.getTaggedUserIds() : List.of())
                .setContextJson(entity.getContextJson() != null ? entity.getContextJson() : "")
                .setStatus(entity.getStatus() != null ? entity.getStatus() : "ACTIVE")
                .setScheduledPublishTime(entity.getScheduledPublishTime() != null ? entity.getScheduledPublishTime() : 0L)
                .build();
    }

    private List<String> fetchCandidatesFromRecommendService(String userId) {
        String url = "http://recommend-service:8096/api/recommend/candidates/" + userId + "?limit=200";
        try {
            HttpRequest restRequest = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .timeout(Duration.ofMillis(800))
                    .build();

            HttpResponse<String> response = httpClient.send(restRequest, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                return objectMapper.readValue(response.body(), new TypeReference<List<String>>() {});
            }
        } catch (Exception e) {
            log.warn("Could not fetch recommendation candidates from Qdrant via recommend-service: {}", e.getMessage());
        }
        return List.of();
    }

    @SuppressWarnings("unchecked")
    private List<Post> rankPostsMultiObjective(List<PostEntity> entities, String userId) {
        if (entities.isEmpty()) {
            return List.of();
        }

        List<String> candidateIds = entities.stream()
                .map(PostEntity::getId)
                .collect(Collectors.toList());

        Map<String, Double> scoreMap = new HashMap<>();

        try {
            String url = "http://recommend-service:8096/api/recommend/rank";
            String payload = objectMapper.writeValueAsString(Map.of(
                    "userId", userId,
                    "candidates", candidateIds
            ));

            HttpRequest restRequest = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(payload))
                    .timeout(Duration.ofMillis(800))
                    .build();

            HttpResponse<String> response = httpClient.send(restRequest, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                Map<String, Object> responseMap = objectMapper.readValue(response.body(), new TypeReference<Map<String, Object>>() {});
                List<Map<String, Object>> rankedPosts = (List<Map<String, Object>>) responseMap.get("rankedPosts");
                if (rankedPosts != null) {
                    for (Map<String, Object> item : rankedPosts) {
                        String postId = (String) item.get("postId");
                        Number score = (Number) item.get("score");
                        if (postId != null && score != null) {
                            scoreMap.put(postId, score.doubleValue());
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Could not fetch AutoGluon heavy ranking scores, falling back to heuristic calculations: {}", e.getMessage());
            return rankPostsMultiObjectiveHeuristic(entities, userId);
        }

        final Map<String, Double> finalScoreMap = scoreMap;
        final long now = System.currentTimeMillis();

        class ScoredPost {
            final PostEntity entity;
            final double score;
            ScoredPost(PostEntity entity, double score) {
                this.entity = entity;
                this.score = score;
            }
        }

        List<ScoredPost> scoredList = new ArrayList<>();
        for (PostEntity entity : entities) {
            double score = finalScoreMap.getOrDefault(entity.getId(), 0.05);

            double ageHours = (double) (now - (entity.getTimestamp() != null ? entity.getTimestamp() : now)) / 3600000.0;
            if (ageHours < 0) ageHours = 0;
            double decayMultiplier = Math.exp(-0.05 * ageHours);
            score = score * decayMultiplier;

            if (ageHours < 2.0) {
                score += 2.5;
            }

            scoredList.add(new ScoredPost(entity, score));
        }

        scoredList.sort((a, b) -> Double.compare(b.score, a.score));

        return scoredList.stream()
                .map(sp -> mapToProtoPost(sp.entity))
                .collect(Collectors.toList());
    }

    @SuppressWarnings("unchecked")
    private List<Post> rankPostsMultiObjectiveHeuristic(List<PostEntity> entities, String userId) {
        List<String> topTargets = new ArrayList<>();
        Map<String, Integer> categoryCounts = new HashMap<>();
        List<String> recentSeen = new ArrayList<>();
        double totalInteractions = 0.0;
        double avgDwellTimeMs = 0.0;
        int fastSkips = 0;
        int hides = 0;

        try {
            String url = "http://recommend-service:8096/api/recommend/features/user/" + userId;
            HttpRequest restRequest = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .timeout(Duration.ofMillis(800))
                    .build();

            HttpResponse<String> response = httpClient.send(restRequest, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                Map<String, Object> featureVector = objectMapper.readValue(response.body(), new TypeReference<Map<String, Object>>() {});

                if (featureVector.containsKey("topTargets")) {
                    topTargets = (List<String>) featureVector.get("topTargets");
                }
                if (featureVector.containsKey("categoryCounts")) {
                    Map<String, Number> rawCategories = (Map<String, Number>) featureVector.get("categoryCounts");
                    for (Map.Entry<String, Number> entry : rawCategories.entrySet()) {
                        categoryCounts.put(entry.getKey().toLowerCase(), entry.getValue().intValue());
                        totalInteractions += entry.getValue().doubleValue();
                    }
                }
                if (featureVector.containsKey("avgDwellTimeMs")) {
                    avgDwellTimeMs = ((Number) featureVector.get("avgDwellTimeMs")).doubleValue();
                }
                if (featureVector.containsKey("totalFastSkips")) {
                    fastSkips = ((Number) featureVector.get("totalFastSkips")).intValue();
                }
                if (featureVector.containsKey("totalHides")) {
                    hides = ((Number) featureVector.get("totalHides")).intValue();
                }
                if (featureVector.containsKey("recentSeen")) {
                    recentSeen = (List<String>) featureVector.get("recentSeen");
                }
            }
        } catch (Exception e) {
            log.warn("Could not fetch user features for heuristic fallback ranking: {}", e.getMessage());
        }

        final List<String> finalTopTargets = topTargets;
        final Map<String, Integer> finalCategoryCounts = categoryCounts;
        final List<String> finalRecentSeen = recentSeen;
        final double finalTotalInteractions = totalInteractions > 0 ? totalInteractions : 1.0;
        final double finalAvgDwell = avgDwellTimeMs;
        final int finalSkips = fastSkips;
        final int finalHides = hides;

        class HeuristicScoredPost {
            final PostEntity entity;
            final double score;
            HeuristicScoredPost(PostEntity entity, double score) {
                this.entity = entity;
                this.score = score;
            }
        }

        List<HeuristicScoredPost> scoredList = new ArrayList<>();
        for (PostEntity entity : entities) {
            String format = getPostFormatType(entity);
            double categoryAffinity = 0.1;
            if (finalCategoryCounts.containsKey(format)) {
                categoryAffinity = (double) finalCategoryCounts.get(format) / finalTotalInteractions;
            }

            double creatorAffinity = (entity.getAuthorId() != null && finalTopTargets.contains(entity.getAuthorId())) ? 0.8 : 0.05;

            double pLike = categoryAffinity * 0.4 + creatorAffinity * 0.6;
            double pComment = categoryAffinity * 0.3 + creatorAffinity * 0.7;
            double pShare = creatorAffinity * 0.9;
            double pDwellTimeGt5s = (finalAvgDwell > 5000) ? (categoryAffinity * 0.8) : (categoryAffinity * 0.3);
            double pHide = (finalHides > 0 || finalSkips > 0) ? (0.05 + 0.1 * (finalSkips / finalTotalInteractions)) : 0.01;

            double score = (1.0 * pLike) + (2.0 * pComment) + (3.0 * pShare) + (1.5 * pDwellTimeGt5s) - (5.0 * pHide);

            boolean hasSeenRecently = finalRecentSeen.contains(entity.getId());
            boolean hasPositiveEngagement = finalTopTargets.contains(entity.getId()) || (finalAvgDwell > 5000 && finalCategoryCounts.containsKey(format));
            if (hasSeenRecently && !hasPositiveEngagement) {
                score = score * 0.3;
            }

            scoredList.add(new HeuristicScoredPost(entity, score));
        }

        scoredList.sort((a, b) -> Double.compare(b.score, a.score));

        return scoredList.stream()
                .map(sp -> mapToProtoPost(sp.entity))
                .collect(Collectors.toList());
    }

    private List<Post> diversifyFeed(List<Post> rankedPosts) {
        if (rankedPosts.size() < 3) {
            return rankedPosts;
        }

        List<Post> diversified = new ArrayList<>();
        List<Post> backlog = new ArrayList<>();

        for (Post post : rankedPosts) {
            if (diversified.isEmpty()) {
                diversified.add(post);
                continue;
            }

            String lastAuthor = diversified.get(diversified.size() - 1).getAuthorId();
            if (post.getAuthorId().equals(lastAuthor)) {
                backlog.add(post);
            } else {
                diversified.add(post);
                if (!backlog.isEmpty()) {
                    List<Post> remainingBacklog = new ArrayList<>();
                    for (Post bPost : backlog) {
                        String currentLastAuthor = diversified.get(diversified.size() - 1).getAuthorId();
                        if (!bPost.getAuthorId().equals(currentLastAuthor)) {
                            diversified.add(bPost);
                        } else {
                            remainingBacklog.add(bPost);
                        }
                    }
                    backlog = remainingBacklog;
                }
            }
        }

        diversified.addAll(backlog);
        return diversified;
    }

    private List<Post> applyEpsilonGreedyExploration(List<Post> rankedPosts, List<PostEntity> globalCandidates) {
        if (rankedPosts.size() < 4) {
            return rankedPosts;
        }

        List<Post> explored = new ArrayList<>();
        Random rand = new Random();

        List<Post> explorationPool = globalCandidates.stream()
                .filter(p -> rankedPosts.stream().noneMatch(rp -> rp.getId().equals(p.getId())))
                .map(this::mapToProtoPost)
                .collect(Collectors.toList());
        Collections.shuffle(explorationPool);

        int explorationIdx = 0;
        double epsilon = 0.10;

        for (Post post : rankedPosts) {
            if (rand.nextDouble() < epsilon && explorationIdx < explorationPool.size()) {
                explored.add(explorationPool.get(explorationIdx++));
            } else {
                explored.add(post);
            }
        }
        return explored;
    }

    private String getPostFormatType(PostEntity entity) {
        if (entity.getMediaJson() == null || entity.getMediaJson().isBlank()) {
            return "text";
        }
        String json = entity.getMediaJson().toLowerCase();
        if (json.contains("\"video\"") || json.contains("mp4") || json.contains("m3u8")) {
            return "video";
        }
        if (json.contains("\"photo\"") || json.contains("\"image\"") || json.contains("jpg") || json.contains("png") || json.contains("gif")) {
            return "photo";
        }
        return "text";
    }

    private void notifyRecommendServiceOfNewPost(PostEntity post) {
        String recommendUrl = "http://recommend-service:8096/api/recommend/posts";
        try {
            String format = getPostFormatType(post);
            String payload = objectMapper.writeValueAsString(Map.of(
                    "id", post.getId(),
                    "authorId", post.getAuthorId() != null ? post.getAuthorId() : "",
                    "content", post.getContent() != null ? post.getContent() : "",
                    "category", format,
                    "timestamp", post.getTimestamp() != null ? post.getTimestamp() : System.currentTimeMillis()
            ));
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(recommendUrl))
                    .header("Content-Type", "application/json")
                    .timeout(Duration.ofSeconds(2))
                    .POST(HttpRequest.BodyPublishers.ofString(payload))
                    .build();
            httpClient.sendAsync(request, HttpResponse.BodyHandlers.discarding())
                    .thenAccept(res -> {
                        if (res.statusCode() == 200) {
                            log.info("Successfully notified recommend-service of new post: {}", post.getId());
                        } else {
                            log.warn("recommend-service returned status code {} for new post {}", res.statusCode(), post.getId());
                        }
                    })
                    .exceptionally(ex -> {
                        log.warn("Failed to notify recommend-service of new post {}: {}", post.getId(), ex.getMessage());
                        return null;
                    });
        } catch (Exception e) {
            log.warn("Failed to serialize/notify recommend-service of new post {}: {}", post.getId(), e.getMessage());
        }
    }

    private void enqueueModeration(PostEntity post) {
        String linkGuardUrl = System.getenv().getOrDefault(
                "LINK_GUARD_SERVICE_URL", "http://linkguard-service:8086");
        try {
            String payload = objectMapper.writeValueAsString(Map.of(
                    "post_id", post.getId(),
                    "content", post.getContent() == null ? "" : post.getContent()));
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(linkGuardUrl + "/moderation/posts"))
                    .header("Content-Type", "application/json")
                    .timeout(Duration.ofSeconds(3))
                    .POST(HttpRequest.BodyPublishers.ofString(payload))
                    .build();
            HttpResponse<Void> response = httpClient.send(
                    request, HttpResponse.BodyHandlers.discarding());
            if (response.statusCode() != 202) {
                log.warn("LinkGuard did not accept moderation for post {}: HTTP {}",
                        post.getId(), response.statusCode());
            }
        } catch (Exception exception) {
            log.warn("Could not enqueue LinkGuard moderation for post {}", post.getId(), exception);
        }
    }

    private List<String> extractHashtags(String content) {
        if (content == null || content.isEmpty()) {
            return new ArrayList<>();
        }
        List<String> list = new ArrayList<>();
        java.util.regex.Matcher matcher = java.util.regex.Pattern.compile("#(\\w+)").matcher(content);
        while (matcher.find()) {
            String hashtag = matcher.group(1).toLowerCase();
            if (!list.contains(hashtag)) {
                list.add(hashtag);
            }
        }
        return list;
    }

    @lombok.Data
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class ImageTagItem {
        private String id;
        private double x;
        private double y;
        private String userId;
    }

    @lombok.Data
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class MediaItem {
        private String src;
        private String altText;
        private List<ImageTagItem> tags;
        private String backgroundColor;
    }
}
