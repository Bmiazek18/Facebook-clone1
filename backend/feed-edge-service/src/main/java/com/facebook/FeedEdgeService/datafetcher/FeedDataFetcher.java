package com.facebook.FeedEdgeService.datafetcher;

import com.facebook.feed.grpc.*;
// Importy typów wygenerowanych przez DGS Codegen ze schematu GraphQL
import com.facebook.FeedEdgeService.codegen.types.CreateEventInput;
import com.facebook.FeedEdgeService.codegen.types.CreatePostInput;
import com.facebook.FeedEdgeService.codegen.types.CreateStoryInput;
import com.facebook.FeedEdgeService.codegen.types.Event;
import com.facebook.FeedEdgeService.codegen.types.Post;
import com.facebook.FeedEdgeService.codegen.types.PostMediaInput;
import com.facebook.FeedEdgeService.codegen.types.ReactionDetail;
import com.facebook.FeedEdgeService.codegen.types.ImageTagInput;
import com.facebook.FeedEdgeService.codegen.types.ReactionUser;
import com.facebook.FeedEdgeService.codegen.types.Story;

import com.facebook.FeedEdgeService.mapper.FeedMapper;
import com.facebook.search.grpc.SearchEventsRequest;
import com.facebook.search.grpc.SearchGrpcServiceGrpc;
import com.netflix.graphql.dgs.*;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@DgsComponent
public class FeedDataFetcher {

    private static final Logger log = LoggerFactory.getLogger(FeedDataFetcher.class);

    @GrpcClient("feed-service")
    private FeedGrpcServiceGrpc.FeedGrpcServiceBlockingStub feedGrpcStub;

    @GrpcClient("user-service")
    private com.facebook.user.grpc.UserGrpcServiceGrpc.UserGrpcServiceBlockingStub userGrpcStub;

    @GrpcClient("search-service")
    private SearchGrpcServiceGrpc.SearchGrpcServiceBlockingStub searchGrpcStub;

    @GrpcClient("groups-service")
    private com.facebook.groups.grpc.GroupsGrpcServiceGrpc.GroupsGrpcServiceBlockingStub groupsGrpcStub;

    private final FeedMapper feedMapper;
    private final com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();

    // Wstrzyknięcie mappera MapStruct
    public FeedDataFetcher(FeedMapper feedMapper) {
        this.feedMapper = feedMapper;
    }

    @DgsQuery
    public List<Post> getFeed(
            @InputArgument String currentUserId,
            @InputArgument Integer limit,
            @InputArgument Integer offset,
            @InputArgument String hashtag) {
        log.info("Edge: Fetching feed via gRPC for user: {}, limit: {}, offset: {}, hashtag: {}", currentUserId, limit, offset, hashtag);

        try {
            var builder = com.facebook.feed.grpc.GetFeedRequest.newBuilder()
                    .setCurrentUserId(currentUserId)
                    .setLimit(limit != null ? limit : 10)
                    .setOffset(offset != null ? offset : 0);
            if (hashtag != null && !hashtag.isEmpty()) {
                builder.setHashtag(hashtag);
            }
            com.facebook.feed.grpc.GetFeedRequest request = builder.build();

            com.facebook.feed.grpc.GetFeedResponse response = feedGrpcStub.getFeed(request);
            if (response.getPostsList() == null) {
                return List.of();
            }
            return response.getPostsList().stream()
                    .map(this::enrichPost)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Failed to fetch feed via gRPC", e);
            throw new RuntimeException("Feed core service unavailable: " + e.getMessage());
        }
    }

    @DgsQuery
    public List<Post> getGroupFeed(
            @InputArgument String groupId,
            @InputArgument Integer limit,
            @InputArgument Integer offset) {
        log.info("Edge: Fetching group feed via gRPC for group: {}, limit: {}, offset: {}", groupId, limit, offset);
        try {
            var response = feedGrpcStub.getGroupFeed(com.facebook.feed.grpc.GetGroupFeedRequest.newBuilder()
                    .setGroupId(groupId)
                    .setLimit(limit != null ? limit : 10)
                    .setOffset(offset != null ? offset : 0)
                    .build());
            return response.getPostsList().stream()
                    .map(this::enrichPost)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Failed to fetch group feed", e);
            throw new RuntimeException("Feed core service unavailable: " + e.getMessage());
        }
    }

