package com.facebook.UserService.client;

import com.facebook.search.grpc.IndexUserRequest;
import com.facebook.search.grpc.SearchGrpcServiceGrpc;
import com.facebook.search.grpc.SearchUsersRequest;
import com.facebook.search.grpc.SearchUserHit;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
public class SearchServiceClientImpl implements SearchServiceClient {

    @GrpcClient("search-service")
    private SearchGrpcServiceGrpc.SearchGrpcServiceBlockingStub searchGrpcStub;

    @Override
    public List<SearchUserHit> searchUsers(String query) {
        if (query == null || query.isBlank()) {
            return Collections.emptyList();
        }
        return searchGrpcStub.searchUsers(SearchUsersRequest.newBuilder()
                .setQuery(query.trim())
                .build()).getUsersList();
    }

    @Override
    public void indexUser(String id, String username, String firstName, String lastName, String avatarId) {
        searchGrpcStub.indexUser(IndexUserRequest.newBuilder()
                .setId(id)
                .setUsername(username != null ? username : "")
                .setFirstName(firstName != null ? firstName : "")
                .setLastName(lastName != null ? lastName : "")
                .setAvatarId(avatarId != null ? avatarId : "")
                .build());
    }
}
