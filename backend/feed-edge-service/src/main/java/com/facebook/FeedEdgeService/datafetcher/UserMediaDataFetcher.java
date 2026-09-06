package com.facebook.FeedEdgeService.datafetcher;

import com.facebook.FeedEdgeService.codegen.types.UserAlbum;
import com.facebook.FeedEdgeService.codegen.types.UserMediaItem;
import com.facebook.FeedEdgeService.codegen.types.UserMediaResponse;
import com.facebook.feed.grpc.FeedGrpcServiceGrpc;
import com.facebook.feed.grpc.GetUserAlbumsRequest;
import com.facebook.feed.grpc.GetUserAlbumsResponse;
import com.facebook.feed.grpc.GetUserMediaRequest;
import com.facebook.feed.grpc.GetUserMediaResponse;
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

@DgsComponent
public class UserMediaDataFetcher {

    private static final Logger log = LoggerFactory.getLogger(UserMediaDataFetcher.class);

    @GrpcClient("feed-service")
    private FeedGrpcServiceGrpc.FeedGrpcServiceBlockingStub feedGrpcStub;

    private final CircuitBreakerRegistry circuitBreakerRegistry;
    private final RetryRegistry retryRegistry;
    private final BulkheadRegistry bulkheadRegistry;

    public UserMediaDataFetcher(
            CircuitBreakerRegistry circuitBreakerRegistry,
            RetryRegistry retryRegistry,
            BulkheadRegistry bulkheadRegistry) {
        this.circuitBreakerRegistry = circuitBreakerRegistry;
        this.retryRegistry = retryRegistry;
        this.bulkheadRegistry = bulkheadRegistry;
    }

    private <T> T executeWithResilience(Supplier<T> supplier) {
        CircuitBreaker cb = circuitBreakerRegistry.circuitBreaker("feedService");
        Retry retry = retryRegistry.retry("feedService");
        Bulkhead bulkhead = bulkheadRegistry.bulkhead("feedService");

        Supplier<T> decorated = Bulkhead.decorateSupplier(bulkhead,
                CircuitBreaker.decorateSupplier(cb,
                        Retry.decorateSupplier(retry, supplier)));
        return decorated.get();
    }

    @DgsQuery
    public UserMediaResponse getUserMedia(
            @InputArgument String userId,
            @InputArgument String filter,
            @InputArgument String albumName,
            @InputArgument Integer limit,
            @InputArgument Integer offset,
            @RequestHeader(name = "X-User-Id", required = false) String xUserId) {

        String effectiveUserId = (xUserId != null && !xUserId.isBlank()) ? xUserId : userId;
        if (effectiveUserId == null || effectiveUserId.isBlank()) {
            UserMediaResponse empty = new UserMediaResponse();
            empty.setItems(Collections.emptyList());
            empty.setTotalCount(0);
            empty.setHasMore(false);
            return empty;
        }

        try {
            GetUserMediaRequest req = GetUserMediaRequest.newBuilder()
                    .setUserId(effectiveUserId)
                    .setFilter(filter != null ? filter : "ALL")
                    .setAlbumName(albumName != null ? albumName : "")
                    .setLimit(limit != null ? limit : 20)
                    .setOffset(offset != null ? offset : 0)
                    .build();

            GetUserMediaResponse res = executeWithResilience(() -> feedGrpcStub.getUserMedia(req));

            List<UserMediaItem> items = new ArrayList<>();
            for (var it : res.getItemsList()) {
                UserMediaItem item = new UserMediaItem();
                item.setId(String.valueOf(it.getId()));
                item.setUserId(it.getUserId());
                item.setPostId(it.getPostId());
                item.setMediaUrl(it.getMediaUrl());
                item.setMediaType(it.getMediaType());
                item.setAlbumName(it.getAlbumName());
                item.setAltText(it.getAltText());
                item.setCreatedAt(it.getCreatedAt());
                item.setTimestamp(it.getTimestamp());
                items.add(item);
            }

            UserMediaResponse response = new UserMediaResponse();
            response.setItems(items);
            response.setTotalCount(res.getTotalCount());
            response.setHasMore(res.getHasMore());
            return response;
        } catch (Exception e) {
            log.error("Failed to fetch user media via gRPC for user: {}", effectiveUserId, e);
            UserMediaResponse fallback = new UserMediaResponse();
            fallback.setItems(Collections.emptyList());
            fallback.setTotalCount(0);
            fallback.setHasMore(false);
            return fallback;
        }
    }

    @DgsQuery
    public List<UserAlbum> getUserAlbums(
            @InputArgument String userId,
            @RequestHeader(name = "X-User-Id", required = false) String xUserId) {
        String effectiveUserId = (xUserId != null && !xUserId.isBlank()) ? xUserId : userId;
        if (effectiveUserId == null || effectiveUserId.isBlank()) {
            return Collections.emptyList();
        }

        try {
            GetUserAlbumsRequest req = GetUserAlbumsRequest.newBuilder()
                    .setUserId(effectiveUserId)
                    .build();

            GetUserAlbumsResponse res = executeWithResilience(() -> feedGrpcStub.getUserAlbums(req));

            List<UserAlbum> albums = new ArrayList<>();
            for (var a : res.getAlbumsList()) {
                UserAlbum album = new UserAlbum();
                album.setName(a.getName());
                album.setCount(a.getCount());
                album.setCoverUrl(a.getCoverUrl());
                albums.add(album);
            }
            return albums;
        } catch (Exception e) {
            log.error("Failed to fetch user albums via gRPC for user: {}", effectiveUserId, e);
            return Collections.emptyList();
        }
    }
}