    @DgsMutation
    public Post createPost(@InputArgument CreatePostInput input) {
        log.info("Edge: Creating post via gRPC for user: {}", input.getAuthorId());

        List<com.facebook.feed.grpc.PostMedia> protoMediaList = new ArrayList<>();
        if (input.getMedia() != null) {
            for (PostMediaInput m : input.getMedia()) {
                var mediaBuilder = com.facebook.feed.grpc.PostMedia.newBuilder()
                        .setSrc(m.getSrc())
                        .setAltText(m.getAltText() != null ? m.getAltText() : "");
                if (m.getBackgroundColor() != null) {
                    mediaBuilder.setBackgroundColor(m.getBackgroundColor());
                }
                if (m.getTags() != null) {
                    for (ImageTagInput tag : m.getTags()) {
                        mediaBuilder.addTags(com.facebook.feed.grpc.ImageTag.newBuilder()
                                .setId(tag.getId() != null ? tag.getId() : "")
                                .setX(tag.getX())
                                .setY(tag.getY())
                                .setUserId(tag.getUserId() != null ? tag.getUserId() : "")
                                .build());
                    }
                }
                protoMediaList.add(mediaBuilder.build());
            }
        }

        try {
            com.facebook.feed.grpc.CreatePostRequest.Builder reqBuilder = com.facebook.feed.grpc.CreatePostRequest.newBuilder()
                    .setContent(input.getContent())
                    .setAuthorId(input.getAuthorId())
                    .addAllMedia(protoMediaList)
                    .setIsAnonymous(input.getIsAnonymous() != null ? input.getIsAnonymous() : false)
                    .setTargetId(input.getTargetId() != null ? input.getTargetId() : "")
                    .setTargetType(input.getTargetType() != null ? input.getTargetType() : "");

            if (input.getVisibility() != null) {
                reqBuilder.setVisibility(input.getVisibility());
            }
            if (input.getAllowedUserIds() != null) {
                reqBuilder.addAllAllowedUserIds(input.getAllowedUserIds());
            }
            if (input.getTaggedUsersIds() != null) {
                reqBuilder.addAllTaggedUserIds(input.getTaggedUsersIds());
            }
            if (input.getContext() != null) {
                try {
                    reqBuilder.setContextJson(objectMapper.writeValueAsString(input.getContext()));
                } catch (Exception e) {
                    log.error("Failed to serialize post context to JSON", e);
                }
            }
            if (input.getScheduledPublishTime() != null) {
                reqBuilder.setScheduledPublishTime((long) input.getScheduledPublishTime().doubleValue());
            }

            CreatePostResponse response = feedGrpcStub.createPost(reqBuilder.build());
            return enrichPost(response.getPost());
        } catch (Exception e) {
            log.error("Failed to create post", e);
            throw new RuntimeException("Feed core service unavailable: " + e.getMessage());
        }
    }

    @DgsMutation
    public Post voteOnPoll(
            @InputArgument String postId,
            @InputArgument String optionId,
            @InputArgument String userId) {
        log.info("Edge: Voting on poll for post {} option {} by user {}", postId, optionId, userId);
        try {
            var response = feedGrpcStub.voteOnPoll(com.facebook.feed.grpc.VoteOnPollRequest.newBuilder()
                    .setPostId(postId)
                    .setOptionId(optionId)
                    .setUserId(userId)
                    .build());
            return enrichPost(response.getPost());
        } catch (Exception e) {
            log.error("Failed to vote on poll", e);
            throw new RuntimeException("Feed core service unavailable: " + e.getMessage());
        }
    }

    @DgsMutation
    public Boolean markStoryAsViewed(@InputArgument String storyId, @InputArgument String viewerId) {
        log.info("Edge: Marking story {} as viewed by viewer {}", storyId, viewerId);
        try {
            MarkStoryAsViewedResponse response = feedGrpcStub.markStoryAsViewed(MarkStoryAsViewedRequest.newBuilder()
                    .setStoryId(storyId)
                    .setViewerId(viewerId)
                    .build());
            return response.getSuccess();
        } catch (Exception e) {
            log.error("Failed to mark story as viewed", e);
            return false;
        }
    }

