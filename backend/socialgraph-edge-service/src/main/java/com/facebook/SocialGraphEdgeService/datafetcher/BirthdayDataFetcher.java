package com.facebook.SocialGraphEdgeService.datafetcher;

import com.facebook.socialgraph.generated.types.BirthdayUser;
import com.facebook.socialgraph.generated.types.UserSearchResponse;
import com.facebook.socialgraph.grpc.GetBirthdayUsersRequest;
import com.facebook.socialgraph.grpc.GetBirthdayUsersResponse;
import com.facebook.socialgraph.grpc.SocialGraphGrpcServiceGrpc;
import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsQuery;
import com.netflix.graphql.dgs.InputArgument;
import io.github.resilience4j.bulkhead.Bulkhead;
import io.github.resilience4j.bulkhead.BulkheadRegistry;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryRegistry;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@DgsComponent
public class BirthdayDataFetcher {

    private static final Logger log = LoggerFactory.getLogger(BirthdayDataFetcher.class);

    @GrpcClient("social-graph-service")
    private SocialGraphGrpcServiceGrpc.SocialGraphGrpcServiceBlockingStub socialGraphGrpcStub;

    private final CircuitBreakerRegistry circuitBreakerRegistry;
    private final RetryRegistry retryRegistry;
    private final BulkheadRegistry bulkheadRegistry;

    public BirthdayDataFetcher(
            CircuitBreakerRegistry circuitBreakerRegistry,
            RetryRegistry retryRegistry,
            BulkheadRegistry bulkheadRegistry) {
        this.circuitBreakerRegistry = circuitBreakerRegistry;
        this.retryRegistry = retryRegistry;
        this.bulkheadRegistry = bulkheadRegistry;
    }

    private <T> T executeWithResilience(Supplier<T> supplier) {
        CircuitBreaker cb = circuitBreakerRegistry.circuitBreaker("socialGraphService");
        Retry retry = retryRegistry.retry("socialGraphService");
        Bulkhead bulkhead = bulkheadRegistry.bulkhead("socialGraphService");

        Supplier<T> decorated = Bulkhead.decorateSupplier(bulkhead,
                CircuitBreaker.decorateSupplier(cb,
                        Retry.decorateSupplier(retry, supplier)));
        return decorated.get();
    }

    @DgsQuery
    public List<BirthdayUser> getBirthdayUsers(
            @InputArgument String currentUserId,
            @RequestHeader(name = "X-User-Id", required = false) String xUserId) {
        String effectiveUserId = (xUserId != null && !xUserId.isBlank()) ? xUserId : currentUserId;
        if (effectiveUserId == null || effectiveUserId.isBlank()) {
            return Collections.emptyList();
        }

        log.info("Edge: Fetching birthday users via gRPC for user: {}", effectiveUserId);
        try {
            GetBirthdayUsersResponse response = executeWithResilience(() ->
                    socialGraphGrpcStub.getBirthdayUsers(GetBirthdayUsersRequest.newBuilder()
                            .setUserId(effectiveUserId)
                            .build()));

            return response.getUsersList().stream()
                    .map(u -> {
                        BirthdayUser birthdayUser = new BirthdayUser();
                        birthdayUser.setUserId(u.getUserId());
                        birthdayUser.setBirthDate(u.getBirthDate());

                        UserSearchResponse user = new UserSearchResponse();
                        user.setId(u.getUserId());
                        birthdayUser.setUser(user);

                        return birthdayUser;
                    })
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Failed to fetch birthday users via gRPC for user: {}", effectiveUserId, e);
            return Collections.emptyList();
        }
    }
}