package com.facebook.FeedService.grpc;

import com.facebook.FeedService.grpc.handler.*;
import com.facebook.feed.grpc.*;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;

@GrpcService
@RequiredArgsConstructor
@Slf4j
public class FeedGrpcServiceImpl extends FeedGrpcServiceGrpc.FeedGrpcServiceImplBase {

    private final PostGrpcHandler postGrpcHandler;
    private final StoryGrpcHandler storyGrpcHandler;
    private final EventGrpcHandler eventGrpcHandler;
    private final CommentAndReactionGrpcHandler commentAndReactionGrpcHandler;
    private final UserMediaGrpcHandler userMediaGrpcHandler;

    // --- POST & FEED METHODS ---
    @Override
    public void getFeed(GetFeedRequest request, StreamObserver<GetFeedResponse> responseObserver) {
        postGrpcHandler.getFeed(request, responseObserver);
    }

    @Override
    public void createPost(CreatePostRequest request, StreamObserver<CreatePostResponse> responseObserver) {
        postGrpcHandler.createPost(request, responseObserver);
    }

    @Override
    public void getPostById(GetPostByIdRequest request, StreamObserver<GetPostByIdResponse> responseObserver) {
        postGrpcHandler.getPostById(request, responseObserver);
    }

    @Override
    public void updatePostStatus(UpdatePostStatusRequest request, StreamObserver<UpdatePostStatusResponse> responseObserver) {
        postGrpcHandler.updatePostStatus(request, responseObserver);
    }

    @Override
    public void updatePostMediaStatus(UpdatePostMediaStatusRequest request, StreamObserver<UpdatePostMediaStatusResponse> responseObserver) {
        postGrpcHandler.updatePostMediaStatus(request, responseObserver);
    }

    @Override
    public void voteOnPoll(VoteOnPollRequest request, StreamObserver<VoteOnPollResponse> responseObserver) {
        postGrpcHandler.voteOnPoll(request, responseObserver);
    }

    @Override
    public void getGroupFeed(GetGroupFeedRequest request, StreamObserver<GetGroupFeedResponse> responseObserver) {
        postGrpcHandler.getGroupFeed(request, responseObserver);
    }

    @Override
    public void getScheduledPosts(GetScheduledPostsRequest request, StreamObserver<GetScheduledPostsResponse> responseObserver) {
        postGrpcHandler.getScheduledPosts(request, responseObserver);
    }

    // --- STORY METHODS ---
    @Override
    public void getActiveStories(GetActiveStoriesRequest request, StreamObserver<GetActiveStoriesResponse> responseObserver) {
        storyGrpcHandler.getActiveStories(request, responseObserver);
    }

    @Override
    public void createStory(CreateStoryRequest request, StreamObserver<CreateStoryResponse> responseObserver) {
        storyGrpcHandler.createStory(request, responseObserver);
    }

    @Override
    public void markStoryAsViewed(MarkStoryAsViewedRequest request, StreamObserver<MarkStoryAsViewedResponse> responseObserver) {
        storyGrpcHandler.markStoryAsViewed(request, responseObserver);
    }

    // --- EVENT METHODS ---
    @Override
    public void createEvent(CreateEventRequest request, StreamObserver<CreateEventResponse> responseObserver) {
        eventGrpcHandler.createEvent(request, responseObserver);
    }

    @Override
    public void getEventById(GetEventByIdRequest request, StreamObserver<GetEventByIdResponse> responseObserver) {
        eventGrpcHandler.getEventById(request, responseObserver);
    }

    @Override
    public void getEvents(GetEventsRequest request, StreamObserver<GetEventsResponse> responseObserver) {
        eventGrpcHandler.getEvents(request, responseObserver);
    }

    // --- COMMENT & REACTION METHODS ---
    @Override
    public void getComments(GetCommentsRequest request, StreamObserver<GetCommentsResponse> responseObserver) {
        commentAndReactionGrpcHandler.getComments(request, responseObserver);
    }

    @Override
    public void addComment(AddCommentRequest request, StreamObserver<AddCommentResponse> responseObserver) {
        commentAndReactionGrpcHandler.addComment(request, responseObserver);
    }

    @Override
    public void getPostReactions(GetPostReactionsRequest request, StreamObserver<GetPostReactionsResponse> responseObserver) {
        commentAndReactionGrpcHandler.getPostReactions(request, responseObserver);
    }

    @Override
    public void reactToPost(ReactToPostRequest request, StreamObserver<ReactToPostResponse> responseObserver) {
        commentAndReactionGrpcHandler.reactToPost(request, responseObserver);
    }

    @Override
    public void reactToComment(ReactToCommentRequest request, StreamObserver<ReactToCommentResponse> responseObserver) {
        commentAndReactionGrpcHandler.reactToComment(request, responseObserver);
    }

    // --- USER MEDIA METHODS ---
    @Override
    public void getUserMedia(GetUserMediaRequest request, StreamObserver<GetUserMediaResponse> responseObserver) {
        userMediaGrpcHandler.getUserMedia(request, responseObserver);
    }

    @Override
    public void getUserAlbums(GetUserAlbumsRequest request, StreamObserver<GetUserAlbumsResponse> responseObserver) {
        userMediaGrpcHandler.getUserAlbums(request, responseObserver);
    }
}
