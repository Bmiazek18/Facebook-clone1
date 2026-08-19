package com.facebook.UserService.client;

import com.facebook.socialgraph.grpc.GetRelationsRequest;
import com.facebook.socialgraph.grpc.GetRelationsResponse;
import com.facebook.socialgraph.grpc.UserRelation;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class SocialGraphClientImpl implements SocialGraphClient {

    @GrpcClient("social-graph-service")
    private com.facebook.socialgraph.grpc.SocialGraphGrpcServiceGrpc.SocialGraphGrpcServiceBlockingStub socialGraphGrpcStub;

    @Override
    public List<UserRelation> getRelations(UUID userId, List<UUID> targetUserIds) {
        if (targetUserIds == null || targetUserIds.isEmpty()) {
            return Collections.emptyList();
        }

        try {
            GetRelationsRequest grpcRequest = GetRelationsRequest.newBuilder()
                    .setUserId(userId.toString())
                    .addAllTargetUserIds(targetUserIds.stream().map(UUID::toString).collect(Collectors.toList()))
                    .build();

            GetRelationsResponse grpcResponse = socialGraphGrpcStub.getRelations(grpcRequest);
            return grpcResponse.getRelationsList();

        } catch (Exception e) {
            System.err.println("Warning: Could not connect to external Social Graph service via gRPC: " + e.getMessage());
        }

        return targetUserIds.stream()
                .map(id -> UserRelation.newBuilder()
                        .setTargetUserId(id.toString())
                        .setFriend(false)
                        .setMutualFriendsCount(0)
                        .build())
                .collect(Collectors.toList());
    }
}
