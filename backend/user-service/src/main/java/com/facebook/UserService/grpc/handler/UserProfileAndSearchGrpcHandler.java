package com.facebook.UserService.grpc.handler;

import com.facebook.UserService.mapper.UserProtoMapper;
import com.facebook.UserService.model.User;
import com.facebook.UserService.service.UserActiveService;
import com.facebook.UserService.service.UserService;
import com.facebook.user.grpc.*;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserProfileAndSearchGrpcHandler {

    private final UserService userService;
    private final UserActiveService userActiveService;
    private final GrpcUnaryHelper grpcUnaryHelper;

    public void getUserById(GetUserByIdRequest request, StreamObserver<GetUserByIdResponse> responseObserver) {
        log.info("gRPC: Fetching user profile by ID: {}", request.getUserId());
        grpcUnaryHelper.handleUnary(
                () -> GetUserByIdResponse.newBuilder()
                        .setUser(userService.getUserProfileById(UUID.fromString(request.getUserId())))
                        .build(),
                responseObserver,
                "Failed to get user by ID via gRPC"
        );
    }

    public void recordSearch(RecordSearchRequest request, StreamObserver<RecordSearchResponse> responseObserver) {
        log.info("gRPC: Recording search: searchedUser={}, searchingUser={}", request.getSearchedUserId(), request.getSearchingUserId());
        grpcUnaryHelper.handleUnary(
                () -> {
                    UUID searchedUserId = UUID.fromString(request.getSearchedUserId());
                    UUID searchingUserId = !request.getSearchingUserId().isEmpty() ? UUID.fromString(request.getSearchingUserId()) : null;
                    userService.searchUserById(searchedUserId, searchingUserId);
                    return RecordSearchResponse.newBuilder().setSuccess(true).build();
                },
                responseObserver,
                "Failed to record search via gRPC"
        );
    }

    public void deleteSearchHistoryItem(DeleteSearchHistoryRequest request, StreamObserver<DeleteSearchHistoryResponse> responseObserver) {
        log.info("gRPC: Deleting search history item: searchedUser={}, searchingUser={}", request.getSearchedUserId(), request.getSearchingUserId());
        grpcUnaryHelper.handleUnary(
                () -> {
                    UUID searchedUserId = UUID.fromString(request.getSearchedUserId());
                    UUID searchingUserId = !request.getSearchingUserId().isEmpty() ? UUID.fromString(request.getSearchingUserId()) : null;
                    userService.deleteSearchHistoryItem(searchedUserId, searchingUserId);
                    return DeleteSearchHistoryResponse.newBuilder().setSuccess(true).build();
                },
                responseObserver,
                "Failed to delete search history item via gRPC"
        );
    }

    public void searchUsers(SearchUsersRequest request, StreamObserver<SearchUsersResponse> responseObserver) {
        log.info("gRPC: Searching users with query: {}, currentUserId: {}", request.getQuery(), request.getCurrentUserId());
        grpcUnaryHelper.handleUnary(
                () -> {
                    UUID currentUserId = request.getCurrentUserId().isEmpty()
                            ? null
                            : UUID.fromString(request.getCurrentUserId());
                    return SearchUsersResponse.newBuilder()
                            .addAllUsers(userService.searchUsers(request.getQuery(), currentUserId))
                            .build();
                },
                responseObserver,
                "Failed to search users via gRPC"
        );
    }

    public void getSearchHistory(GetSearchHistoryRequest request, StreamObserver<GetSearchHistoryResponse> responseObserver) {
        log.info("gRPC: Fetching search history");
        grpcUnaryHelper.handleUnary(
                () -> {
                    UUID userId = request.getUserId().isEmpty()
                            ? null
                            : UUID.fromString(request.getUserId());
                    return GetSearchHistoryResponse.newBuilder()
                            .addAllUsers(userService.getSearchHistory(userId))
                            .build();
                },
                responseObserver,
                "Failed to fetch search history via gRPC"
        );
    }

    public void getActiveStatuses(GetActiveStatusesRequest request, StreamObserver<GetActiveStatusesResponse> responseObserver) {
        log.info("gRPC: Fetching active statuses for {} users", request.getUserIdsCount());
        grpcUnaryHelper.handleUnary(
                () -> {
                    List<UUID> ids = request.getUserIdsList().stream()
                            .map(UUID::fromString)
                            .collect(Collectors.toList());
                    return GetActiveStatusesResponse.newBuilder()
                            .addAllStatuses(userActiveService.getActiveStatuses(ids))
                            .build();
                },
                responseObserver,
                "Failed to get active statuses via gRPC"
        );
    }

    public void setUserActive(SetUserActiveRequest request, StreamObserver<SetUserActiveResponse> responseObserver) {
        log.info("gRPC: Setting user active: {}", request.getUserId());
        grpcUnaryHelper.handleUnary(
                () -> {
                    userActiveService.setUserActive(UUID.fromString(request.getUserId()));
                    return SetUserActiveResponse.newBuilder().setSuccess(true).build();
                },
                responseObserver,
                "Failed to set user active via gRPC"
        );
    }

    public void updateProfile(UpdateProfileRequest request, StreamObserver<UpdateProfileResponse> responseObserver) {
        log.info("gRPC: Updating profile for user: {}", request.getUserId());
        grpcUnaryHelper.handleUnary(
                () -> UpdateProfileResponse.newBuilder()
                        .setUser(userService.updateProfile(UUID.fromString(request.getUserId()), request))
                        .build(),
                responseObserver,
                "Failed to update profile via gRPC"
        );
    }

    public void getAllUsers(GetAllUsersRequest request, StreamObserver<GetAllUsersResponse> responseObserver) {
        log.info("gRPC: Fetching all users for search service reindexing, page={}, size={}", request.getPage(), request.getSize());
        grpcUnaryHelper.handleUnary(
                () -> {
                    List<User> users = userService.getAllUsers(request.getPage(), request.getSize());
                    List<UserDto> dtos = users.stream()
                            .map(UserProtoMapper::toUserDto)
                            .collect(Collectors.toList());
                    return GetAllUsersResponse.newBuilder()
                            .addAllUsers(dtos)
                            .build();
                },
                responseObserver,
                "Failed to get all users via gRPC"
        );
    }
}
