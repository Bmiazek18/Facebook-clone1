package com.facebook.SocialGraphEdgeService.datafetcher;

// Importujemy klasę wygenerowaną przez DGS Codegen
import com.facebook.socialgraph.generated.types.FriendRequestResponse;

import com.facebook.socialgraph.grpc.FriendRequestMsg;
import com.facebook.socialgraph.grpc.SocialGraphGrpcServiceGrpc;
import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsMutation;
import com.netflix.graphql.dgs.InputArgument;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryRegistry;
import io.github.resilience4j.bulkhead.Bulkhead;
import io.github.resilience4j.bulkhead.BulkheadRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.web.bind.annotation.RequestHeader;

@DgsComponent
public class FriendRequestDataFetcher {

    private static final Logger log = LoggerFactory.getLogger(FriendRequestDataFetcher.class);

    @GrpcClient("social-graph-service")
    private SocialGraphGrpcServiceGrpc.SocialGraphGrpcServiceBlockingStub socialGraphGrpcStub;

    private final CircuitBreakerRegistry circuitBreakerRegistry;
    private final RetryRegistry retryRegistry;
    private final BulkheadRegistry bulkheadRegistry;

    public FriendRequestDataFetcher(
            CircuitBreakerRegistry circuitBreakerRegistry,
            RetryRegistry retryRegistry,
            BulkheadRegistry bulkheadRegistry) {
        this.circuitBreakerRegistry = circuitBreakerRegistry;
        this.retryRegistry = retryRegistry;
        this.bulkheadRegistry = bulkheadRegistry;
    }

    @DgsMutation
    public FriendRequestResponse sendFriendRequest(
            @InputArgument String senderId,
            @InputArgument String receiverId,
            @RequestHeader(name = "X-User-Id", required = false) String xUserId) {
        String effectiveSenderId = (xUserId != null && !xUserId.isBlank()) ? xUserId : senderId;
        log.info("Edge: Sending friend request from {} to {} via gRPC", effectiveSenderId, receiverId);
        try {
            return executeWithResilience(() -> {
                com.facebook.socialgraph.grpc.FriendRequestResponse grpcResponse = socialGraphGrpcStub.sendFriendRequest(FriendRequestMsg.newBuilder()
                        .setSenderId(effectiveSenderId != null ? effectiveSenderId : "")
                        .setReceiverId(receiverId)
                        .build());

                FriendRequestResponse response = new FriendRequestResponse();
                response.setSuccess(grpcResponse.getSuccess());
                response.setMessage(grpcResponse.getMessage());
                return response;
            });
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
            @InputArgument String receiverId,
            @RequestHeader(name = "X-User-Id", required = false) String xUserId) {
        String effectiveReceiverId = (xUserId != null && !xUserId.isBlank()) ? xUserId : receiverId;
        log.info("Edge: Accepting friend request from {} by {} via gRPC", senderId, effectiveReceiverId);
        try {
            return executeWithResilience(() -> {
                com.facebook.socialgraph.grpc.FriendRequestResponse grpcResponse = socialGraphGrpcStub.acceptFriendRequest(FriendRequestMsg.newBuilder()
                        .setSenderId(senderId)
                        .setReceiverId(effectiveReceiverId != null ? effectiveReceiverId : "")
                        .build());

                FriendRequestResponse response = new FriendRequestResponse();
                response.setSuccess(grpcResponse.getSuccess());
                response.setMessage(grpcResponse.getMessage());
                return response;
            });
        } catch (Exception e) {
            log.error("Failed to accept friend request", e);

            FriendRequestResponse errorResponse = new FriendRequestResponse();
            errorResponse.setSuccess(false);
            errorResponse.setMessage("SocialGraph service error: " + e.getMessage());

            return errorResponse;
        }
    }

    private <T> T executeWithResilience(java.util.function.Supplier<T> action) {
        CircuitBreaker cb = circuitBreakerRegistry.circuitBreaker("socialGraphService");
        Retry retry = retryRegistry.retry("socialGraphService");
        Bulkhead bulkhead = bulkheadRegistry.bulkhead("socialGraphService");
        return CircuitBreaker.decorateSupplier(cb, Retry.decorateSupplier(retry, Bulkhead.decorateSupplier(bulkhead, action))).get();
    }
}