package com.facebook.UserEdgeService.datafetcher;

import com.facebook.UserEdgeService.mapper.EdgeMapper;
import com.facebook.user.generated.types.*;
import com.facebook.user.grpc.*;
import com.netflix.graphql.dgs.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@DgsComponent
@RequiredArgsConstructor
public class UserDataFetcher {

    @GrpcClient("user-service")
    private UserGrpcServiceGrpc.UserGrpcServiceBlockingStub userGrpcStub;

    private final EdgeMapper edgeMapper;

    @DgsQuery
    public UserSearchResponse getUserById(@InputArgument String userId) {
        try {
            return edgeMapper.grpcUserToDgsUser(
                    userGrpcStub.getUserById(GetUserByIdRequest.newBuilder().setUserId(userId).build()).getUser()
            );
        } catch (io.grpc.StatusRuntimeException e) {
            if (isUserNotFound(e)) {
                log.debug("User not found: {}", userId);
                return null;
            }
            log.error("Failed to fetch user by ID", e);
            throw new RuntimeException("Core service unavailable: " + e.getMessage());
        } catch (Exception e) {
            if (isUserNotFound(e)) {
                log.debug("User not found: {}", userId);
                return null;
            }
            log.error("Failed to fetch user by ID", e);
            throw new RuntimeException("Core service unavailable: " + e.getMessage());
        }
    }

    @DgsQuery
    public List<UserSearchResponse> searchUsers(@InputArgument String query, @InputArgument String currentUserId) {
        return executeGrpc(() -> userGrpcStub.searchUsers(SearchUsersRequest.newBuilder()
                        .setQuery(query != null ? query : "")
                        .setCurrentUserId(currentUserId != null ? currentUserId : "")
                        .build())
                .getUsersList().stream().map(edgeMapper::grpcUserToDgsUser).toList(), "Failed to search users");
    }

    @DgsQuery
    public List<UserSearchResponse> getSearchHistory(
            @InputArgument String userId,
            @RequestHeader(name = "X-User-Id", required = false) String xUserId) {
        String finalUserId = (xUserId != null && !xUserId.isEmpty()) ? xUserId : userId;
        return executeGrpc(() -> userGrpcStub.getSearchHistory(GetSearchHistoryRequest.newBuilder().setUserId(finalUserId != null ? finalUserId : "").build())
                .getUsersList().stream().map(edgeMapper::grpcUserToDgsUser).toList(), "Failed to fetch search history");
    }

    @DgsQuery
    public List<UserActiveStatus> getActiveStatuses(@InputArgument List<String> userIds) {
        return executeGrpc(() -> userGrpcStub.getActiveStatuses(GetActiveStatusesRequest.newBuilder().addAllUserIds(userIds).build())
                .getStatusesList().stream().map(s -> {
                    UserActiveStatus status = new UserActiveStatus();
                    status.setUserId(s.getUserId());
                    status.setActive(s.getActive());
                    status.setLastActiveText(s.getLastActiveText());
                    status.setLastActiveTimestamp(s.getLastActiveTimestamp());
                    return status;
                }).toList(), "Failed to get active statuses");
    }

    @DgsMutation
    public Boolean setUserActive(@InputArgument String userId) {
        try { userGrpcStub.setUserActive(SetUserActiveRequest.newBuilder().setUserId(userId).build()); return true; }
        catch (Exception e) { log.error("Failed to set user active", e); return false; }
    }

    @DgsMutation
    public Boolean recordSearch(
            @InputArgument String searchedUserId,
            @InputArgument String searchingUserId,
            @RequestHeader(name = "X-User-Id", required = false) String xUserId) {
        try {
            String finalSearchingUserId = (xUserId != null && !xUserId.isEmpty()) ? xUserId : searchingUserId;
            userGrpcStub.recordSearch(RecordSearchRequest.newBuilder()
                    .setSearchedUserId(searchedUserId)
                    .setSearchingUserId(finalSearchingUserId != null ? finalSearchingUserId : "")
                    .build());
            return true;
        }
        catch (Exception e) {
            log.error("Failed to record search", e);
            return false;
        }
    }