    @DgsQuery
    public List<Story> getActiveStories(@InputArgument String currentUserId) {
        log.info("Edge: Fetching active stories via gRPC for user: {}", currentUserId);
        try {
            GetActiveStoriesResponse response = feedGrpcStub.getActiveStories(GetActiveStoriesRequest.newBuilder()
                    .setCurrentUserId(currentUserId)
                    .build());

            return response.getStoriesList().stream()
                    .map(feedMapper::mapToStory)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Failed to fetch active stories", e);
            throw new RuntimeException("Feed core service unavailable: " + e.getMessage());
        }
    }

    @DgsMutation
    public Story createStory(@InputArgument CreateStoryInput input) {
        log.info("Edge: Creating story via gRPC for user: {}", input.getAuthorId());
        try {
            CreateStoryResponse response = feedGrpcStub.createStory(CreateStoryRequest.newBuilder()
                    .setAuthorId(input.getAuthorId())
                    .setMediaUrl(input.getMediaUrl())
                    .setMediaType(input.getMediaType())
                    .setText(input.getText() != null ? input.getText() : "")
                    .build());

            return feedMapper.mapToStory(response.getStory());
        } catch (Exception e) {
            log.error("Failed to create story", e);
            throw new RuntimeException("Feed core service unavailable: " + e.getMessage());
        }
    }

    @DgsData(parentType = "Post", field = "detectedLanguage")
    public String getDetectedLanguage(DgsDataFetchingEnvironment dfe) {
        Post post = dfe.getSource();
        if (post == null || post.getContent() == null || post.getContent().isEmpty()) {
            return "en";
        }
        try {
            log.info("Edge (Feed): Resolving detectedLanguage for post {} via user-service gRPC", post.getId());
            var res = userGrpcStub.detectLanguage(com.facebook.user.grpc.DetectLanguageRequest.newBuilder()
                    .setText(post.getContent())
                    .build());
            return res.getLanguageCode();
        } catch (Exception e) {
            log.error("Failed to resolve detectedLanguage via user-service", e);
            return "en";
        }
    }

    @DgsMutation
    public Event createEvent(@InputArgument CreateEventInput input) {
        log.info("Edge: Creating event via gRPC for user: {}", input.getUserId());
        try {
            // Używamy gotowej metody z mappera, która bezpiecznie buduje obiekt gRPC
            CreateEventRequest request = feedMapper.buildCreateEventRequest(input);

            CreateEventResponse response = feedGrpcStub.createEvent(request);
            return feedMapper.mapToEvent(response.getEvent());
        } catch (Exception e) {
            log.error("Failed to create event", e);
            throw new RuntimeException("Feed core service unavailable: " + e.getMessage());
        }
    }

    @DgsQuery
    public Event getEventById(@InputArgument String id) {
        log.info("Edge: Fetching event by id: {}", id);
        try {
            GetEventByIdResponse response = feedGrpcStub.getEventById(GetEventByIdRequest.newBuilder()
                    .setId(id)
                    .build());
            if (response.hasEvent()) {
                return feedMapper.mapToEvent(response.getEvent());
            }
            return null;
        } catch (Exception e) {
            log.error("Failed to fetch event by id", e);
            throw new RuntimeException("Feed core service unavailable: " + e.getMessage());
        }
    }

    @DgsQuery
    public List<Event> getEvents(@InputArgument Integer limit, @InputArgument Integer offset) {
        log.info("Edge: Fetching events");
        try {
            GetEventsResponse response = feedGrpcStub.getEvents(GetEventsRequest.newBuilder()
                    .setLimit(limit != null ? limit : 0)
                    .setOffset(offset != null ? offset : 0)
                    .build());
            return response.getEventsList().stream()
                    .map(feedMapper::mapToEvent)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Failed to fetch events", e);
            throw new RuntimeException("Feed core service unavailable: " + e.getMessage());
        }
    }

    @DgsQuery
    public List<Event> searchEvents(@InputArgument String query) {
        log.info("Edge: Searching events with query via gRPC: {}", query);
        try {
            var response = searchGrpcStub.searchEvents(SearchEventsRequest.newBuilder()
                    .setQuery(query != null ? query : "")
                    .build());
            return response.getEventsList().stream()
                    .map(hit -> {
                        Event event = new Event();
                        event.setId(hit.getId());
                        event.setUserId(hit.getUserId());
                        event.setName(hit.getName());
                        event.setTitle(hit.getTitle());
                        event.setStartDate(hit.getStartDate());
                        event.setStartTime(hit.getStartTime());
                        event.setEndDate(hit.getEndDate());
                        event.setEndTime(hit.getEndTime());
                        event.setType(hit.getType());
                        event.setPrivacy(hit.getPrivacy());
                        event.setDescription(hit.getDescription());
                        event.setLocation(hit.getLocation());
                        event.setLocationName(hit.getLocationName());
                        event.setAddress(hit.getAddress());
                        event.setShowGuestList(hit.getShowGuestList());
                        event.setDate(hit.getDate());
                        event.setFrequency(hit.getFrequency());
                        event.setImages(hit.getImagesList());
                        return event;
                    })
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Failed to search events via search-service gRPC", e);
            throw new RuntimeException("Search service unavailable: " + e.getMessage());
        }
    }

