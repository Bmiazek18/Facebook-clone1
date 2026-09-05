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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import net.devh.boot.grpc.client.inject.GrpcClient;

import java.util.List;
import java.util.stream.Collectors;

@DgsComponent
public class FriendSuggestionDataFetcher {

    private static final Logger log = LoggerFactory.getLogger(FriendSuggestionDataFetcher.class);

    @GrpcClient("social-graph-service")
    private SocialGraphGrpcServiceGrpc.SocialGraphGrpcServiceBlockingStub socialGraphGrpcStub;

    @DgsQuery
    public List<UserSearchResponse> getFriends(@InputArgument String userId,
                                               @InputArgument String filterType,
                                               @InputArgument Integer limit,
                                               @InputArgument Integer offset) {
        log.info("Edge: Fetching friends via gRPC for user: {}, filter: {}, limit: {}, offset: {}", userId, filterType, limit, offset);
        try {
            var reqBuilder = com.facebook.socialgraph.grpc.GetFriendsRequest.newBuilder()
                    .setUserId(userId);
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

            // Zamiast Map<String, Object> zwracamy wygenerowany typ UserSearchResponse
            return response.getFriendIdsList().stream()
                    .map(fid -> {
                        UserSearchResponse user = new UserSearchResponse();
                        user.setId(fid);
                        return user;
                    })
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Failed to fetch friends list", e);
            throw new RuntimeException("SocialGraph core service unavailable: " + e.getMessage());
        }
    }

    @DgsQuery
    public List<FriendSuggestion> getFriendSuggestions(@InputArgument String currentUserId) {
        log.info("Edge: Fetching friend suggestions via gRPC for user: {}", currentUserId);
        try {
            GetFriendSuggestionsResponse response = socialGraphGrpcStub.getFriendSuggestions(GetFriendSuggestionsRequest.newBuilder()
                    .setUserId(currentUserId)
                    .build());

            // Mapujemy odpowiedź gRPC bezpośrednio na wygenerowany model DGS
            return response.getSuggestionsList().stream()
                    .map(s -> {
                        if (s.getUserId() == null || s.getUserId().isBlank()) {
                            return null;
                        }
                        FriendSuggestion suggestion = new FriendSuggestion();
                        suggestion.setUserId(s.getUserId());
                        suggestion.setMutualFriendsCount(s.getMutualFriendsCount());

                        // Od razu budujemy i przypisujemy zagnieżdżony obiekt UserSearchResponse
                        UserSearchResponse user = new UserSearchResponse();
                        user.setId(s.getUserId());
                        suggestion.setUser(user);

                        return suggestion;
                    })
                    .filter(java.util.Objects::nonNull)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Failed to fetch friend suggestions", e);
            throw new RuntimeException("SocialGraph core service unavailable: " + e.getMessage());
        }
    }

    @DgsQuery
    public List<FriendSuggestion> getFriendRequests(@InputArgument String currentUserId) {
        log.info("Edge: Fetching friend requests via gRPC for user: {}", currentUserId);
        try {
            GetFriendSuggestionsResponse response = socialGraphGrpcStub.getFriendRequests(GetFriendSuggestionsRequest.newBuilder()
                    .setUserId(currentUserId)
                    .build());

            return response.getSuggestionsList().stream()
                    .map(s -> {
                        if (s.getUserId() == null || s.getUserId().isBlank()) {
                            return null;
                        }
                        FriendSuggestion suggestion = new FriendSuggestion();
                        suggestion.setUserId(s.getUserId());
                        suggestion.setMutualFriendsCount(s.getMutualFriendsCount());

                        // Od razu budujemy i przypisujemy zagnieżdżony obiekt UserSearchResponse
                        UserSearchResponse user = new UserSearchResponse();
                        user.setId(s.getUserId());
                        suggestion.setUser(user);

                        return suggestion;
                    })
                    .filter(java.util.Objects::nonNull)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Failed to fetch friend requests", e);
            throw new RuntimeException("SocialGraph core service unavailable: " + e.getMessage());
        }
    }
}