package com.facebook.FeedService.grpc;

import com.facebook.FeedService.entity.PostEntity;
import com.facebook.FeedService.entity.StoryEntity;
import com.facebook.FeedService.entity.StoryViewEntity;
import com.facebook.FeedService.entity.EventEntity;
import com.facebook.FeedService.dto.ReactionRequest;
import com.facebook.FeedService.repository.PostRepository;
import com.facebook.FeedService.repository.StoryRepository;
import com.facebook.FeedService.repository.StoryViewRepository;
import com.facebook.FeedService.repository.EventRepository;
import com.facebook.FeedService.service.CommentService;
import com.facebook.FeedService.service.PostStatusService;
import com.facebook.abr.grpc.AbrGrpcServiceGrpc;
import com.facebook.abr.grpc.GetVideoInfoRequest;
import com.facebook.abr.grpc.ProcessStoryImageRequest;
import com.facebook.feed.grpc.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import net.devh.boot.grpc.client.inject.GrpcClient;

import java.time.Instant;
import java.time.Duration;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@GrpcService
@RequiredArgsConstructor
@Slf4j
public class FeedGrpcServiceImpl extends FeedGrpcServiceGrpc.FeedGrpcServiceImplBase {

    private final PostRepository postRepository;
    private final StoryRepository storyRepository;
    private final StoryViewRepository storyViewRepository;
    private final EventRepository eventRepository;
    private final com.facebook.FeedService.service.ReactionService reactionService;
    private final CommentService commentService;
    private final PostStatusService postStatusService;
    private final com.facebook.FeedService.service.ImageColorService imageColorService;
    private final com.facebook.FeedService.service.UserMediaService userMediaService;
    private final com.facebook.FeedService.scheduler.ScheduledPostPublisher scheduledPostPublisher;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(2))
            .build();

    @GrpcClient("social-graph-service")
    private com.facebook.socialgraph.grpc.SocialGraphGrpcServiceGrpc.SocialGraphGrpcServiceBlockingStub socialGraphGrpcStub;

    @GrpcClient("abr-service")
    private AbrGrpcServiceGrpc.AbrGrpcServiceBlockingStub abrGrpcStub;

    @GrpcClient("groups-service")
    private com.facebook.groups.grpc.GroupsGrpcServiceGrpc.GroupsGrpcServiceBlockingStub groupsGrpcStub;

    @org.springframework.beans.factory.annotation.Autowired
    private com.facebook.FeedService.util.MentionHelper mentionHelper;


    @Override
    public void getFeed(GetFeedRequest request, StreamObserver<GetFeedResponse> responseObserver) {
        log.info("gRPC: Fetching feed for user: {}, limit: {}, offset: {}",
                request.getCurrentUserId(), request.getLimit(), request.getOffset());

        try {
            String currentUserId = request.getCurrentUserId();
            Set<String> allowedAuthors = new HashSet<>();
            allowedAuthors.add(currentUserId);

            try {
                if (socialGraphGrpcStub != null) {
                    com.facebook.socialgraph.grpc.GetFriendsResponse friendsResponse = socialGraphGrpcStub.getFriends(
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
                // STAGE 1: Candidate Retrieval (Qdrant Vector Similarity search via recommend-service)
                List<String> candidateIds = fetchCandidatesFromRecommendService(currentUserId);
                if (candidateIds != null && !candidateIds.isEmpty()) {
                    List<PostEntity> fetched = postRepository.findAllById(candidateIds);
                    // Retain Qdrant vector similarity ranking order
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

            // Convert entities and filter by authors and visibility settings
            List<PostEntity> filteredEntities = candidates.stream()
                    .filter(post -> {
                        if (post.getAuthorId() == null) return false;

                        // Exclude group posts from the general homepage feed
                        if ("Group".equalsIgnoreCase(post.getTargetType())) {
                            return false;
                        }

                        // Exclude posts that are not active/visible (e.g. rejected, hidden, rendering, scheduled)
                        String status = post.getStatus() != null ? post.getStatus() : "ACTIVE";
                        if ("REJECTED".equalsIgnoreCase(status) || 
                            "HIDDEN".equalsIgnoreCase(status) || 
                            "NOT_VISIBLE".equalsIgnoreCase(status) || 
                            "RENDERING".equalsIgnoreCase(status) ||
                            "SCHEDULED".equalsIgnoreCase(status)) {
                            return false;
                        }

                        String authorId = post.getAuthorId();

                        // 1. Viewer is the author: always visible
                        if (currentUserId.equals(authorId)) {
                            return true;
                        }

                        String vis = post.getVisibility() != null ? post.getVisibility() : "PUBLIC";

                        // 2. If PUBLIC: visible to everyone
                        if ("PUBLIC".equalsIgnoreCase(vis)) {
                            return true;
                        }

                        // 3. If visibility is ONLY_ME: only the author can see (handled above, so hide for everyone else)
                        if ("ONLY_ME".equalsIgnoreCase(vis)) {
                            return false;
                        }

                        // 4. If visibility is SELECT_USERS or SPECIFIC_FRIENDS: viewer must be in the allowed list
                        if ("SELECT_USERS".equalsIgnoreCase(vis) || "SPECIFIC_FRIENDS".equalsIgnoreCase(vis)) {
                            return post.getAllowedUserIds() != null && post.getAllowedUserIds().contains(currentUserId);
                        }

                        // 5. For FRIENDS_ONLY / FRIENDS: visible if the author is a friend (or self)
                        if ("FRIENDS_ONLY".equalsIgnoreCase(vis) || "FRIENDS".equalsIgnoreCase(vis)) {
                            return allowedAuthors.contains(authorId);
                        }

                        // Default fallback: visible to friends
                        return allowedAuthors.contains(authorId);
                    })
                    .collect(Collectors.toList());

            // STAGE 2: Heavy Ranking & Multi-Objective scoring (weighted sum of Like/Comment/Share/DwellTime - Hide)
            List<Post> posts;
            if (request.getCurrentUserId() != null && !request.getCurrentUserId().isEmpty() && !isHashtagSearch) {
                posts = rankPostsMultiObjective(filteredEntities, request.getCurrentUserId());
            } else {
                posts = filteredEntities.stream()
                        .map(this::mapToProtoPost)
                        .collect(Collectors.toList());
            }

            // STAGE 3: Re-ranking & Business Logic (Diversification - Karuzela autorów)
            posts = diversifyFeed(posts);
            posts = applyEpsilonGreedyExploration(posts, candidates);

            // Simple Pagination
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

    @SuppressWarnings("unchecked")
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
            // Call recommend-service's AutoGluon Heavy Ranking endpoint
            String url = "http://recommend-service:8096/api/recommend/rank";
            String payload = objectMapper.writeValueAsString(java.util.Map.of(
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

            // 1. Time Decay (Half-life gravity)
            double ageHours = (double) (now - (entity.getTimestamp() != null ? entity.getTimestamp() : now)) / 3600000.0;
            if (ageHours < 0) ageHours = 0;
            double decayMultiplier = Math.exp(-0.05 * ageHours);
            score = score * decayMultiplier;

            // 2. Cold Start Boost (guaranteed impressions for new posts)
            if (ageHours < 2.0) {
                score += 2.5;
            }

            scoredList.add(new ScoredPost(entity, score));
        }

        // Sort descending by score
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
        final long now = System.currentTimeMillis();

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
                // Consecutive post by the same author - push to backlog to insert later
                backlog.add(post);
            } else {
                diversified.add(post);
                
                // Try draining backlog when author changes
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

        // Append whatever remains in backlog at the end
        diversified.addAll(backlog);
        return diversified;
    }

    private List<Post> applyEpsilonGreedyExploration(List<Post> rankedPosts, List<PostEntity> globalCandidates) {
        if (rankedPosts.size() < 4) {
            return rankedPosts;
        }
        
        List<Post> explored = new ArrayList<>();
        java.util.Random rand = new java.util.Random();
        
        // Extract posts from globalCandidates that are NOT in the current ranked list to form exploration pool
        List<Post> explorationPool = globalCandidates.stream()
                .filter(p -> rankedPosts.stream().noneMatch(rp -> rp.getId().equals(p.getId())))
                .map(this::mapToProtoPost)
                .collect(Collectors.toList());
        Collections.shuffle(explorationPool);
        
        int explorationIdx = 0;
        double epsilon = 0.10; // 10% chance to explore content outside the user interest bubble

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
            String payload = objectMapper.writeValueAsString(java.util.Map.of(
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

    @Override
    public void createPost(CreatePostRequest request, StreamObserver<CreatePostResponse> responseObserver) {
        log.info("gRPC: Creating post for user: {}", request.getAuthorId());

        try {
            // Process media: extract file ID from URLs
            List<PostMedia> processedMedia = new ArrayList<>();
            for (com.facebook.feed.grpc.PostMedia m : request.getMediaList()) {
                String fileId = extractFileId(m.getSrc());
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

            // Check if post contains a video via ABR gRPC service
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

                // Trigger mention notifications
                try {
                    mentionHelper.sendMentionNotifications(saved.getMentionedUserIds(), saved.getAuthorId(), "swoim poście");
                } catch (Exception ex) {
                    log.error("Failed to send mention notifications for post " + saved.getId(), ex);
                }

                // Atomically increment the share count of the original post if this is a share
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

    /**
     * Moderation is deliberately best-effort: a temporary LinkGuard outage must
     * not make post creation unavailable. The guard owns retrying its Celery job.
     */
    private void enqueueModeration(PostEntity post) {
        String linkGuardUrl = System.getenv().getOrDefault(
                "LINK_GUARD_SERVICE_URL", "http://linkguard-service:8086");
        try {
            String payload = objectMapper.writeValueAsString(java.util.Map.of(
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

    // Helper to map entity to proto
    private Post mapToProtoPost(PostEntity entity) {
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
                            .setSrc(reconstructUrl(item.src))
                            .setAltText(item.altText != null ? item.altText : "")
                            .setBackgroundColor(bgColor != null ? bgColor : "");
                    if (item.tags != null) {
                        for (ImageTagItem t : item.tags) {
                            mediaBuilder.addTags(com.facebook.feed.grpc.ImageTag.newBuilder()
                                    .setId(t.id != null ? t.id : "")
                                    .setX(t.x)
                                    .setY(t.y)
                                    .setUserId(t.userId != null ? t.userId : "")
                                    .build());
                        }
                    }
                    protoMediaList.add(mediaBuilder.build());
                }
            } catch (Exception e) {
                log.error("Failed to deserialize post media JSON", e);
            }
        }

        java.util.Map<String, java.util.List<String>> reactionsMap = reactionService.getReactionDetails(entity.getId());
        List<com.facebook.feed.grpc.ReactionDetail> protoReactions = new ArrayList<>();
        if (reactionsMap != null) {
            reactionsMap.forEach((type, userIds) -> {
                protoReactions.add(com.facebook.feed.grpc.ReactionDetail.newBuilder()
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

    @Override
    public void getActiveStories(GetActiveStoriesRequest request, StreamObserver<GetActiveStoriesResponse> responseObserver) {
        log.info("gRPC: Fetching active stories for user: {}", request.getCurrentUserId());
        try {
            String currentUserId = request.getCurrentUserId();
            Set<String> allowedAuthors = new HashSet<>();
            allowedAuthors.add(currentUserId);

            try {
                if (socialGraphGrpcStub != null) {
                    com.facebook.socialgraph.grpc.GetFriendsResponse friendsResponse = socialGraphGrpcStub.getFriends(
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

    @Override
    public void createStory(CreateStoryRequest request, StreamObserver<CreateStoryResponse> responseObserver) {
        log.info("gRPC: Creating story for user: {}", request.getAuthorId());
        try {
            String fileId = extractFileId(request.getMediaUrl());
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

    @Override
    public void markStoryAsViewed(MarkStoryAsViewedRequest request, StreamObserver<MarkStoryAsViewedResponse> responseObserver) {
        log.info("gRPC: Marking story {} as viewed by {}", request.getStoryId(), request.getViewerId());
        try {
            String storyId = request.getStoryId();
            String viewerId = request.getViewerId();

            java.util.Optional<StoryViewEntity> existing = storyViewRepository.findByStoryIdAndViewerId(storyId, viewerId);
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

    private Story mapToProtoStory(StoryEntity entity) {
        List<String> viewerIds = storyViewRepository.findAllByStoryId(entity.getId()).stream()
                .map(StoryViewEntity::getViewerId)
                .collect(Collectors.toList());

        Story.Builder builder = Story.newBuilder()
                .setId(entity.getId())
                .setAuthorId(entity.getAuthorId())
                .setMediaUrl(reconstructStoryUrl(entity.getMediaUrl()))
                .setMediaType(entity.getMediaType())
                .setText(entity.getText() != null ? entity.getText() : "")
                .setCreatedAt(entity.getCreatedAt().toString())
                .setExpiresAt(entity.getExpiresAt().toString())
                .addAllViewedByUserIds(viewerIds);
        if (entity.getThumbMediaUrl() != null && !entity.getThumbMediaUrl().isEmpty()) {
            builder.setThumbMediaUrl(reconstructStoryUrl(entity.getThumbMediaUrl()));
        }
        return builder.build();
    }

    private String extractFileId(String src) {
        if (src == null) return null;
        String path = src;
        if (path.contains("/media/")) {
            path = path.substring(path.lastIndexOf("/media/") + "/media/".length());
        } else if (path.contains("/files/")) {
            path = path.substring(path.lastIndexOf("/files/") + "/files/".length());
        }
        int qIdx = path.indexOf('?');
        if (qIdx != -1) path = path.substring(0, qIdx);
        int hIdx = path.indexOf('#');
        if (hIdx != -1) path = path.substring(0, hIdx);
        // tusd S3 object keys are the upload id without the "+{info}" suffix used in URLs
        int plusIdx = path.indexOf('+');
        if (plusIdx != -1) path = path.substring(0, plusIdx);
        return path;
    }

    private static final String SHARED_SECRET = "secret-media-key";

    private String signUrl(String path) {
        long expires = (System.currentTimeMillis() / 1000L) + 3600;
        String expiresStr = String.valueOf(expires);
        String signature = generateSignature(path, expiresStr, SHARED_SECRET);
        return path + "?expires=" + expiresStr + "&signature=" + signature;
    }

    private String generateSignature(String path, String expires, String secret) {
        try {
            javax.crypto.spec.SecretKeySpec signingKey = new javax.crypto.spec.SecretKeySpec(secret.getBytes(), "HmacSHA256");
            javax.crypto.Mac mac = javax.crypto.Mac.getInstance("HmacSHA256");
            mac.init(signingKey);
            byte[] rawHmac = mac.doFinal((path + expires).getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : rawHmac) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate signature", e);
        }
    }

    private String reconstructStoryUrl(String src) {
        if (src == null) return null;
        if (src.startsWith("stories/")) {
            return signUrl("/" + src);
        }
        return reconstructUrl(src);
    }

    private String reconstructUrl(String src) {
        if (src == null) return null;
        if (src.startsWith("http://") || src.startsWith("https://")) {
            return src;
        }
        // Permanent post media lives in feed-uploads and is served via /media/ (not tus /files/)
        if (src.startsWith("/media/")) {
            return signUrl(src);
        }
        if (src.startsWith("/files/")) {
            return signUrl("/media/" + src.substring("/files/".length()));
        }
        return signUrl("/media/" + src);
    }

    @lombok.Data
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    private static class ImageTagItem {
        private String id;
        private double x;
        private double y;
        private String userId;
    }

    // Helper static class for JSON serialization
    @lombok.Data
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    private static class MediaItem {
        private String src;
        private String altText;
        private List<ImageTagItem> tags;
        private String backgroundColor;
    }

    @Override
    public void getPostById(GetPostByIdRequest request, StreamObserver<GetPostByIdResponse> responseObserver) {
        log.info("gRPC: Fetching post by id: {}", request.getPostId());
        try {
            java.util.Optional<PostEntity> entityOpt = postRepository.findById(request.getPostId());
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

    private Event mapToProtoEvent(EventEntity entity) {
        return Event.newBuilder()
                .setId(entity.getId() != null ? entity.getId() : "")
                .setUserId(entity.getUserId() != null ? entity.getUserId() : "")
                .setName(entity.getName() != null ? entity.getName() : "")
                .setTitle(entity.getTitle() != null ? entity.getTitle() : "")
                .setStartDate(entity.getStartDate() != null ? entity.getStartDate() : "")
                .setStartTime(entity.getStartTime() != null ? entity.getStartTime() : "")
                .setEndDate(entity.getEndDate() != null ? entity.getEndDate() : "")
                .setEndTime(entity.getEndTime() != null ? entity.getEndTime() : "")
                .setType(entity.getType() != null ? entity.getType() : "")
                .setPrivacy(entity.getPrivacy() != null ? entity.getPrivacy() : "")
                .setDescription(entity.getDescription() != null ? entity.getDescription() : "")
                .addAllImages(entity.getImages() != null ? entity.getImages() : Collections.emptyList())
                .setLocation(entity.getLocation() != null ? entity.getLocation() : "")
                .setLocationName(entity.getLocationName() != null ? entity.getLocationName() : "")
                .setAddress(entity.getAddress() != null ? entity.getAddress() : "")
                .setShowGuestList(entity.getShowGuestList() != null ? entity.getShowGuestList() : true)
                .addAllHosts(entity.getHosts() != null ? entity.getHosts() : Collections.emptyList())
                .setDate(entity.getDate() != null ? entity.getDate() : "")
                .setResponses(entity.getResponses() != null ? entity.getResponses() : 0)
                .setGuestsGoing(entity.getGuestsGoing() != null ? entity.getGuestsGoing() : 0)
                .setGuestsInterested(entity.getGuestsInterested() != null ? entity.getGuestsInterested() : 0)
                .addAllCoordinates(entity.getCoordinates() != null ? entity.getCoordinates() : Collections.emptyList())
                .setFrequency(entity.getFrequency() != null ? entity.getFrequency() : "")
                .build();
    }

    private void syncEventToSearchService(EventEntity event) {
        String searchServiceUrl = System.getenv().getOrDefault("SEARCH_SERVICE_URL", "http://search-service:8088");
        try {
            java.util.Map<String, Object> payload = new java.util.HashMap<>();
            payload.put("id", event.getId());
            payload.put("userId", event.getUserId());
            payload.put("name", event.getName());
            payload.put("title", event.getTitle());
            payload.put("startDate", event.getStartDate());
            payload.put("startTime", event.getStartTime());
            payload.put("endDate", event.getEndDate());
            payload.put("endTime", event.getEndTime());
            payload.put("type", event.getType());
            payload.put("privacy", event.getPrivacy());
            payload.put("description", event.getDescription());
            payload.put("location", event.getLocation());
            payload.put("locationName", event.getLocationName());
            payload.put("address", event.getAddress());
            payload.put("showGuestList", event.getShowGuestList());
            payload.put("date", event.getDate());
            payload.put("frequency", event.getFrequency());
            payload.put("images", event.getImages());

            String jsonPayload = objectMapper.writeValueAsString(payload);
            HttpRequest searchReq = HttpRequest.newBuilder()
                    .uri(URI.create(searchServiceUrl + "/api/search/events/index"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                    .timeout(Duration.ofSeconds(2))
                    .build();
            HttpResponse<String> response = httpClient.send(searchReq, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                log.info("Successfully synced event {} to search-service", event.getId());
            } else {
                log.error("Failed to sync event to search-service, status: {}", response.statusCode());
            }
        } catch (Exception e) {
            log.error("Failed to sync event to search-service", e);
        }
    }

    @Override
    public void createEvent(CreateEventRequest request, StreamObserver<CreateEventResponse> responseObserver) {
        log.info("gRPC: Creating event: {}", request.getName());
        try {
            String eventId = request.getId();
            if (eventId == null || eventId.trim().isEmpty()) {
                eventId = UUID.randomUUID().toString();
            }

            EventEntity entity = EventEntity.builder()
                    .id(eventId)
                    .userId(request.getUserId())
                    .name(request.getName())
                    .title(request.getTitle())
                    .startDate(request.getStartDate())
                    .startTime(request.getStartTime())
                    .endDate(request.getEndDate())
                    .endTime(request.getEndTime())
                    .type(request.getType())
                    .privacy(request.getPrivacy())
                    .description(request.getDescription())
                    .images(request.getImagesList() != null ? new ArrayList<>(request.getImagesList()) : new ArrayList<>())
                    .location(request.getLocation())
                    .locationName(request.getLocationName())
                    .address(request.getAddress())
                    .showGuestList(request.getShowGuestList())
                    .hosts(request.getHostsList() != null ? new ArrayList<>(request.getHostsList()) : new ArrayList<>())
                    .date(request.getDate())
                    .coordinates(request.getCoordinatesList() != null ? new ArrayList<>(request.getCoordinatesList()) : new ArrayList<>())
                    .frequency(request.getFrequency())
                    .responses(0)
                    .guestsGoing(0)
                    .guestsInterested(0)
                    .build();

            EventEntity saved = eventRepository.save(entity);

            // Sync to search service
            syncEventToSearchService(saved);

            Event protoEvent = mapToProtoEvent(saved);
            responseObserver.onNext(CreateEventResponse.newBuilder().setEvent(protoEvent).build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("Failed to create event", e);
            responseObserver.onError(io.grpc.Status.INTERNAL
                    .withDescription("Internal error: " + e.getMessage())
                    .asRuntimeException());
        }
    }

    @Override
    public void getEventById(GetEventByIdRequest request, StreamObserver<GetEventByIdResponse> responseObserver) {
        log.info("gRPC: Fetching event by id: {}", request.getId());
        try {
            java.util.Optional<EventEntity> entityOpt = eventRepository.findById(request.getId());
            if (entityOpt.isPresent()) {
                Event protoEvent = mapToProtoEvent(entityOpt.get());
                responseObserver.onNext(GetEventByIdResponse.newBuilder().setEvent(protoEvent).build());
            } else {
                responseObserver.onNext(GetEventByIdResponse.newBuilder().build());
            }
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("Failed to get event by id", e);
            responseObserver.onError(io.grpc.Status.INTERNAL
                    .withDescription("Internal error: " + e.getMessage())
                    .asRuntimeException());
        }
    }

    @Override
    public void getEvents(GetEventsRequest request, StreamObserver<GetEventsResponse> responseObserver) {
        log.info("gRPC: Fetching events");
        try {
            List<EventEntity> entities = eventRepository.findAll();
            List<Event> protoEvents = entities.stream()
                    .map(this::mapToProtoEvent)
                    .collect(Collectors.toList());

            responseObserver.onNext(GetEventsResponse.newBuilder().addAllEvents(protoEvents).build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("Failed to get events", e);
            responseObserver.onError(io.grpc.Status.INTERNAL
                    .withDescription("Internal error: " + e.getMessage())
                    .asRuntimeException());
        }
    }

    @Override
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

    @Override
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

    @Override
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

    @Override
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

    @Override
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

    @Override
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

    @Override
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

    @Override
    public void voteOnPoll(VoteOnPollRequest request, StreamObserver<VoteOnPollResponse> responseObserver) {
        log.info("gRPC: Voting on poll for post: {}, option: {}, user: {}", 
                request.getPostId(), request.getOptionId(), request.getUserId());
        try {
            java.util.Optional<PostEntity> entityOpt = postRepository.findById(request.getPostId());
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

            Map<String, Object> context = objectMapper.readValue(contextJson, new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>() {});
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
                                votes = new ArrayList<>(votes); // Ensure mutable
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

    @Override
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

    @Override
    public void getUserMedia(GetUserMediaRequest request, StreamObserver<GetUserMediaResponse> responseObserver) {
        try {
            var page = userMediaService.getUserMedia(
                    request.getUserId(),
                    request.getFilter(),
                    request.getAlbumName(),
                    request.getLimit() > 0 ? request.getLimit() : 20,
                    request.getOffset()
            );

            GetUserMediaResponse.Builder responseBuilder = GetUserMediaResponse.newBuilder()
                    .setTotalCount((int) page.totalCount())
                    .setHasMore(page.hasMore());

            for (var item : page.items()) {
                UserMediaItemDto.Builder itemDto = UserMediaItemDto.newBuilder()
                        .setId(item.getId() != null ? item.getId() : 0)
                        .setUserId(item.getUserId() != null ? item.getUserId() : "")
                        .setPostId(item.getPostId() != null ? item.getPostId() : "")
                        .setMediaUrl(item.getMediaUrl() != null ? item.getMediaUrl() : "")
                        .setMediaType(item.getMediaType() != null ? item.getMediaType() : "IMAGE")
                        .setAlbumName(item.getAlbumName() != null ? item.getAlbumName() : "")
                        .setAltText(item.getAltText() != null ? item.getAltText() : "")
                        .setCreatedAt(item.getCreatedAt() != null ? item.getCreatedAt().toString() : "")
                        .setTimestamp(item.getTimestamp() != null ? item.getTimestamp() : 0);

                responseBuilder.addItems(itemDto.build());
            }

            responseObserver.onNext(responseBuilder.build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("Failed to fetch user media for user {}", request.getUserId(), e);
            responseObserver.onError(io.grpc.Status.INTERNAL.withDescription(e.getMessage()).asRuntimeException());
        }
    }

    @Override
    public void getUserAlbums(GetUserAlbumsRequest request, StreamObserver<GetUserAlbumsResponse> responseObserver) {
        try {
            var albums = userMediaService.getUserAlbums(request.getUserId());
            GetUserAlbumsResponse.Builder responseBuilder = GetUserAlbumsResponse.newBuilder();

            for (var a : albums) {
                responseBuilder.addAlbums(UserAlbumDto.newBuilder()
                        .setName(a.name())
                        .setCount((int) a.count())
                        .setCoverUrl(a.coverUrl() != null ? a.coverUrl() : "")
                        .build());
            }

            responseObserver.onNext(responseBuilder.build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("Failed to fetch user albums for user {}", request.getUserId(), e);
            responseObserver.onError(io.grpc.Status.INTERNAL.withDescription(e.getMessage()).asRuntimeException());
        }
    }

    @Override
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
}
