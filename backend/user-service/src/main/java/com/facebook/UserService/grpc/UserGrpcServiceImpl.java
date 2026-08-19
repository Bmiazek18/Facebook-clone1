package com.facebook.UserService.grpc;

import com.facebook.UserService.service.DownstreamGrpcService;
import com.facebook.UserService.service.PageService;
import com.facebook.UserService.service.PageTokenService;
import com.facebook.UserService.service.TicketService;
import com.facebook.UserService.service.TranslationService;
import com.facebook.UserService.service.UserActiveService;
import com.facebook.UserService.service.UserService;
import com.facebook.user.grpc.*;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@GrpcService
@RequiredArgsConstructor
@Slf4j
public class UserGrpcServiceImpl extends UserGrpcServiceGrpc.UserGrpcServiceImplBase {

    private final UserService userService;
    private final UserActiveService userActiveService;
    private final TranslationService translationService;
    private final DownstreamGrpcService downstreamGrpcService;
    private final TicketService ticketService;
    private final PageService pageService;
    private final PageTokenService pageTokenService;

    @Override
    public void getUserById(GetUserByIdRequest request, StreamObserver<GetUserByIdResponse> responseObserver) {
        log.info("gRPC: Fetching user profile by ID: {}", request.getUserId());
        handleUnary(
                () -> GetUserByIdResponse.newBuilder()
                        .setUser(userService.getUserProfileById(UUID.fromString(request.getUserId())))
                        .build(),
                responseObserver,
                "Failed to get user by ID via gRPC"
        );
    }

    @Override
    public void recordSearch(RecordSearchRequest request, StreamObserver<RecordSearchResponse> responseObserver) {
        log.info("gRPC: Recording search: searchedUser={}, searchingUser={}", request.getSearchedUserId(), request.getSearchingUserId());
        handleUnary(
                () -> {
                    UUID searchedUserId = UUID.fromString(request.getSearchedUserId());
                    UUID searchingUserId = !request.getSearchingUserId().isEmpty() ? UUID.fromString(request.getSearchingUserId()) : null;
                    userService.searchUserById(searchedUserId, searchingUserId);
                    return RecordSearchResponse.newBuilder().setSuccess(true).build();
                },
                responseObserver,
                "Failed to record search via gRPC"
        );
    }

    @Override
    public void generateTicket(GenerateTicketRequest request, StreamObserver<GenerateTicketResponse> responseObserver) {
        log.info("gRPC: Generating one-time ticket for user: {}", request.getUserId());
        handleUnary(
                () -> GenerateTicketResponse.newBuilder()
                        .setTicket(ticketService.generateTicket(request.getUserId()))
                        .build(),
                responseObserver,
                "Failed to generate ticket via gRPC"
        );
    }

    @Override
    public void resolveMediaUrl(ResolveMediaUrlRequest request, StreamObserver<ResolveMediaUrlResponse> responseObserver) {
        log.debug("gRPC: Resolving media reference");
        handleUnary(
                () -> {
                    var access = userService.resolveMediaAccess(request.getReference());
                    return ResolveMediaUrlResponse.newBuilder()
                            .setObjectKey(access.objectKey() != null ? access.objectKey() : "")
                            .setStableUrl(access.stableUrl() != null ? access.stableUrl() : "")
                            .setPresignedUrl(access.presignedUrl() != null ? access.presignedUrl() : "")
                            .setExpiresAt(access.expiresAt())
                            .build();
                },
                responseObserver,
                "Failed to resolve media URL via gRPC"
        );
    }

    @Override
    public void searchUsers(SearchUsersRequest request, StreamObserver<SearchUsersResponse> responseObserver) {
        log.info("gRPC: Searching users with query: {}, currentUserId: {}", request.getQuery(), request.getCurrentUserId());
        handleUnary(
                () -> {
                    UUID currentUserId = request.getCurrentUserId().isEmpty()
                            ? null
                            : UUID.fromString(request.getCurrentUserId());
                    return SearchUsersResponse.newBuilder()
                            .addAllUsers(userService.searchUsers(request.getQuery(), currentUserId))
                            .build();
                },
                responseObserver,
                "Failed to search users via gRPC"
        );
    }

