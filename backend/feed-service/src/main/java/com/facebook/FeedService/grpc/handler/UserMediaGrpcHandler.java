package com.facebook.FeedService.grpc.handler;

import com.facebook.FeedService.service.UserMediaService;
import com.facebook.feed.grpc.*;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserMediaGrpcHandler {

    private final UserMediaService userMediaService;

    public void getUserMedia(GetUserMediaRequest request, StreamObserver<GetUserMediaResponse> responseObserver) {
        try {
            var page = userMediaService.getUserMedia(
                    request.getUserId(),
                    request.getFilter(),
                    request.getAlbumName(),
                    request.getLimit() > 0 ? request.getLimit() : 20,
                    request.getOffset()
            );

            GetUserMediaResponse.Builder responseBuilder = GetUserMediaResponse.newBuilder()
                    .setTotalCount((int) page.totalCount())
                    .setHasMore(page.hasMore());

            for (var item : page.items()) {
                UserMediaItemDto.Builder itemDto = UserMediaItemDto.newBuilder()
                        .setId(item.getId() != null ? item.getId() : 0)
                        .setUserId(item.getUserId() != null ? item.getUserId() : "")
                        .setPostId(item.getPostId() != null ? item.getPostId() : "")
                        .setMediaUrl(item.getMediaUrl() != null ? item.getMediaUrl() : "")
                        .setMediaType(item.getMediaType() != null ? item.getMediaType() : "IMAGE")
                        .setAlbumName(item.getAlbumName() != null ? item.getAlbumName() : "")
                        .setAltText(item.getAltText() != null ? item.getAltText() : "")
                        .setCreatedAt(item.getCreatedAt() != null ? item.getCreatedAt().toString() : "")
                        .setTimestamp(item.getTimestamp() != null ? item.getTimestamp() : 0);

                responseBuilder.addItems(itemDto.build());
            }

            responseObserver.onNext(responseBuilder.build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("Failed to fetch user media for user {}", request.getUserId(), e);
            responseObserver.onError(io.grpc.Status.INTERNAL.withDescription(e.getMessage()).asRuntimeException());
        }
    }

    public void getUserAlbums(GetUserAlbumsRequest request, StreamObserver<GetUserAlbumsResponse> responseObserver) {
        try {
            var albums = userMediaService.getUserAlbums(request.getUserId());
            GetUserAlbumsResponse.Builder responseBuilder = GetUserAlbumsResponse.newBuilder();

            for (var a : albums) {
                responseBuilder.addAlbums(UserAlbumDto.newBuilder()
                        .setName(a.name())
                        .setCount((int) a.count())
                        .setCoverUrl(a.coverUrl() != null ? a.coverUrl() : "")
                        .build());
            }

            responseObserver.onNext(responseBuilder.build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("Failed to fetch user albums for user {}", request.getUserId(), e);
            responseObserver.onError(io.grpc.Status.INTERNAL.withDescription(e.getMessage()).asRuntimeException());
        }
    }
}
