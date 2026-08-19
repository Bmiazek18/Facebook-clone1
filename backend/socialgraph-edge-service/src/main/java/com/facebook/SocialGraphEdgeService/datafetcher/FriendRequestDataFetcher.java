package com.facebook.SocialGraphEdgeService.datafetcher;

// Importujemy klasę wygenerowaną przez DGS Codegen
import com.facebook.socialgraph.generated.types.FriendRequestResponse;

import com.facebook.socialgraph.grpc.FriendRequestMsg;
import com.facebook.socialgraph.grpc.SocialGraphGrpcServiceGrpc;
import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsMutation;
import com.netflix.graphql.dgs.InputArgument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import net.devh.boot.grpc.client.inject.GrpcClient;

@DgsComponent
public class FriendRequestDataFetcher {

    private static final Logger log = LoggerFactory.getLogger(FriendRequestDataFetcher.class);

    @GrpcClient("social-graph-service")
    private SocialGraphGrpcServiceGrpc.SocialGraphGrpcServiceBlockingStub socialGraphGrpcStub;

    @DgsMutation
    public FriendRequestResponse sendFriendRequest(
            @InputArgument String senderId,
            @InputArgument String receiverId) {
        log.info("Edge: Sending friend request from {} to {} via gRPC", senderId, receiverId);
        try {
            // Używamy pełnej ścieżki dla odpowiedzi gRPC, aby uniknąć konfliktu nazw z klasą z DGS
            com.facebook.socialgraph.grpc.FriendRequestResponse grpcResponse = socialGraphGrpcStub.sendFriendRequest(FriendRequestMsg.newBuilder()
                    .setSenderId(senderId)
                    .setReceiverId(receiverId)
                    .build());

            // Tworzymy wygenerowany obiekt GraphQL i przypisujemy wartości
            FriendRequestResponse response = new FriendRequestResponse();
            response.setSuccess(grpcResponse.getSuccess());
            response.setMessage(grpcResponse.getMessage());

            return response;
        } catch (Exception e) {
            log.error("Failed to send friend request", e);

            FriendRequestResponse errorResponse = new FriendRequestResponse();
            errorResponse.setSuccess(false);
            errorResponse.setMessage("SocialGraph service error: " + e.getMessage());

            return errorResponse;
        }
    }

    @DgsMutation
    public FriendRequestResponse acceptFriendRequest(
            @InputArgument String senderId,
            @InputArgument String receiverId) {
        log.info("Edge: Accepting friend request from {} to {} via gRPC", senderId, receiverId);
        try {
            com.facebook.socialgraph.grpc.FriendRequestResponse grpcResponse = socialGraphGrpcStub.acceptFriendRequest(FriendRequestMsg.newBuilder()
                    .setSenderId(senderId)
                    .setReceiverId(receiverId)
                    .build());

            FriendRequestResponse response = new FriendRequestResponse();
            response.setSuccess(grpcResponse.getSuccess());
            response.setMessage(grpcResponse.getMessage());

            return response;
        } catch (Exception e) {
            log.error("Failed to accept friend request", e);

            FriendRequestResponse errorResponse = new FriendRequestResponse();
            errorResponse.setSuccess(false);
            errorResponse.setMessage("SocialGraph service error: " + e.getMessage());

            return errorResponse;
        }
    }
}