    @DgsMutation
    public String generateTicket(
            @InputArgument String userId,
            @RequestHeader(name = "X-User-Id", required = false) String xUserId) {
        String finalUserId = (xUserId != null && !xUserId.isEmpty()) ? xUserId : userId;
        return executeGrpc(() -> userGrpcStub.generateTicket(
                GenerateTicketRequest.newBuilder().setUserId(finalUserId != null ? finalUserId : "").build()).getTicket(),
                "Failed to generate ticket");
    }

    @DgsMutation
    public UserSearchResponse updateProfile(@InputArgument String userId, @InputArgument UpdateProfileInput input) {
        return executeGrpc(() -> edgeMapper.grpcUserToDgsUser(
                userGrpcStub.updateProfile(edgeMapper.buildUpdateProfileRequest(userId, input)).getUser()
        ), "Failed to update profile");
    }

    @DgsQuery
    public String detectLanguage(@InputArgument String text) {
        return executeGrpc(() -> userGrpcStub.detectLanguage(DetectLanguageRequest.newBuilder().setText(text).build()).getLanguageCode(), "Failed to detect language");
    }

    @DgsMutation
    public String translateText(@InputArgument String text, @InputArgument String targetLanguage) {
        return executeGrpc(() -> userGrpcStub.translateText(TranslateTextRequest.newBuilder().setText(text).setTargetLanguage(targetLanguage).build()).getTranslatedText(), "Failed to translate text");
    }

    @DgsQuery
    public List<Notification> getNotifications(@InputArgument String userId) {
        return executeGrpc(() -> {
            var rawList = userGrpcStub.getNotificationsHistory(GetNotificationsHistoryRequest.newBuilder().setUserId(userId).build()).getNotificationsList();
            return rawList.stream().map(n -> {
                Notification dto = edgeMapper.grpcNotificationToDgsNotification(n);
                extractSenderId(n.getMessage()).ifPresent(sId -> {
                    try {
                        var userRes = userGrpcStub.getUserById(GetUserByIdRequest.newBuilder().setUserId(sId).build()).getUser();
                        NotificationUser sender = new NotificationUser();
                        sender.setId(userRes.getId()); sender.setFirstName(userRes.getFirstName());
                        sender.setLastName(userRes.getLastName()); sender.setAvatarId(userRes.getAvatarId());
                        dto.setSender(sender);
                    } catch (Exception ignored) {}
                });
                return dto;
            }).toList();
        }, "Failed to fetch notifications");
    }

    @DgsMutation
    public Boolean markNotificationAsRead(@InputArgument String id) {
        try { userGrpcStub.markNotificationAsRead(MarkNotificationAsReadRequest.newBuilder().setId(id).build()); return true; }
        catch (Exception e) { log.error("Failed to mark notification as read", e); return false; }
    }

    @DgsQuery
    public Listing getListing(@InputArgument String id) {
        return executeGrpc(() -> edgeMapper.grpcListingToDgsListing(
                userGrpcStub.getListing(GetListingRequest.newBuilder().setId(id).build()).getListing()
        ), "Failed to fetch listing");
    }

    @DgsQuery
    public List<Listing> getNearbyListings(@InputArgument Double lat, @InputArgument Double lon, @InputArgument Double radius, @InputArgument String query) {
        return executeGrpc(() -> userGrpcStub.getNearbyListings(GetNearbyListingsRequest.newBuilder()
                        .setLatitude(lat != null ? lat : 0.0)
                        .setLongitude(lon != null ? lon : 0.0)
                        .setRadius(radius != null ? radius : 10000.0)
                        .setQuery(query != null ? query : "")
                        .build())
                .getListingsList().stream().map(edgeMapper::grpcListingToDgsListing).toList(), "Failed to fetch nearby listings");
    }