    @Override
    public void getSearchHistory(GetSearchHistoryRequest request, StreamObserver<GetSearchHistoryResponse> responseObserver) {
        log.info("gRPC: Fetching search history");
        handleUnary(
                () -> {
                    UUID userId = request.getUserId().isEmpty()
                            ? null
                            : UUID.fromString(request.getUserId());
                    return GetSearchHistoryResponse.newBuilder()
                            .addAllUsers(userService.getSearchHistory(userId))
                            .build();
                },
                responseObserver,
                "Failed to fetch search history via gRPC"
        );
    }

    @Override
    public void getActiveStatuses(GetActiveStatusesRequest request, StreamObserver<GetActiveStatusesResponse> responseObserver) {
        log.info("gRPC: Fetching active statuses for {} users", request.getUserIdsCount());
        handleUnary(
                () -> {
                    List<UUID> ids = request.getUserIdsList().stream()
                            .map(UUID::fromString)
                            .collect(Collectors.toList());
                    return GetActiveStatusesResponse.newBuilder()
                            .addAllStatuses(userActiveService.getActiveStatuses(ids))
                            .build();
                },
                responseObserver,
                "Failed to get active statuses via gRPC"
        );
    }

    @Override
    public void setUserActive(SetUserActiveRequest request, StreamObserver<SetUserActiveResponse> responseObserver) {
        log.info("gRPC: Setting user active: {}", request.getUserId());
        handleUnary(
                () -> {
                    userActiveService.setUserActive(UUID.fromString(request.getUserId()));
                    return SetUserActiveResponse.newBuilder().setSuccess(true).build();
                },
                responseObserver,
                "Failed to set user active via gRPC"
        );
    }

    @Override
    public void updateProfile(UpdateProfileRequest request, StreamObserver<UpdateProfileResponse> responseObserver) {
        log.info("gRPC: Updating profile for user: {}", request.getUserId());
        handleUnary(
                () -> UpdateProfileResponse.newBuilder()
                        .setUser(userService.updateProfile(UUID.fromString(request.getUserId()), request))
                        .build(),
                responseObserver,
                "Failed to update profile via gRPC"
        );
    }

    @Override
    public void detectLanguage(DetectLanguageRequest request, StreamObserver<DetectLanguageResponse> responseObserver) {
        log.info("gRPC: Detecting language for text length: {}", request.getText().length());
        handleUnary(
                () -> DetectLanguageResponse.newBuilder()
                        .setLanguageCode(translationService.detectLanguage(request.getText()))
                        .build(),
                responseObserver,
                "Failed to detect language via gRPC"
        );
    }

    @Override
    public void translateText(TranslateTextRequest request, StreamObserver<TranslateTextResponse> responseObserver) {
        log.info("gRPC: Translating text length: {} to {}", request.getText().length(), request.getTargetLanguage());
        handleUnary(
                () -> TranslateTextResponse.newBuilder()
                        .setTranslatedText(translationService.translateText(request.getText(), request.getTargetLanguage()))
                        .build(),
                responseObserver,
                "Failed to translate text via gRPC"
        );
    }

    @Override
    public void getChatMessages(GetChatMessagesRequest request, StreamObserver<GetChatMessagesResponse> responseObserver) {
        log.info("gRPC: Fetching chat messages for conversation: {}", request.getConversationId());
        handleUnary(
                () -> GetChatMessagesResponse.newBuilder()
                        .addAllMessages(downstreamGrpcService.getChatMessages(request.getConversationId()))
                        .build(),
                responseObserver,
                "Failed to fetch chat messages via gRPC"
        );
    }

    @Override
    public void getNotificationsHistory(GetNotificationsHistoryRequest request, StreamObserver<GetNotificationsHistoryResponse> responseObserver) {
        log.info("gRPC: Fetching notifications for user: {}", request.getUserId());
        handleUnary(
                () -> GetNotificationsHistoryResponse.newBuilder()
                        .addAllNotifications(downstreamGrpcService.getNotificationsHistory(request.getUserId()))
                        .build(),
                responseObserver,
                "Failed to fetch notifications via gRPC"
        );
    }

