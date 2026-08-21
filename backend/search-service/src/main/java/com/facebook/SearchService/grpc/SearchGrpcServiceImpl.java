package com.facebook.SearchService.grpc;

import com.facebook.SearchService.model.MeiliEvent;
import com.facebook.SearchService.model.MeiliUser;
import com.facebook.SearchService.model.User;
import com.facebook.SearchService.service.SearchService;
import com.facebook.search.grpc.*;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.server.service.GrpcService;

import java.util.UUID;

@GrpcService
@RequiredArgsConstructor
public class SearchGrpcServiceImpl extends SearchGrpcServiceGrpc.SearchGrpcServiceImplBase {

    private final SearchService searchService;

    @Override
    public void searchUsers(SearchUsersRequest request, StreamObserver<SearchUsersResponse> responseObserver) {
        try {
            SearchUsersResponse.Builder builder = SearchUsersResponse.newBuilder();
            
            // 1. Search Users
            for (MeiliUser user : searchService.searchUsers(request.getQuery())) {
                builder.addUsers(SearchUserHit.newBuilder()
                        .setId(user.getId())
                        .setUsername(user.getUsername() != null ? user.getUsername() : "")
                        .setFirstName(user.getFirstName() != null ? user.getFirstName() : "")
                        .setLastName(user.getLastName() != null ? user.getLastName() : "")
                        .setAvatarId(user.getAvatarId() != null ? user.getAvatarId() : "")
                        .setNewPostsCount(0)
                        .build());
            }

            // 2. Search Groups
            for (com.facebook.GroupsService.event.GroupIndexEvent group : searchService.searchGroups(request.getQuery())) {
                builder.addUsers(SearchUserHit.newBuilder()
                        .setId(group.getId())
                        .setUsername("__group__")
                        .setFirstName(group.getName() != null ? group.getName() : "")
                        .setLastName("")
                        .setAvatarId(group.getImage() != null ? group.getImage() : "")
                        .setNewPostsCount(group.getNewPostsCount() != null ? group.getNewPostsCount() : 0)
                        .build());
            }

            // 3. Search Pages
            for (com.facebook.UserService.dto.PageIndexEvent page : searchService.searchPages(request.getQuery())) {
                builder.addUsers(SearchUserHit.newBuilder()
                        .setId(page.getId())
                        .setUsername("__page__")
                        .setFirstName(page.getName() != null ? page.getName() : "")
                        .setLastName(page.getCategory() != null ? page.getCategory() : "")
                        .setAvatarId(page.getAvatarUrl() != null ? page.getAvatarUrl() : "")
                        .setNewPostsCount(0)
                        .build());
            }

            responseObserver.onNext(builder.build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(io.grpc.Status.INTERNAL
                    .withDescription("Failed to search users: " + e.getMessage())
                    .asRuntimeException());
        }
    }

    @Override
    public void indexUser(IndexUserRequest request, StreamObserver<IndexUserResponse> responseObserver) {
        try {
            User user = new User();
            user.setId(UUID.fromString(request.getId()));
            user.setUsername(request.getUsername());
            user.setFirstName(request.getFirstName());
            user.setLastName(request.getLastName());
            user.setAvatarId(request.getAvatarId());
            searchService.indexUser(user);
            responseObserver.onNext(IndexUserResponse.newBuilder().setSuccess(true).build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(io.grpc.Status.INTERNAL
                    .withDescription("Failed to index user: " + e.getMessage())
                    .asRuntimeException());
        }
    }

    @Override
    public void searchEvents(SearchEventsRequest request, StreamObserver<SearchEventsResponse> responseObserver) {
        try {
            SearchEventsResponse.Builder builder = SearchEventsResponse.newBuilder();
            for (MeiliEvent event : searchService.searchEvents(request.getQuery())) {
                SearchEventHit.Builder hit = SearchEventHit.newBuilder()
                        .setId(event.getId() != null ? event.getId() : "")
                        .setUserId(event.getUserId() != null ? event.getUserId() : "")
                        .setName(event.getName() != null ? event.getName() : "")
                        .setTitle(event.getTitle() != null ? event.getTitle() : "")
                        .setStartDate(event.getStartDate() != null ? event.getStartDate() : "")
                        .setStartTime(event.getStartTime() != null ? event.getStartTime() : "")
                        .setEndDate(event.getEndDate() != null ? event.getEndDate() : "")
                        .setEndTime(event.getEndTime() != null ? event.getEndTime() : "")
                        .setType(event.getType() != null ? event.getType() : "")
                        .setPrivacy(event.getPrivacy() != null ? event.getPrivacy() : "")
                        .setDescription(event.getDescription() != null ? event.getDescription() : "")
                        .setLocation(event.getLocation() != null ? event.getLocation() : "")
                        .setLocationName(event.getLocationName() != null ? event.getLocationName() : "")
                        .setAddress(event.getAddress() != null ? event.getAddress() : "")
                        .setDate(event.getDate() != null ? event.getDate() : "")
                        .setFrequency(event.getFrequency() != null ? event.getFrequency() : "");

                if (event.getShowGuestList() != null) {
                    hit.setShowGuestList(event.getShowGuestList());
                }
                if (event.getImages() != null) {
                    hit.addAllImages(event.getImages());
                }
                builder.addEvents(hit.build());
            }
            responseObserver.onNext(builder.build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(io.grpc.Status.INTERNAL
                    .withDescription("Failed to search events: " + e.getMessage())
                    .asRuntimeException());
        }
    }
}
