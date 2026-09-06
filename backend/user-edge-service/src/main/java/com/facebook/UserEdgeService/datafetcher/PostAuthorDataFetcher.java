package com.facebook.UserEdgeService.datafetcher;

import com.facebook.UserEdgeService.mapper.EdgeMapper;
import com.facebook.user.generated.DgsConstants;
import com.facebook.user.generated.types.Comment;
import com.facebook.user.generated.types.Post;
import com.facebook.user.generated.types.Story;
import com.facebook.user.generated.types.UserSearchResponse;

import com.facebook.user.grpc.GetUserByIdRequest;
import com.facebook.user.grpc.GetUserByIdResponse;
import com.facebook.user.grpc.UserGrpcServiceGrpc;
import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsData;
import com.netflix.graphql.dgs.DgsDataFetchingEnvironment;
import com.netflix.graphql.dgs.DgsEntityFetcher;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.dataloader.DataLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryRegistry;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@DgsComponent
public class PostAuthorDataFetcher {

    private static final Logger log = LoggerFactory.getLogger(PostAuthorDataFetcher.class);

    @GrpcClient("user-service")
    private UserGrpcServiceGrpc.UserGrpcServiceBlockingStub userGrpcStub;

    private final EdgeMapper edgeMapper;
    private final CircuitBreakerRegistry circuitBreakerRegistry;
    private final RetryRegistry retryRegistry;
    private final io.github.resilience4j.bulkhead.BulkheadRegistry bulkheadRegistry;

    public PostAuthorDataFetcher(EdgeMapper edgeMapper,
                                 CircuitBreakerRegistry circuitBreakerRegistry,
                                 RetryRegistry retryRegistry,
                                 io.github.resilience4j.bulkhead.BulkheadRegistry bulkheadRegistry) {
        this.edgeMapper = edgeMapper;
        this.circuitBreakerRegistry = circuitBreakerRegistry;
        this.retryRegistry = retryRegistry;
        this.bulkheadRegistry = bulkheadRegistry;
    }

    @DgsEntityFetcher(name = DgsConstants.POST.TYPE_NAME)
    public Post resolvePost(Map<String, Object> representation) {
        Post p = new Post();
        p.setId((String) representation.get("id"));
        p.setAuthorId((String) representation.get("authorId"));
        return p;
    }

    @DgsEntityFetcher(name = DgsConstants.COMMENT.TYPE_NAME)
    public Comment resolveComment(Map<String, Object> representation) {
        Comment c = new Comment();
        c.setId((String) representation.get("id"));
        c.setUserId((String) representation.get("userId"));
        return c;
    }

    @DgsEntityFetcher(name = DgsConstants.STORY.TYPE_NAME)
    public Story resolveStory(Map<String, Object> representation) {
        Story s = new Story();
        s.setId((String) representation.get("id"));
        s.setAuthorId((String) representation.get("authorId"));
        return s;
    }

    @DgsData(parentType = DgsConstants.POST.TYPE_NAME, field = "author")
    public CompletableFuture<UserSearchResponse> getAuthor(DgsDataFetchingEnvironment dfe) {
        Post post = dfe.getSource();
        if (post == null || post.getAuthorId() == null || post.getAuthorId().isEmpty()) {
            return CompletableFuture.completedFuture(null);
        }

        DataLoader<String, UserSearchResponse> dataLoader = dfe.getDataLoader("userDataLoader");
        if (dataLoader != null) {
            return dataLoader.load(post.getAuthorId());
        }
        return CompletableFuture.completedFuture(fetchUser(post.getAuthorId()));
    }

    @DgsData(parentType = DgsConstants.COMMENT.TYPE_NAME, field = "author")
    public CompletableFuture<UserSearchResponse> getCommentAuthor(DgsDataFetchingEnvironment dfe) {
        Comment comment = dfe.getSource();
        if (comment == null || comment.getUserId() == null || comment.getUserId().isEmpty()) {
            return CompletableFuture.completedFuture(null);
        }

        DataLoader<String, UserSearchResponse> dataLoader = dfe.getDataLoader("userDataLoader");
        if (dataLoader != null) {
            return dataLoader.load(comment.getUserId());
        }
        return CompletableFuture.completedFuture(fetchUser(comment.getUserId()));
    }

    @DgsData(parentType = DgsConstants.STORY.TYPE_NAME, field = "author")
    public CompletableFuture<UserSearchResponse> getStoryAuthor(DgsDataFetchingEnvironment dfe) {
        Story story = dfe.getSource();
        if (story == null || story.getAuthorId() == null || story.getAuthorId().isEmpty()) {
            return CompletableFuture.completedFuture(null);
        }

        DataLoader<String, UserSearchResponse> dataLoader = dfe.getDataLoader("userDataLoader");
        if (dataLoader != null) {
            return dataLoader.load(story.getAuthorId());
        }
        return CompletableFuture.completedFuture(fetchUser(story.getAuthorId()));
    }

    // Pomocnicza metoda wykorzystująca nasz wspólny EdgeMapper
    private UserSearchResponse fetchUser(String targetUserId) {
        CircuitBreaker cb = circuitBreakerRegistry.circuitBreaker("userService");
        Retry retry = retryRegistry.retry("userService");
        io.github.resilience4j.bulkhead.Bulkhead bulkhead = bulkheadRegistry.bulkhead("userService");

        try {
            GetUserByIdResponse response = CircuitBreaker.decorateSupplier(cb,
                    Retry.decorateSupplier(retry, io.github.resilience4j.bulkhead.Bulkhead.decorateSupplier(bulkhead, () -> userGrpcStub.getUserById(GetUserByIdRequest.newBuilder()
                            .setUserId(targetUserId)
                            .build())))
            ).get();

            return edgeMapper.grpcUserToDgsUser(response.getUser());
        } catch (Exception e) {
            log.error("Failed to resolve author via gRPC for ID: {}", targetUserId, e);
            return null;
        }
    }
}