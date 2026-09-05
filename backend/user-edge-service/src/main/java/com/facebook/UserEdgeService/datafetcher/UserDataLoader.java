package com.facebook.UserEdgeService.datafetcher;

import com.facebook.UserEdgeService.mapper.EdgeMapper;
import com.facebook.user.generated.types.UserSearchResponse;
import com.facebook.user.grpc.GetUserByIdRequest;
import com.facebook.user.grpc.GetUserByIdResponse;
import com.facebook.user.grpc.UserGrpcServiceGrpc;
import com.netflix.graphql.dgs.DgsDataLoader;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.dataloader.MappedBatchLoader;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ForkJoinPool;

@Slf4j
@DgsDataLoader(name = "userDataLoader")
@RequiredArgsConstructor
public class UserDataLoader implements MappedBatchLoader<String, UserSearchResponse> {

    @GrpcClient("user-service")
    private UserGrpcServiceGrpc.UserGrpcServiceBlockingStub userGrpcStub;

    private final EdgeMapper edgeMapper;

    @Override
    public CompletionStage<Map<String, UserSearchResponse>> load(Set<String> userIds) {
        return CompletableFuture.supplyAsync(() -> {
            log.info("UserDataLoader: Batch loading {} users: {}", userIds.size(), userIds);
            Map<String, UserSearchResponse> userMap = new HashMap<>();

            for (String userId : userIds) {
                if (userId == null || userId.trim().isEmpty()) continue;
                try {
                    GetUserByIdResponse response = userGrpcStub.getUserById(
                        GetUserByIdRequest.newBuilder().setUserId(userId).build()
                    );
                    if (response != null && response.hasUser()) {
                        userMap.put(userId, edgeMapper.grpcUserToDgsUser(response.getUser()));
                    }
                } catch (Exception e) {
                    log.error("UserDataLoader: Failed to fetch user with id {}", userId, e);
                    UserSearchResponse fallback = new UserSearchResponse();
                    fallback.setId(userId);
                    userMap.put(userId, fallback);
                }
            }

            return userMap;
        }, ForkJoinPool.commonPool());
    }
}
