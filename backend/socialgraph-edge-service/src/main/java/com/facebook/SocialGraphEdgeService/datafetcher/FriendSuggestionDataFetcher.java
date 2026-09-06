package com.facebook.SocialGraphEdgeService.datafetcher;

// Importujemy bezpieczne typy z generatora
import com.facebook.socialgraph.generated.types.FriendSuggestion;
import com.facebook.socialgraph.generated.types.UserSearchResponse;

import com.facebook.socialgraph.grpc.GetFriendSuggestionsRequest;
import com.facebook.socialgraph.grpc.GetFriendSuggestionsResponse;
import com.facebook.socialgraph.grpc.SocialGraphGrpcServiceGrpc;
import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsQuery;
import com.netflix.graphql.dgs.InputArgument;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryRegistry;
import io.github.resilience4j.bulkhead.Bulkhead;
import io.github.resilience4j.bulkhead.BulkheadRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.List;
import java.util.stream.Collectors;

@DgsComponent
public class FriendSuggestionDataFetcher {

    private static final Logger log = LoggerFactory.getLogger(FriendSuggestionDataFetcher.class);

    @GrpcClient("social-graph-service")
    private SocialGraphGrpcServiceGrpc.SocialGraphGrpcServiceBlockingStub socialGraphGrpcStub;

    private final CircuitBreakerRegistry circuitBreakerRegistry;
    private final RetryRegistry retryRegistry;
    private final BulkheadRegistry bulkheadRegistry;

    public FriendSuggestionDataFetcher(
            CircuitBreakerRegistry circuitBreakerRegistry,
            RetryRegistry retryRegistry,
            BulkheadRegistry bulkheadRegistry) {
        this.circuitBreakerRegistry = circuitBreakerRegistry;
        this.retryRegistry = retryRegistry;
        this.bulkheadRegistry = bulkheadRegistry;
    }

    @DgsQuery
    public List<UserSearchResponse> getFriends(
            @InputArgument String userId,
            @InputArgument String filterType,
            @InputArgument Integer limit,
            @InputArgument Integer offset,
            @RequestHeader(name = "X-User-Id", required = false) String xUserId) {
        String effectiveUserId = (xUserId != null && !xUserId.isBlank() && (userId == null || userId.isBlank())) ? xUserId : userId;
        log.info("Edge: Fetching friends via gRPC for user: {}, filter: {}, limit: {}, offset: {}", effectiveUserId, filterType, limit, offset);
        try {
            return executeWithResilience(() -> {
                var reqBuilder = com.facebook.socialgraph.grpc.GetFriendsRequest.newBuilder()
                        .setUserId(effectiveUserId != null ? effectiveUserId : "");
                if (filterType != null && !filterType.isBlank()) {
                    reqBuilder.setFilterType(filterType);
                }
                if (limit != null && limit > 0) {
                    reqBuilder.setLimit(limit);
                }
                if (offset != null && offset >= 0) {
                    reqBuilder.setOffset(offset);
                }

                com.facebook.socialgraph.grpc.GetFriendsResponse response = socialGraphGrpcStub.getFriends(reqBuilder.build());

                return response.getFriendIdsList().stream()
                        .map(fid -> {
                            UserSearchResponse user = new UserSearchResponse();
                            user.setId(fid);
                            return user;
                        })
                        .collect(Collectors.toList());
            });
        } catch (Exception e) {
            log.error("Failed to fetch friends list", e);
            throw new RuntimeException("SocialGraph core service unavailable: " + e.getMessage());
        }
    }

    @DgsQuery
    public List<FriendSuggestion> getFriendSuggestions(
            @InputArgument String currentUserId,
            @RequestHeader(name = "X-User-Id", required = false) String xUserId) {
        String effectiveUserId = (xUserId != null && !xUserId.isBlank()) ? xUserId : currentUserId;
        log.info("Edge: Fetching friend suggestions via gRPC for user: {}", effectiveUserId);
        try {
            return executeWithResilience(() -> {
                GetFriendSuggestionsResponse response = socialGraphGrpcStub.getFriendSuggestions(GetFriendSuggestionsRequest.newBuilder()
                        .setUserId(effectiveUserId != null ? effectiveUserId : "")
                        .build());

                return response.getSuggestionsList().stream()
                        .map(s -> {
                            if (s.getUserId() == null || s.getUserId().isBlank()) {
                                return null;
                            }
                            FriendSuggestion suggestion = new FriendSuggestion();
                            suggestion.setUserId(s.getUserId());
                            suggestion.setMutualFriendsCount(s.getMutualFriendsCount());

                            UserSearchResponse user = new UserSearchResponse();
                            user.setId(s.getUserId());
                            suggestion.setUser(user);

                            return suggestion;
                        })
                        .filter(java.util.Objects::nonNull)
                        .collect(Collectors.toList());
            });
        } catch (Exception e) {
            log.error("Failed to fetch friend suggestions", e);
            throw new RuntimeException("SocialGraph core service unavailable: " + e.getMessage());
        }
    }

    @DgsQuery
    public List<FriendSuggestion> getFriendRequests(
            @InputArgument String currentUserId,
            @RequestHeader(name = "X-User-Id", required = false) String xUserId) {
        String effectiveUserId = (xUserId != null && !xUserId.isBlank()) ? xUserId : currentUserId;
        log.info("Edge: Fetching friend requests via gRPC for user: {}", effectiveUserId);
        try {
            return executeWithResilience(() -> {
                GetFriendSuggestionsResponse response = socialGraphGrpcStub.getFriendRequests(GetFriendSuggestionsRequest.newBuilder()
                        .setUserId(effectiveUserId != null ? effectiveUserId : "")
                        .build());

                return response.getSuggestionsList().stream()
                        .map(s -> {
                            if (s.getUserId() == null || s.getUserId().isBlank()) {
                                return null;
                            }
                            FriendSuggestion suggestion = new FriendSuggestion();
                            suggestion.setUserId(s.getUserId());
                            suggestion.setMutualFriendsCount(s.getMutualFriendsCount());

                            UserSearchResponse user = new UserSearchResponse();
                            user.setId(s.getUserId());
                            suggestion.setUser(user);

                            return suggestion;
                        })
                        .filter(java.util.Objects::nonNull)
                        .collect(Collectors.toList());
            });
        } catch (Exception e) {
            log.error("Failed to fetch friend requests", e);
            throw new RuntimeException("SocialGraph core service unavailable: " + e.getMessage());
        }
    }

    private <T> T executeWithResilience(java.util.function.Supplier<T> action) {
        CircuitBreaker cb = circuitBreakerRegistry.circuitBreaker("socialGraphService");
        Retry retry = retryRegistry.retry("socialGraphService");
        Bulkhead bulkhead = bulkheadRegistry.bulkhead("socialGraphService");
        return CircuitBreaker.decorateSupplier(cb, Retry.decorateSupplier(retry, Bulkhead.decorateSupplier(bulkhead, action))).get();
    }
}