    @DgsMutation
    public Listing createListing(@InputArgument CreateListingInput input) {
        return executeGrpc(() -> edgeMapper.grpcListingToDgsListing(
                userGrpcStub.createListing(edgeMapper.buildCreateListingRequest(input)).getListing()
        ), "Failed to create listing");
    }

    // ==========================================
    // PAGE OPERATIONS
    // ==========================================

    @DgsQuery
    public List<com.facebook.user.generated.types.Page> pages() {
        return executeGrpc(() -> userGrpcStub.getPagesByOwnerId(
                GetPagesByOwnerIdRequest.newBuilder().setOwnerId("").build()
        ).getPagesList().stream().map(p -> {
            var page = new com.facebook.user.generated.types.Page();
            page.setId(p.getId());
            page.setName(p.getName());
            page.setDomain(p.getDomain());
            page.setOwnerId(p.getOwnerId());
            page.setDescription(p.getDescription());
            page.setAvatarId(p.getAvatarId());
            page.setCreatedAt(p.getCreatedAt());
            page.setUpdatedAt(p.getUpdatedAt());
            return page;
        }).toList(), "Failed to fetch all pages");
    }

    @DgsQuery
    public com.facebook.user.generated.types.Page page(@InputArgument String id) {
        return executeGrpc(() -> {
            var p = userGrpcStub.getPageById(GetPageByIdRequest.newBuilder().setPageId(id).build()).getPage();
            var page = new com.facebook.user.generated.types.Page();
            page.setId(p.getId());
            page.setName(p.getName());
            page.setDomain(p.getDomain());
            page.setOwnerId(p.getOwnerId());
            page.setDescription(p.getDescription());
            page.setAvatarId(p.getAvatarId());
            page.setCreatedAt(p.getCreatedAt());
            page.setUpdatedAt(p.getUpdatedAt());
            return page;
        }, "Failed to fetch page by ID");
    }

    @DgsQuery
    public com.facebook.user.generated.types.Page pageByWebsite(@InputArgument String domain) {
        return executeGrpc(() -> {
            var p = userGrpcStub.getPageByDomain(GetPageByDomainRequest.newBuilder().setDomain(domain).build()).getPage();
            var page = new com.facebook.user.generated.types.Page();
            page.setId(p.getId());
            page.setName(p.getName());
            page.setDomain(p.getDomain());
            page.setOwnerId(p.getOwnerId());
            page.setDescription(p.getDescription());
            page.setAvatarId(p.getAvatarId());
            page.setCreatedAt(p.getCreatedAt());
            page.setUpdatedAt(p.getUpdatedAt());
            return page;
        }, "Failed to fetch page by domain");
    }

    @DgsQuery
    public List<com.facebook.user.generated.types.Page> userPages(@InputArgument String userId) {
        return executeGrpc(() -> userGrpcStub.getPagesByOwnerId(
                GetPagesByOwnerIdRequest.newBuilder().setOwnerId(userId).build()
        ).getPagesList().stream().map(p -> {
            var page = new com.facebook.user.generated.types.Page();
            page.setId(p.getId());
            page.setName(p.getName());
            page.setDomain(p.getDomain());
            page.setOwnerId(p.getOwnerId());
            page.setDescription(p.getDescription());
            page.setAvatarId(p.getAvatarId());
            page.setCreatedAt(p.getCreatedAt());
            page.setUpdatedAt(p.getUpdatedAt());
            return page;
        }).toList(), "Failed to fetch user pages");
    }

