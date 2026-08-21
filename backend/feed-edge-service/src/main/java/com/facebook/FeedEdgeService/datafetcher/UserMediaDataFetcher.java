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
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@DgsComponent
@Slf4j
public class UserMediaDataFetcher {

    @GrpcClient("feed-service")
    private FeedGrpcServiceGrpc.FeedGrpcServiceBlockingStub feedGrpcStub;

    @DgsQuery
    public UserMediaResponse getUserMedia(
            @InputArgument String userId,
            @InputArgument String filter,
            @InputArgument String albumName,
            @InputArgument Integer limit,
            @InputArgument Integer offset) {

        try {
            GetUserMediaRequest req = GetUserMediaRequest.newBuilder()
                    .setUserId(userId)
                    .setFilter(filter != null ? filter : "ALL")
                    .setAlbumName(albumName != null ? albumName : "")
                    .setLimit(limit != null ? limit : 20)
                    .setOffset(offset != null ? offset : 0)
                    .build();

            GetUserMediaResponse res = feedGrpcStub.getUserMedia(req);

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
            log.error("Failed to fetch user media via gRPC for user: {}", userId, e);
            UserMediaResponse fallback = new UserMediaResponse();
            fallback.setItems(Collections.emptyList());
            fallback.setTotalCount(0);
            fallback.setHasMore(false);
            return fallback;
        }
    }

    @DgsQuery
    public List<UserAlbum> getUserAlbums(@InputArgument String userId) {
        try {
            GetUserAlbumsRequest req = GetUserAlbumsRequest.newBuilder()
                    .setUserId(userId)
                    .build();

            GetUserAlbumsResponse res = feedGrpcStub.getUserAlbums(req);

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
            log.error("Failed to fetch user albums via gRPC for user: {}", userId, e);
            return Collections.emptyList();
        }
    }
}
