package com.facebook.UserService.grpc.handler;

import com.facebook.UserService.dto.CreatePageRequest;
import com.facebook.UserService.dto.PageDto;
import com.facebook.UserService.service.PageService;
import com.facebook.UserService.service.PageTokenService;
import com.facebook.user.grpc.*;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class PageManagementGrpcHandler {

    private static final Logger log = LoggerFactory.getLogger(PageManagementGrpcHandler.class);

    private final PageService pageService;
    private final PageTokenService pageTokenService;
    private final GrpcUnaryHelper grpcUnaryHelper;

    public PageManagementGrpcHandler(PageService pageService,
                                     PageTokenService pageTokenService,
                                     GrpcUnaryHelper grpcUnaryHelper) {
        this.pageService = pageService;
        this.pageTokenService = pageTokenService;
        this.grpcUnaryHelper = grpcUnaryHelper;
    }

    public void createPage(com.facebook.user.grpc.CreatePageRequest request, StreamObserver<CreatePageResponse> responseObserver) {
        log.info("gRPC: Creating page: {}", request.getName());
        grpcUnaryHelper.handleUnary(
                () -> {
                    CreatePageRequest pageReq = new CreatePageRequest();
                    pageReq.setName(request.getName());
                    pageReq.setWebsite(request.getDomain());
                    pageReq.setBio(request.getDescription());
                    var pageDto = pageService.createPage(pageReq, request.getOwnerId());
                    return CreatePageResponse.newBuilder()
                            .setPage(mapPageDtoToProto(pageDto))
                            .build();
                },
                responseObserver,
                "Failed to create page via gRPC"
        );
    }

    public void getPageById(GetPageByIdRequest request, StreamObserver<GetPageByIdResponse> responseObserver) {
        log.info("gRPC: Fetching page by ID: {}", request.getPageId());
        grpcUnaryHelper.handleUnary(
                () -> {
                    var pageDto = pageService.getPageById(UUID.fromString(request.getPageId()));
                    return GetPageByIdResponse.newBuilder()
                            .setPage(mapPageDtoToProto(pageDto))
                            .build();
                },
                responseObserver,
                "Failed to get page by ID via gRPC"
        );
    }

    public void getPageByDomain(GetPageByDomainRequest request, StreamObserver<GetPageByDomainResponse> responseObserver) {
        log.info("gRPC: Fetching page by domain: {}", request.getDomain());
        grpcUnaryHelper.handleUnary(
                () -> {
                    var pageDto = pageService.getPageByWebsite(request.getDomain());
                    return GetPageByDomainResponse.newBuilder()
                            .setPage(mapPageDtoToProto(pageDto))
                            .build();
                },
                responseObserver,
                "Failed to get page by domain via gRPC"
        );
    }

    public void getPagesByOwnerId(GetPagesByOwnerIdRequest request, StreamObserver<GetPagesByOwnerIdResponse> responseObserver) {
        log.info("gRPC: Fetching pages for owner: {}", request.getOwnerId());
        grpcUnaryHelper.handleUnary(
                () -> {
                    List<PageDto> pageDtos;
                    String ownerId = request.getOwnerId();
                    if (ownerId == null || ownerId.trim().isEmpty()) {
                        pageDtos = pageService.getAllPages();
                    } else {
                        try {
                            pageDtos = pageService.getPagesByOwnerId(UUID.fromString(ownerId), ownerId);
                        } catch (IllegalArgumentException e) {
                            log.warn("Invalid ownerId UUID format: '{}'. Falling back to all pages.", ownerId);
                            pageDtos = pageService.getAllPages();
                        }
                    }
                    return GetPagesByOwnerIdResponse.newBuilder()
                            .addAllPages(pageDtos.stream().map(this::mapPageDtoToProto).collect(Collectors.toList()))
                            .build();
                },
                responseObserver,
                "Failed to get pages by owner ID via gRPC"
        );
    }

    public void exchangePageToken(ExchangePageTokenRequest request, StreamObserver<ExchangePageTokenResponse> responseObserver) {
        log.info("gRPC: Exchanging page token for pageId: {}, userId: {}", request.getPageId(), request.getUserId());
        grpcUnaryHelper.handleUnary(
                () -> {
                    UUID pageId = UUID.fromString(request.getPageId());
                    UUID userId = UUID.fromString(request.getUserId());
                    var pageDto = pageService.getPageById(pageId);
                    boolean authorized = pageService.isUserAuthorizedForPage(pageId, userId);

                    if (!authorized) {
                        throw new IllegalArgumentException("User not authorized for this page");
                    }

                    String accessToken = pageTokenService.generatePageAccessToken(pageDto, userId);
                    return ExchangePageTokenResponse.newBuilder()
                            .setSuccess(true)
                            .setAccessToken(accessToken)
                            .setTokenType("Bearer")
                            .setExpiresIn(pageTokenService.getPageTokenTtlSeconds())
                            .setPage(mapPageDtoToProto(pageDto))
                            .build();
                },
                responseObserver,
                "Failed to exchange page token via gRPC"
        );
    }

    public void verifyPageAccess(VerifyPageAccessRequest request, StreamObserver<VerifyPageAccessResponse> responseObserver) {
        log.info("gRPC: Verifying page access for pageId: {}, userId: {}", request.getPageId(), request.getUserId());
        grpcUnaryHelper.handleUnary(
                () -> {
                    UUID pageId = UUID.fromString(request.getPageId());
                    UUID userId = UUID.fromString(request.getUserId());
                    boolean authorized = pageService.isUserAuthorizedForPage(pageId, userId);
                    return VerifyPageAccessResponse.newBuilder()
                            .setAuthorized(authorized)
                            .setMessage(authorized ? "Authorized" : "Not authorized")
                            .build();
                },
                responseObserver,
                "Failed to verify page access via gRPC"
        );
    }

    public void clearActivePageSession(ClearActivePageSessionRequest request, StreamObserver<ClearActivePageSessionResponse> responseObserver) {
        log.info("gRPC: Clearing active page session for userId: {}", request.getUserId());
        grpcUnaryHelper.handleUnary(
                () -> {
                    UUID userId = UUID.fromString(request.getUserId());
                    pageTokenService.clearActivePageSession(userId);
                    return ClearActivePageSessionResponse.newBuilder()
                            .setSuccess(true)
                            .build();
                },
                responseObserver,
                "Failed to clear active page session via gRPC"
        );
    }

    public com.facebook.user.grpc.PageDto mapPageDtoToProto(PageDto pageDto) {
        if (pageDto == null) return null;
        return com.facebook.user.grpc.PageDto.newBuilder()
                .setId(pageDto.getId().toString())
                .setName(pageDto.getName())
                .setDomain(pageDto.getWebsite() != null ? pageDto.getWebsite() : "")
                .setOwnerId(pageDto.getOwnerId() != null ? pageDto.getOwnerId().toString() : "")
                .setDescription(pageDto.getBio() != null ? pageDto.getBio() : "")
                .setAvatarId(pageDto.getAvatar() != null ? pageDto.getAvatar() : "")
                .setCreatedAt(pageDto.getCreatedAt() != null ? pageDto.getCreatedAt() : "")
                .setUpdatedAt(pageDto.getUpdatedAt() != null ? pageDto.getUpdatedAt() : "")
                .build();
    }
}