    @DgsMutation
    public com.facebook.user.generated.types.Page createPage(
            @InputArgument CreatePageInput input,
            @RequestHeader(name = "X-User-Id", required = false) String userId) {
        return executeGrpc(() -> {
            var p = userGrpcStub.createPage(CreatePageRequest.newBuilder()
                    .setName(input.getName())
                    .setDomain(input.getDomain())
                    .setDescription(input.getDescription() != null ? input.getDescription() : "")
                    .setOwnerId(userId != null ? userId : "")
                    .build()).getPage();
            var page = new com.facebook.user.generated.types.Page();
            page.setId(p.getId());
            page.setName(p.getName());
            page.setDomain(p.getDomain());
            page.setOwnerId(p.getOwnerId());
            page.setDescription(p.getDescription());
            page.setAvatarId(p.getAvatarId());
            page.setCreatedAt(p.getCreatedAt());
            page.setUpdatedAt(p.getUpdatedAt());
            return page;
        }, "Failed to create page");
    }

    @DgsMutation
    public java.util.Map<String, Object> exchangePageToken(
            @InputArgument String pageId,
            @RequestHeader(name = "X-User-Id", required = false) String userId) {
        return executeGrpc(() -> {
            var resp = userGrpcStub.exchangePageToken(ExchangePageTokenRequest.newBuilder()
                    .setPageId(pageId)
                    .setUserId(userId != null ? userId : "")
                    .build());
            var page = new com.facebook.user.generated.types.Page();
            page.setId(resp.getPage().getId());
            page.setName(resp.getPage().getName());
            page.setDomain(resp.getPage().getDomain());
            page.setOwnerId(resp.getPage().getOwnerId());
            return java.util.Map.of(
                    "active", true,
                    "accessToken", resp.getAccessToken(),
                    "tokenType", resp.getTokenType(),
                    "expiresIn", resp.getExpiresIn(),
                    "pageId", pageId,
                    "userId", userId,
                    "page", page
            );
        }, "Failed to exchange page token");
    }

    @DgsMutation
    public java.util.Map<String, Object> verifyPageAccess(
            @InputArgument String pageId,
            @RequestHeader(name = "X-User-Id", required = false) String userId) {
        return executeGrpc(() -> {
            var resp = userGrpcStub.verifyPageAccess(VerifyPageAccessRequest.newBuilder()
                    .setPageId(pageId)
                    .setUserId(userId != null ? userId : "")
                    .build());
            return java.util.Map.of(
                    "pageId", pageId,
                    "userId", userId,
                    "authorized", resp.getAuthorized(),
                    "message", resp.getMessage()
            );
        }, "Failed to verify page access");
    }

    @DgsMutation
    public java.util.Map<String, Object> clearActivePageToken(
            @RequestHeader(name = "X-User-Id", required = false) String userId) {
        return executeGrpc(() -> {
            userGrpcStub.clearActivePageSession(ClearActivePageSessionRequest.newBuilder()
                    .setUserId(userId != null ? userId : "")
                    .build());
            return java.util.Map.of("cleared", true);
        }, "Failed to clear page token");
    }

    // ==========================================
    // VAULT OPERATIONS (E2EE PIN BACKUP)
    // ==========================================

    @DgsQuery
    public java.util.Map<String, Object> vault(@InputArgument String userId) {
        return executeGrpc(() -> {
            var vaultResp = userGrpcStub.getVault(GetVaultRequest.newBuilder().setUserId(userId).build());
            var v = vaultResp.getVault();
            return java.util.Map.of(
                    "userId", v.getUserId(),
                    "opaqueRecord", v.getOpaqueRecord(),
                    "encryptedHistory", v.getEncryptedHistory(),
                    "failedAttempts", v.getFailedAttempts(),
                    "lastUpdated", v.getLastUpdated()
            );
        }, "Failed to fetch vault");
    }

    @DgsMutation
    public java.util.Map<String, Object> saveVault(
            @InputArgument String userId,
            @InputArgument SaveVaultInput input) {
        return executeGrpc(() -> {
            var vaultResp = userGrpcStub.saveVault(SaveVaultRequest.newBuilder()
                    .setUserId(userId)
                    .setOpaqueRecord(input.getOpaqueRecord())
                    .setEncryptedHistory(input.getEncryptedHistory())
                    .setFailedAttempts(input.getFailedAttempts() != null ? input.getFailedAttempts() : 0)
                    .build());
            var v = vaultResp.getVault();
            return java.util.Map.of(
                    "userId", v.getUserId(),
                    "opaqueRecord", v.getOpaqueRecord(),
                    "encryptedHistory", v.getEncryptedHistory(),
                    "failedAttempts", v.getFailedAttempts(),
                    "lastUpdated", v.getLastUpdated()
            );
        }, "Failed to save vault");
    }

