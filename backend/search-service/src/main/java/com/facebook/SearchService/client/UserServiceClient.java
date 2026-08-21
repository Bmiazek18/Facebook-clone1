package com.facebook.SearchService.client;

import com.facebook.user.grpc.GetAllUsersRequest;
import com.facebook.user.grpc.GetAllUsersResponse;
import com.facebook.user.grpc.UserGrpcServiceGrpc;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Component;

@Component
public class UserServiceClient {

    @GrpcClient("user-service")
    private UserGrpcServiceGrpc.UserGrpcServiceBlockingStub userGrpcStub;

    @CircuitBreaker(name = "userService", fallbackMethod = "getAllUsersFallback")
    @Retry(name = "userService")
    public GetAllUsersResponse getAllUsers(int page, int size) {
        return userGrpcStub.getAllUsers(
                GetAllUsersRequest.newBuilder()
                        .setPage(page)
                        .setSize(size)
                        .build()
        );
    }

    public GetAllUsersResponse getAllUsersFallback(int page, int size, Throwable throwable) {
        System.err.println("Warning: search-service UserServiceClient circuit breaker triggered! Reason: " + throwable.getMessage());
        return GetAllUsersResponse.newBuilder().build(); // returns empty list response
    }
}
