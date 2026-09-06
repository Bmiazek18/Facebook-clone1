package com.facebook.UserService.grpc.handler;

import com.facebook.UserService.service.DownstreamGrpcService;
import com.facebook.UserService.service.TicketService;
import com.facebook.UserService.service.TranslationService;
import com.facebook.UserService.service.UserService;
import com.facebook.user.grpc.*;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserUtilityAndVaultGrpcHandler {

    private final UserService userService;
    private final DownstreamGrpcService downstreamGrpcService;
    private final TicketService ticketService;
    private final TranslationService translationService;
    private final GrpcUnaryHelper grpcUnaryHelper;

    public void generateTicket(GenerateTicketRequest request, StreamObserver<GenerateTicketResponse> responseObserver) {
        log.info("gRPC: Generating one-time ticket for user: {}", request.getUserId());
        grpcUnaryHelper.handleUnary(
                () -> GenerateTicketResponse.newBuilder()
                        .setTicket(ticketService.generateTicket(request.getUserId()))
                        .build(),
                responseObserver,
                "Failed to generate ticket via gRPC"
        );
    }

    public void resolveMediaUrl(ResolveMediaUrlRequest request, StreamObserver<ResolveMediaUrlResponse> responseObserver) {
        log.debug("gRPC: Resolving media reference");
        grpcUnaryHelper.handleUnary(
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

    public void detectLanguage(DetectLanguageRequest request, StreamObserver<DetectLanguageResponse> responseObserver) {
        log.info("gRPC: Detecting language for text length: {}", request.getText().length());
        grpcUnaryHelper.handleUnary(
                () -> DetectLanguageResponse.newBuilder()
                        .setLanguageCode(translationService.detectLanguage(request.getText()))
                        .build(),
                responseObserver,
                "Failed to detect language via gRPC"
        );
    }

    public void translateText(TranslateTextRequest request, StreamObserver<TranslateTextResponse> responseObserver) {
        log.info("gRPC: Translating text length: {} to {}", request.getText().length(), request.getTargetLanguage());
        grpcUnaryHelper.handleUnary(
                () -> TranslateTextResponse.newBuilder()
                        .setTranslatedText(translationService.translateText(request.getText(), request.getTargetLanguage()))
                        .build(),
                responseObserver,
                "Failed to translate text via gRPC"
        );
    }

    public void getChatMessages(GetChatMessagesRequest request, StreamObserver<GetChatMessagesResponse> responseObserver) {
        log.info("gRPC: Fetching chat messages for conversation: {}", request.getConversationId());
        grpcUnaryHelper.handleUnary(
                () -> GetChatMessagesResponse.newBuilder()
                        .addAllMessages(downstreamGrpcService.getChatMessages(request.getConversationId()))
                        .build(),
                responseObserver,
                "Failed to fetch chat messages via gRPC"
        );
    }

    public void getNotificationsHistory(GetNotificationsHistoryRequest request, StreamObserver<GetNotificationsHistoryResponse> responseObserver) {
        log.info("gRPC: Fetching notifications for user: {}", request.getUserId());
        grpcUnaryHelper.handleUnary(
                () -> GetNotificationsHistoryResponse.newBuilder()
                        .addAllNotifications(downstreamGrpcService.getNotificationsHistory(request.getUserId()))
                        .build(),
                responseObserver,
                "Failed to fetch notifications via gRPC"
        );
    }

    public void markNotificationAsRead(MarkNotificationAsReadRequest request, StreamObserver<MarkNotificationAsReadResponse> responseObserver) {
        log.info("gRPC: Marking notification as read: {}", request.getId());
        grpcUnaryHelper.handleUnary(
                () -> {
                    downstreamGrpcService.markNotificationAsRead(request.getId());
                    return MarkNotificationAsReadResponse.newBuilder().setSuccess(true).build();
                },
                responseObserver,
                "Failed to mark notification as read via gRPC"
        );
    }

    public void getListing(GetListingRequest request, StreamObserver<GetListingResponse> responseObserver) {
        log.info("gRPC: Fetching listing: {}", request.getId());
        grpcUnaryHelper.handleUnary(
                () -> GetListingResponse.newBuilder()
                        .setListing(downstreamGrpcService.getListing(request.getId()))
                        .build(),
                responseObserver,
                "Failed to fetch listing via gRPC"
        );
    }

    public void getNearbyListings(GetNearbyListingsRequest request, StreamObserver<GetNearbyListingsResponse> responseObserver) {
        log.info("gRPC: Fetching nearby listings");
        grpcUnaryHelper.handleUnary(
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

    public void createListing(CreateListingRequest request, StreamObserver<CreateListingResponse> responseObserver) {
        log.info("gRPC: Creating listing: {}", request.getTitle());
        grpcUnaryHelper.handleUnary(
                () -> CreateListingResponse.newBuilder()
                        .setListing(downstreamGrpcService.createListing(request))
                        .build(),
                responseObserver,
                "Failed to create listing via gRPC"
        );
    }

    public void getVault(GetVaultRequest request, StreamObserver<GetVaultResponse> responseObserver) {
        log.info("gRPC: Fetching vault for userId: {}", request.getUserId());
        grpcUnaryHelper.handleUnary(
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

    public void saveVault(SaveVaultRequest request, StreamObserver<SaveVaultResponse> responseObserver) {
        log.info("gRPC: Saving vault for userId: {}", request.getUserId());
        grpcUnaryHelper.handleUnary(
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

    public void updateVaultAttempts(UpdateVaultAttemptsRequest request, StreamObserver<UpdateVaultAttemptsResponse> responseObserver) {
        log.info("gRPC: Updating vault attempts for userId: {}", request.getUserId());
        grpcUnaryHelper.handleUnary(
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

    private VaultData mapVaultDataToProto(UUID userId, Map<String, Object> vaultMap) {
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
}
