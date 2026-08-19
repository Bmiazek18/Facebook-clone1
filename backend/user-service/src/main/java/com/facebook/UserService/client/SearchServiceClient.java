package com.facebook.UserService.client;

import com.facebook.search.grpc.SearchUserHit;

import java.util.List;

public interface SearchServiceClient {
    List<SearchUserHit> searchUsers(String query);

    void indexUser(String id, String username, String firstName, String lastName, String avatarId);
}