    // --- Metody pomocnicze ---

    private Post enrichPost(com.facebook.feed.grpc.Post grpcPost) {
        Post post = feedMapper.mapToPost(grpcPost);

        List<ReactionDetail> reactionsList = grpcPost.getReactionsList().stream()
                .map(r -> {
                    List<String> uIds = r.getUserIdsList().stream().map(String::valueOf).collect(Collectors.toList());
                    ReactionDetail rd = new ReactionDetail();
                    rd.setReactionType(r.getReactionType());
                    rd.setUserIds(uIds);
                    rd.setUsers(fetchReactionUsers(uIds));
                    return rd;
                })
                .collect(Collectors.toList());
        post.setReactions(reactionsList);

        // Fetch tagged users
        List<String> taggedIds = grpcPost.getTaggedUserIdsList().stream().map(String::valueOf).collect(Collectors.toList());
        post.setTaggedUsers(fetchReactionUsers(taggedIds));

        // Fetch media photo tags
        if (grpcPost.getMediaCount() > 0 && post.getMedia() != null) {
            for (int i = 0; i < grpcPost.getMediaCount(); i++) {
                com.facebook.feed.grpc.PostMedia grpcMedia = grpcPost.getMedia(i);
                com.facebook.FeedEdgeService.codegen.types.PostMedia dgsMedia = post.getMedia().get(i);
                if (grpcMedia.getTagsCount() > 0) {
                    List<com.facebook.FeedEdgeService.codegen.types.ImageTagType> dgsTags = new ArrayList<>();
                    for (com.facebook.feed.grpc.ImageTag gt : grpcMedia.getTagsList()) {
                        com.facebook.FeedEdgeService.codegen.types.ImageTagType dt = new com.facebook.FeedEdgeService.codegen.types.ImageTagType();
                        dt.setId(gt.getId());
                        dt.setX(gt.getX());
                        dt.setY(gt.getY());
                        dt.setUserId(gt.getUserId());
                        if (gt.getUserId() != null && !gt.getUserId().isEmpty()) {
                            List<ReactionUser> singleUserList = fetchReactionUsers(List.of(gt.getUserId()));
                            if (!singleUserList.isEmpty()) {
                                dt.setUser(singleUserList.get(0));
                            }
                        }
                        dgsTags.add(dt);
                    }
                    dgsMedia.setTags(dgsTags);
                }
            }
        }

        if ("post".equalsIgnoreCase(grpcPost.getTargetType())) {
            if (!grpcPost.getTargetId().trim().isEmpty()) {
                try {
                    GetPostByIdResponse response = feedGrpcStub.getPostById(
                            GetPostByIdRequest.newBuilder()
                                     .setPostId(grpcPost.getTargetId())
                                     .build()
                    );
                    if (response != null && response.hasPost()) {
                        post.setSharedPost(enrichPost(response.getPost()));
                    }
                } catch (Exception e) {
                    log.error("Failed to fetch shared post {}", grpcPost.getTargetId(), e);
                }
            }
        }

        if (grpcPost.getContextJson() != null && !grpcPost.getContextJson().isEmpty()) {
            try {
                com.facebook.FeedEdgeService.codegen.types.PostContext ctx = objectMapper.readValue(
                        grpcPost.getContextJson(),
                        com.facebook.FeedEdgeService.codegen.types.PostContext.class
                );
                post.setContext(ctx);
            } catch (Exception e) {
                log.error("Failed to deserialize post context JSON", e);
            }
        }

        post.setStatus(grpcPost.getStatus());
        post.setScheduledPublishTime((double) grpcPost.getScheduledPublishTime());

        if (("Group".equalsIgnoreCase(grpcPost.getTargetType()) || "group".equalsIgnoreCase(grpcPost.getTargetType()))
                && grpcPost.getTargetId() != null && !grpcPost.getTargetId().trim().isEmpty()
                && grpcPost.getAuthorId() != null && !grpcPost.getAuthorId().trim().isEmpty()) {
            try {
                if (groupsGrpcStub != null) {
                    var memberRes = groupsGrpcStub.getGroupMembership(
                            com.facebook.groups.grpc.GetGroupMembershipRequest.newBuilder()
                                    .setGroupId(grpcPost.getTargetId())
                                    .setUserId(grpcPost.getAuthorId())
                                    .build()
                    );
                    if (memberRes != null && memberRes.getRole() != null && !memberRes.getRole().isEmpty()) {
                        post.setAuthorGroupRole(memberRes.getRole());
                    }
                }
            } catch (Exception e) {
                log.warn("Failed to fetch author group role for post {}: {}", grpcPost.getId(), e.getMessage());
            }
        }

        return post;
    }

