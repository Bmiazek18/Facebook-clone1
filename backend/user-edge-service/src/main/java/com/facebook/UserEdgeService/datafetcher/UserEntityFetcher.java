package com.facebook.UserEdgeService.datafetcher;

import com.facebook.UserEdgeService.mapper.EdgeMapper;
import com.facebook.user.generated.DgsConstants;
import com.facebook.user.generated.types.UserSearchResponse;

import com.facebook.user.grpc.GetUserByIdRequest;
import com.facebook.user.grpc.GetUserByIdResponse;
import com.facebook.user.grpc.UserGrpcServiceGrpc;
import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsEntityFetcher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;

import java.util.Map;

@Slf4j
@DgsComponent
@RequiredArgsConstructor
public class UserEntityFetcher {

    @GrpcClient("user-service")
    private UserGrpcServiceGrpc.UserGrpcServiceBlockingStub userGrpcStub;

    private final EdgeMapper edgeMapper;

    @DgsEntityFetcher(name = DgsConstants.USERSEARCHRESPONSE.TYPE_NAME)
    public UserSearchResponse resolveUser(Map<String, Object> representation) {
        Object idVal = representation.get("id");
        if (idVal == null) {
            log.warn("Edge: No ID provided in representation for federated User entity");
            return null;
        }

        String id = idVal.toString();
        log.info("Edge: Resolving federated User entity for ID: {}", id);

        try {
            GetUserByIdResponse response = userGrpcStub.getUserById(GetUserByIdRequest.newBuilder()
                    .setUserId(id)
                    .build());

            return edgeMapper.grpcUserToDgsUser(response.getUser());

        } catch (Exception e) {
            log.error("Failed to resolve federated User entity via gRPC", e);
            throw new RuntimeException("User core service unavailable: " + e.getMessage(), e);
        }
    }
}