    @Override
    public void markNotificationAsRead(MarkNotificationAsReadRequest request, StreamObserver<MarkNotificationAsReadResponse> responseObserver) {
        log.info("gRPC: Marking notification as read: {}", request.getId());
        handleUnary(
                () -> {
                    downstreamGrpcService.markNotificationAsRead(request.getId());
                    return MarkNotificationAsReadResponse.newBuilder().setSuccess(true).build();
                },
                responseObserver,
                "Failed to mark notification as read via gRPC"
        );
    }

    @Override
    public void getListing(GetListingRequest request, StreamObserver<GetListingResponse> responseObserver) {
        log.info("gRPC: Fetching listing: {}", request.getId());
        handleUnary(
                () -> GetListingResponse.newBuilder()
                        .setListing(downstreamGrpcService.getListing(request.getId()))
                        .build(),
                responseObserver,
                "Failed to fetch listing via gRPC"
        );
    }

    @Override
    public void getNearbyListings(GetNearbyListingsRequest request, StreamObserver<GetNearbyListingsResponse> responseObserver) {
        log.info("gRPC: Fetching nearby listings");
        handleUnary(
                () -> GetNearbyListingsResponse.newBuilder()
                        .addAllListings(downstreamGrpcService.getNearbyListings(
                                request.getLatitude(),
                                request.getLongitude(),
                                request.getRadius(),
                                request.getQuery()))
                        .build(),
                responseObserver,
                "Failed to fetch nearby listings via gRPC"
        );
    }

    @Override
    public void createListing(CreateListingRequest request, StreamObserver<CreateListingResponse> responseObserver) {
        log.info("gRPC: Creating listing: {}", request.getTitle());
        handleUnary(
                () -> CreateListingResponse.newBuilder()
                        .setListing(downstreamGrpcService.createListing(request))
                        .build(),
                responseObserver,
                "Failed to create listing via gRPC"
        );
    }

    // ==========================================
    // PAGE OPERATIONS
    // ==========================================

