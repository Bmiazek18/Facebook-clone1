package com.facebook.UserService.client;

import com.facebook.search.grpc.IndexUserRequest;
import com.facebook.search.grpc.SearchGrpcServiceGrpc;
import com.facebook.search.grpc.SearchUsersRequest;
import com.facebook.search.grpc.SearchUserHit;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
public class SearchServiceClientImpl implements SearchServiceClient {

    @GrpcClient("search-service")
    private SearchGrpcServiceGrpc.SearchGrpcServiceBlockingStub searchGrpcStub;

    @Override
    @CircuitBreaker(name = "searchService", fallbackMethod = "searchUsersFallback")
    @Retry(name = "searchService")
    public List<SearchUserHit> searchUsers(String query) {
        if (query == null || query.isBlank()) {
            return Collections.emptyList();
        }
        return searchGrpcStub.searchUsers(SearchUsersRequest.newBuilder()
                .setQuery(query.trim())
                .build()).getUsersList();
    }

    public List<SearchUserHit> searchUsersFallback(String query, Throwable throwable) {
        System.err.println("Warning: search-service unavailable for query: " + query + ", returning empty results. Reason: " + throwable.getMessage());
        return Collections.emptyList();
    }

    @Override
    @CircuitBreaker(name = "searchService", fallbackMethod = "indexUserFallback")
    @Retry(name = "searchService")
    public void indexUser(String id, String username, String firstName, String lastName, String avatarId) {
        searchGrpcStub.indexUser(IndexUserRequest.newBuilder()
                .setId(id)
                .setUsername(username != null ? username : "")
                .setFirstName(firstName != null ? firstName : "")
                .setLastName(lastName != null ? lastName : "")
                .setAvatarId(avatarId != null ? avatarId : "")
                .build());
    }

    public void indexUserFallback(String id, String username, String firstName, String lastName, String avatarId, Throwable throwable) {
        System.err.println("Warning: Could not index user " + id + " in search-service, skipping. Reason: " + throwable.getMessage());
    }
}