    @DgsMutation
    public java.util.Map<String, Object> updateVaultAttempts(
            @InputArgument String userId,
            @InputArgument Integer attempts) {
        return executeGrpc(() -> {
            var vaultResp = userGrpcStub.updateVaultAttempts(UpdateVaultAttemptsRequest.newBuilder()
                    .setUserId(userId)
                    .setAttempts(attempts != null ? attempts : 0)
                    .build());
            var v = vaultResp.getVault();
            return java.util.Map.of(
                    "userId", v.getUserId(),
                    "failedAttempts", v.getFailedAttempts(),
                    "lastUpdated", v.getLastUpdated()
            );
        }, "Failed to update vault attempts");
    }

    @DgsData(parentType = "UserSearchResponse", field = "avatar")
    public String getAvatar(DgsDataFetchingEnvironment dfe) {
        UserSearchResponse user = dfe.getSource();
        if (user == null || user.getAvatarId() == null || user.getAvatarId().isEmpty()) {
            return null;
        }
        try {
            var res = userGrpcStub.resolveMediaUrl(ResolveMediaUrlRequest.newBuilder()
                    .setReference(user.getAvatarId())
                    .build());
            return res.getStableUrl();
        } catch (Exception e) {
            log.error("Failed to resolve avatar URL for user: {}", user.getId(), e);
            return null;
        }
    }

    @DgsData(parentType = "NotificationUser", field = "avatar")
    public String getNotificationUserAvatar(DgsDataFetchingEnvironment dfe) {
        NotificationUser user = dfe.getSource();
        if (user == null || user.getAvatarId() == null || user.getAvatarId().isEmpty()) {
            return null;
        }
        try {
            var res = userGrpcStub.resolveMediaUrl(ResolveMediaUrlRequest.newBuilder()
                    .setReference(user.getAvatarId())
                    .build());
            return res.getStableUrl();
        } catch (Exception e) {
            log.error("Failed to resolve avatar URL for notification user: {}", user.getId(), e);
            return null;
        }
    }

    @DgsData(parentType = "UserSearchResponse", field = "cover")
    public String getCover(DgsDataFetchingEnvironment dfe) {
        UserSearchResponse user = dfe.getSource();
        if (user == null || user.getCoverId() == null || user.getCoverId().isEmpty()) {
            return null;
        }
        try {
            var res = userGrpcStub.resolveMediaUrl(ResolveMediaUrlRequest.newBuilder()
                    .setReference(user.getCoverId())
                    .build());
            return res.getStableUrl();
        } catch (Exception e) {
            log.error("Failed to resolve cover URL for user: {}", user.getId(), e);
            return null;
        }
    }

    private Optional<String> extractSenderId(String msg) {
        if (msg == null || msg.trim().isEmpty()) return Optional.empty();
        try {
            UUID.fromString(msg.trim());
            return Optional.of(msg.trim());
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    private boolean isUserNotFound(Throwable e) {
        String msg = e.getMessage();
        if (msg != null && msg.contains("User not found")) {
            return true;
        }
        if (e instanceof io.grpc.StatusRuntimeException sre) {
            return sre.getStatus().getCode() == io.grpc.Status.Code.NOT_FOUND;
        }
        return e.getCause() != null && isUserNotFound(e.getCause());
    }

    private <T> T executeGrpc(java.util.function.Supplier<T> action, String errorMsg) {
        try { return action.get(); }
        catch (Exception e) { log.error(errorMsg, e); throw new RuntimeException("Core service unavailable: " + e.getMessage()); }
    }
}