    @Override
    public void createPage(CreatePageRequest request, StreamObserver<CreatePageResponse> responseObserver) {
        log.info("gRPC: Creating page: {}", request.getName());
        handleUnary(
                () -> {
                    com.facebook.UserService.dto.CreatePageRequest pageReq = new com.facebook.UserService.dto.CreatePageRequest();
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

    @Override
    public void getPageById(GetPageByIdRequest request, StreamObserver<GetPageByIdResponse> responseObserver) {
        log.info("gRPC: Fetching page by ID: {}", request.getPageId());
        handleUnary(
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

    @Override
    public void getPageByDomain(GetPageByDomainRequest request, StreamObserver<GetPageByDomainResponse> responseObserver) {
        log.info("gRPC: Fetching page by domain: {}", request.getDomain());
        handleUnary(
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

    @Override
    public void getPagesByOwnerId(GetPagesByOwnerIdRequest request, StreamObserver<GetPagesByOwnerIdResponse> responseObserver) {
        log.info("gRPC: Fetching pages for owner: {}", request.getOwnerId());
        handleUnary(
                () -> {
                    List<com.facebook.UserService.dto.PageDto> pageDtos;
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

    @Override
    public void exchangePageToken(ExchangePageTokenRequest request, StreamObserver<ExchangePageTokenResponse> responseObserver) {
        log.info("gRPC: Exchanging page token for pageId: {}, userId: {}", request.getPageId(), request.getUserId());
        handleUnary(
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

    @Override
    public void verifyPageAccess(VerifyPageAccessRequest request, StreamObserver<VerifyPageAccessResponse> responseObserver) {
        log.info("gRPC: Verifying page access for pageId: {}, userId: {}", request.getPageId(), request.getUserId());
        handleUnary(
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

    @Override
    public void clearActivePageSession(ClearActivePageSessionRequest request, StreamObserver<ClearActivePageSessionResponse> responseObserver) {
        log.info("gRPC: Clearing active page session for userId: {}", request.getUserId());
        handleUnary(
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

    // ==========================================
    // VAULT OPERATIONS (E2EE PIN BACKUP)
    // ==========================================

    @Override
    public void getVault(GetVaultRequest request, StreamObserver<GetVaultResponse> responseObserver) {
        log.info("gRPC: Fetching vault for userId: {}", request.getUserId());
        handleUnary(
                () -> {
                    UUID userId = UUID.fromString(request.getUserId());
                    var vaultData = userService.getVault(userId);
                    return GetVaultResponse.newBuilder()
                            .setVault(mapVaultDataToProto(userId, vaultData))
                            .build();
                },
                responseObserver,
                "Failed to get vault via gRPC"
        );
    }

    @Override
    public void saveVault(SaveVaultRequest request, StreamObserver<SaveVaultResponse> responseObserver) {
        log.info("gRPC: Saving vault for userId: {}", request.getUserId());
        handleUnary(
                () -> {
                    UUID userId = UUID.fromString(request.getUserId());
                    userService.saveVault(userId, request.getOpaqueRecord(), request.getEncryptedHistory(), request.getFailedAttempts());
                    return SaveVaultResponse.newBuilder()
                            .setVault(mapVaultDataToProto(userId, userService.getVault(userId)))
                            .build();
                },
                responseObserver,
                "Failed to save vault via gRPC"
        );
    }

    @Override
    public void updateVaultAttempts(UpdateVaultAttemptsRequest request, StreamObserver<UpdateVaultAttemptsResponse> responseObserver) {
        log.info("gRPC: Updating vault attempts for userId: {}", request.getUserId());
        handleUnary(
                () -> {
                    UUID userId = UUID.fromString(request.getUserId());
                    userService.updateVaultAttempts(userId, request.getAttempts());
                    return UpdateVaultAttemptsResponse.newBuilder()
                            .setVault(mapVaultDataToProto(userId, userService.getVault(userId)))
                            .build();
                },
                responseObserver,
                "Failed to update vault attempts via gRPC"
        );
    }

    // ==========================================
    // HELPER METHODS
    // ==========================================

    private PageDto mapPageDtoToProto(com.facebook.UserService.dto.PageDto pageDto) {
        if (pageDto == null) return null;
        return PageDto.newBuilder()
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

    private VaultData mapVaultDataToProto(UUID userId, java.util.Map<String, Object> vaultMap) {
        if (vaultMap == null || vaultMap.isEmpty()) {
            return VaultData.newBuilder()
                    .setUserId(userId.toString())
                    .build();
        }
        return VaultData.newBuilder()
                .setUserId(userId.toString())
                .setOpaqueRecord((String) vaultMap.getOrDefault("opaqueRecord", ""))
                .setEncryptedHistory((String) vaultMap.getOrDefault("encryptedHistory", ""))
                .setFailedAttempts((Integer) vaultMap.getOrDefault("failedAttempts", 0))
                .setLastUpdated(String.valueOf(vaultMap.getOrDefault("lastUpdated", System.currentTimeMillis())))
                .build();
    }

    private <T> void handleUnary(java.util.function.Supplier<T> action,
                                 StreamObserver<T> responseObserver,
                                 String errorMsg) {
        try {
            responseObserver.onNext(action.get());
            responseObserver.onCompleted();
        } catch (IllegalArgumentException e) {
            if (e.getMessage() != null && e.getMessage().contains("User not found")) {
                responseObserver.onError(io.grpc.Status.NOT_FOUND
                        .withDescription(e.getMessage())
                        .asRuntimeException());
                return;
            }
            log.error(errorMsg, e);
            responseObserver.onError(io.grpc.Status.INVALID_ARGUMENT
                    .withDescription(e.getMessage())
                    .asRuntimeException());
        } catch (Exception e) {
            log.error(errorMsg, e);
            responseObserver.onError(io.grpc.Status.INTERNAL
                    .withDescription("Internal error: " + e.getMessage())
                    .asRuntimeException());
        }
    }
}
