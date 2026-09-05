package com.facebook.UserService.service;

import com.facebook.chat.grpc.ChatGrpcServiceGrpc;
import com.facebook.chat.grpc.GetMessagesRequest;
import com.facebook.marketplace.grpc.*;
import com.facebook.notification.grpc.*;
import com.facebook.user.grpc.*;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DownstreamGrpcService {

    private static final Logger log = LoggerFactory.getLogger(DownstreamGrpcService.class);

    @GrpcClient("chat-service")
    private ChatGrpcServiceGrpc.ChatGrpcServiceBlockingStub chatGrpcStub;

    @GrpcClient("notification-service")
    private NotificationGrpcServiceGrpc.NotificationGrpcServiceBlockingStub notificationGrpcStub;

    @GrpcClient("marketplace-service")
    private MarketplaceGrpcServiceGrpc.MarketplaceGrpcServiceBlockingStub marketplaceGrpcStub;

    private final UserService userService;
    private final CircuitBreakerRegistry circuitBreakerRegistry;
    private final RetryRegistry retryRegistry;
    private final io.github.resilience4j.bulkhead.BulkheadRegistry bulkheadRegistry;

    public DownstreamGrpcService(UserService userService,
                                  CircuitBreakerRegistry circuitBreakerRegistry,
                                  RetryRegistry retryRegistry,
                                  io.github.resilience4j.bulkhead.BulkheadRegistry bulkheadRegistry) {
        this.userService = userService;
        this.circuitBreakerRegistry = circuitBreakerRegistry;
        this.retryRegistry = retryRegistry;
        this.bulkheadRegistry = bulkheadRegistry;
    }

    private <T> T executeWithResilience(String serviceName, java.util.function.Supplier<T> action) {
        CircuitBreaker cb = circuitBreakerRegistry.circuitBreaker(serviceName);
        Retry retry = retryRegistry.retry(serviceName);
        io.github.resilience4j.bulkhead.Bulkhead bulkhead = bulkheadRegistry.bulkhead(serviceName);
        return CircuitBreaker.decorateSupplier(cb, Retry.decorateSupplier(retry, io.github.resilience4j.bulkhead.Bulkhead.decorateSupplier(bulkhead, action))).get();
    }

    private void executeWithResilienceVoid(String serviceName, Runnable action) {
        CircuitBreaker cb = circuitBreakerRegistry.circuitBreaker(serviceName);
        Retry retry = retryRegistry.retry(serviceName);
        io.github.resilience4j.bulkhead.Bulkhead bulkhead = bulkheadRegistry.bulkhead(serviceName);
        CircuitBreaker.decorateRunnable(cb, Retry.decorateRunnable(retry, io.github.resilience4j.bulkhead.Bulkhead.decorateRunnable(bulkhead, action))).run();
    }

    public List<ChatMessageDto> getChatMessages(String conversationId) {
        try {
            return executeWithResilience("chatService", () -> chatGrpcStub.getMessages(GetMessagesRequest.newBuilder()
                            .setConversationId(conversationId)
                            .build()))
                    .getMessagesList().stream()
                    .map(this::toUserChatMessage)
                    .toList();
        } catch (Exception e) {
            log.error("Failed to fetch chat messages for conversation {}", conversationId, e);
            throw new RuntimeException("Chat service unavailable: " + e.getMessage(), e);
        }
    }

    public List<com.facebook.user.grpc.NotificationDto> getNotificationsHistory(String userId) {
        try {
            return executeWithResilience("notificationService", () -> notificationGrpcStub.getHistory(GetHistoryRequest.newBuilder()
                            .setUserId(userId)
                            .build()))
                    .getNotificationsList().stream()
                    .map(this::toUserNotification)
                    .toList();
        } catch (Exception e) {
            log.error("Failed to fetch notifications for user {}", userId, e);
            throw new RuntimeException("Notification service unavailable: " + e.getMessage(), e);
        }
    }

    public void markNotificationAsRead(String id) {
        try {
            executeWithResilienceVoid("notificationService", () -> notificationGrpcStub.markAsRead(MarkAsReadRequest.newBuilder()
                    .setId(Long.parseLong(id))
                    .build()));
        } catch (Exception e) {
            log.error("Failed to mark notification {} as read", id, e);
            throw new RuntimeException("Notification service unavailable: " + e.getMessage(), e);
        }
    }

    public ListingDto getListing(String id) {
        try {
            MarketplaceItemDto item = executeWithResilience("marketplaceService", () -> marketplaceGrpcStub.getItem(GetItemRequest.newBuilder()
                    .setId(id)
                    .build())).getItem();
            return toUserListing(item);
        } catch (Exception e) {
            log.error("Failed to fetch listing {}", id, e);
            throw new RuntimeException("Marketplace service unavailable: " + e.getMessage(), e);
        }
    }

    public List<ListingDto> getNearbyListings(double latitude, double longitude, double radius, String query) {
        try {
            SearchItemsRequest.Builder requestBuilder = SearchItemsRequest.newBuilder()
                    .setLatitude(latitude)
                    .setLongitude(longitude)
                    .setRadiusKm(radius / 1000.0);
            if (query != null && !query.isBlank()) {
                requestBuilder.setQuery(query);
            }
            return executeWithResilience("marketplaceService", () -> marketplaceGrpcStub.searchItems(requestBuilder.build()))
                    .getItemsList().stream()
                    .map(this::toUserListing)
                    .toList();
        } catch (Exception e) {
            log.error("Failed to fetch nearby listings", e);
            throw new RuntimeException("Marketplace service unavailable: " + e.getMessage(), e);
        }
    }

    public ListingDto createListing(CreateListingRequest request) {
        try {
            MarketplaceItemDto item = executeWithResilience("marketplaceService", () -> marketplaceGrpcStub.createItem(CreateItemRequest.newBuilder()
                    .setTitle(request.getTitle())
                    .setPrice(request.getPrice())
                    .setCategory(request.getCategory())
                    .setCondition(request.getCondition())
                    .setDescription(request.getDescription())
                    .setLatitude(request.getLatitude())
                    .setLongitude(request.getLongitude())
                    .build())).getItem();
            return toUserListing(item);
        } catch (Exception e) {
            log.error("Failed to create listing", e);
            throw new RuntimeException("Marketplace service unavailable: " + e.getMessage(), e);
        }
    }

    private ChatMessageDto toUserChatMessage(com.facebook.chat.grpc.ChatMessageDto msg) {
        return ChatMessageDto.newBuilder()
                .setMessageId(msg.getMessageId())
                .setSenderId(msg.getSenderId())
                .setText(msg.getText())
                .setReplyToId(msg.getReplyToId())
                .setReplyToText(msg.getReplyToText())
                .setReplyToSenderId(msg.getReplyToSenderId())
                .setImageUrl(resolveMediaField(msg.getImageUrl()))
                .setAudioUrl(resolveMediaField(msg.getAudioUrl()))
                .setFileUrl(resolveMediaField(msg.getFileUrl()))
                .setFileName(msg.getFileName())
                .setFileSize(msg.getFileSize())
                .setLinkUrl(msg.getLinkUrl())
                .setDuration(msg.getDuration())
                .setIsPinned(msg.getIsPinned())
                .setTime(msg.getTime())
                .putAllReactions(msg.getReactionsMap())
                .build();
    }

    private String resolveMediaField(String storedValue) {
        if (storedValue == null || storedValue.isBlank()) {
            return storedValue;
        }
        try {
            return userService.resolveMediaUrlForClient(storedValue);
        } catch (Exception e) {
            log.warn("Failed to refresh media URL, returning stored value: {}", e.getMessage());
            return storedValue;
        }
    }

    private com.facebook.user.grpc.NotificationDto toUserNotification(com.facebook.notification.grpc.NotificationDto notification) {
        return com.facebook.user.grpc.NotificationDto.newBuilder()
                .setId(notification.getId())
                .setUserId(notification.getUserId())
                .setTitle(notification.getTitle())
                .setMessage(notification.getMessage())
                .setRead(notification.getRead())
                .setCreatedAt(notification.getCreatedAt())
                .build();
    }

    private ListingDto toUserListing(MarketplaceItemDto item) {
        return ListingDto.newBuilder()
                .setId(item.getId())
                .setTitle(item.getTitle())
                .setPrice(item.getPrice())
                .setCategory(item.getCategory())
                .setCondition(item.getCondition())
                .setDescription(item.getDescription())
                .setLatitude(item.getLatitude())
                .setLongitude(item.getLongitude())
                .setCreatedAt(item.getCreatedAt())
                .build();
    }
}