    private List<ReactionUser> fetchReactionUsers(List<String> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return Collections.emptyList();
        }

        List<ReactionUser> list = new ArrayList<>();
        for (String id : userIds) {
            try {
                com.facebook.user.grpc.GetUserByIdResponse response = userGrpcStub.getUserById(
                        com.facebook.user.grpc.GetUserByIdRequest.newBuilder().setUserId(id).build()
                );
                var u = response.getUser();

                ReactionUser reactionUser = new ReactionUser();
                reactionUser.setId(u.getId());
                reactionUser.setFirstName(u.getFirstName());
                reactionUser.setLastName(u.getLastName());
                reactionUser.setAvatarId(u.getAvatarId());

                String avatarUrl = "";
                if (u.getAvatarId() != null && !u.getAvatarId().isEmpty()) {
                    try {
                        var mediaRes = userGrpcStub.resolveMediaUrl(com.facebook.user.grpc.ResolveMediaUrlRequest.newBuilder()
                                .setReference(u.getAvatarId())
                                .build());
                        avatarUrl = mediaRes.getPresignedUrl();
                    } catch (Exception e) {
                        log.error("Failed to resolve avatar for reaction user: {}", id, e);
                    }
                }
                reactionUser.setAvatar(avatarUrl);
                list.add(reactionUser);
            } catch (Exception e) {
                log.error("Failed to fetch reaction user details via gRPC for id: {}", id, e);
            }
        }
        return list;
    }

    @DgsQuery
    public Post getPostById(@InputArgument String postId) {
        log.info("Edge: Fetching post by ID: {}", postId);
        try {
            GetPostByIdResponse response = feedGrpcStub.getPostById(GetPostByIdRequest.newBuilder()
                    .setPostId(postId)
                    .build());
            if (response != null && response.hasPost()) {
                return enrichPost(response.getPost());
            }
            return null;
        } catch (Exception e) {
            log.error("Failed to fetch post by ID: {}", postId, e);
            throw new RuntimeException("Feed core service unavailable: " + e.getMessage());
        }
    }

    @DgsQuery
    public List<Post> getPageScheduledPosts(@InputArgument String pageId) {
        log.info("Edge: Fetching scheduled posts for page: {}", pageId);
        try {
            var response = feedGrpcStub.getScheduledPosts(com.facebook.feed.grpc.GetScheduledPostsRequest.newBuilder()
                    .setTargetType("page")
                    .setTargetId(pageId)
                    .build());
            return response.getPostsList().stream()
                    .map(this::enrichPost)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Failed to fetch scheduled posts for page {}", pageId, e);
            return List.of();
        }
    }

    @DgsQuery
    public List<Post> getScheduledPosts(
            @InputArgument String targetType,
            @InputArgument String targetId,
            @InputArgument String authorId) {
        log.info("Edge: Fetching scheduled posts for targetType: {}, targetId: {}, authorId: {}", targetType, targetId, authorId);
        try {
            var reqBuilder = com.facebook.feed.grpc.GetScheduledPostsRequest.newBuilder();
            if (targetType != null) reqBuilder.setTargetType(targetType);
            if (targetId != null) reqBuilder.setTargetId(targetId);
            if (authorId != null) reqBuilder.setAuthorId(authorId);

            var response = feedGrpcStub.getScheduledPosts(reqBuilder.build());
            return response.getPostsList().stream()
                    .map(this::enrichPost)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Failed to fetch scheduled posts", e);
            return List.of();
        }
    }
}