package com.facebook.ChatEdgeService.datafetcher;

import com.facebook.ChatEdgeService.codegen.types.ChatMessage;
import com.facebook.ChatEdgeService.codegen.types.ChatUser;
import com.facebook.ChatEdgeService.codegen.types.ChatWithUserResponse;
import com.facebook.ChatEdgeService.codegen.types.SendChatMessageInput;
import com.facebook.ChatEdgeService.mapper.ChatMapper;
import com.facebook.chat.grpc.ChatGrpcServiceGrpc;
import com.facebook.chat.grpc.GetMessagesRequest;
import com.facebook.chat.grpc.SendMessageRequest;
import com.facebook.user.grpc.GetUserByIdRequest;
import com.facebook.user.grpc.ResolveMediaUrlRequest;
import com.facebook.user.grpc.UserGrpcServiceGrpc;
import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsData;
import com.netflix.graphql.dgs.DgsDataFetchingEnvironment;
import com.netflix.graphql.dgs.DgsMutation;
import com.netflix.graphql.dgs.DgsQuery;
import com.netflix.graphql.dgs.InputArgument;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryRegistry;
import io.github.resilience4j.bulkhead.Bulkhead;
import io.github.resilience4j.bulkhead.BulkheadRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.List;

@DgsComponent
public class ChatDataFetcher {

    private static final Logger log = LoggerFactory.getLogger(ChatDataFetcher.class);

    @GrpcClient("chat-service")
    private ChatGrpcServiceGrpc.ChatGrpcServiceBlockingStub chatGrpcStub;

    @GrpcClient("user-service")
    private UserGrpcServiceGrpc.UserGrpcServiceBlockingStub userGrpcStub;

    private final ChatMapper chatMapper;
    private final CircuitBreakerRegistry circuitBreakerRegistry;
    private final RetryRegistry retryRegistry;
    private final BulkheadRegistry bulkheadRegistry;

    public ChatDataFetcher(
            ChatMapper chatMapper,
            CircuitBreakerRegistry circuitBreakerRegistry,
            RetryRegistry retryRegistry,
            BulkheadRegistry bulkheadRegistry) {
        this.chatMapper = chatMapper;
        this.circuitBreakerRegistry = circuitBreakerRegistry;
        this.retryRegistry = retryRegistry;
        this.bulkheadRegistry = bulkheadRegistry;
    }

    @DgsQuery
    public ChatWithUserResponse getChatWithUser(@InputArgument String userId, @InputArgument String conversationId) {
        ChatWithUserResponse response = new ChatWithUserResponse();

        if (userId != null && userId.equals(conversationId)) {
            ChatUser chatUser = new ChatUser();
            chatUser.setId(userId);
            response.setUser(chatUser);
        } else {
            try {
                var user = userGrpcStub.getUserById(GetUserByIdRequest.newBuilder().setUserId(userId).build()).getUser();
                ChatUser chatUser = new ChatUser();
                chatUser.setId(user.getId());
                chatUser.setFirstName(user.getFirstName());
                chatUser.setLastName(user.getLastName());
                chatUser.setAvatarId(user.getAvatarId());
                response.setUser(chatUser);
            } catch (Exception e) {
                log.error("Failed to fetch user {} for chat", userId, e);
                ChatUser chatUser = new ChatUser();
                chatUser.setId(userId);
                response.setUser(chatUser);
            }
        }

        response.setMessages(fetchMessages(conversationId));
        response.setSettings(getChatSettings(conversationId));
        return response;
    }

    @DgsData(parentType = "ChatUser", field = "avatar")
    public String getChatUserAvatar(DgsDataFetchingEnvironment dfe) {
        ChatUser user = dfe.getSource();
        if (user == null || user.getAvatarId() == null || user.getAvatarId().isEmpty()) {
            return null;
        }
        try {
            var res = userGrpcStub.resolveMediaUrl(ResolveMediaUrlRequest.newBuilder()
                    .setReference(user.getAvatarId())
                    .build());
            return res.getStableUrl();
        } catch (Exception e) {
            log.error("Failed to resolve avatar URL for chat user: {}", user.getId(), e);
            return null;
        }
    }

    @DgsQuery
    public List<ChatMessage> getChatMessages(@InputArgument String conversationId) {
        return fetchMessages(conversationId);
    }

    @DgsQuery
    public List<com.facebook.ChatEdgeService.codegen.types.InboxItem> getInbox(
            @InputArgument String userId,
            @RequestHeader(name = "X-User-Id", required = false) String xUserId) {
        String effectiveUserId = (xUserId != null && !xUserId.isBlank()) ? xUserId : userId;
        log.info("Edge: Fetching inbox for user: {}", effectiveUserId);
        return executeGrpc(() -> {
            var response = chatGrpcStub.getInbox(com.facebook.chat.grpc.GetInboxRequest.newBuilder()
                    .setUserId(effectiveUserId != null ? effectiveUserId : "")
                    .build());
            return response.getItemsList().stream()
                    .map(item -> {
                        var gqlItem = new com.facebook.ChatEdgeService.codegen.types.InboxItem();
                        gqlItem.setConversationId(item.getConversationId());
                        gqlItem.setLastActivity((double) item.getLastActivity());
                        gqlItem.setLastMessageText(item.getLastMessageText());
                        gqlItem.setIsUnread(item.getIsUnread());
                        gqlItem.setRecipientId(item.getRecipientId().isEmpty() ? null : item.getRecipientId());
                        gqlItem.setLastMessageSenderId(item.getLastMessageSenderId().isEmpty() ? null : item.getLastMessageSenderId());
                        return gqlItem;
                    })
                    .toList();
        }, "Failed to fetch inbox via gRPC");
    }

    @DgsMutation
    public Boolean markInboxAsRead(
            @InputArgument String userId,
            @InputArgument String conversationId,
            @RequestHeader(name = "X-User-Id", required = false) String xUserId) {
        String effectiveUserId = (xUserId != null && !xUserId.isBlank()) ? xUserId : userId;
        log.info("Edge: Marking conversation {} as read for user {}", conversationId, effectiveUserId);
        try {
            return executeWithResilience(() -> {
                var response = chatGrpcStub.markAsRead(com.facebook.chat.grpc.MarkAsReadRequest.newBuilder()
                        .setUserId(effectiveUserId != null ? effectiveUserId : "")
                        .setConversationId(conversationId)
                        .build());
                return response.getSuccess();
            });
        } catch (Exception e) {
            log.error("Failed to mark inbox as read", e);
            return false;
        }
    }

    @DgsMutation
    public ChatMessage sendChatMessage(
            @InputArgument SendChatMessageInput input,
            @RequestHeader(name = "X-User-Id", required = false) String xUserId) {
        String effectiveSenderId = (xUserId != null && !xUserId.isBlank()) ? xUserId : input.getSenderId();
        SendMessageRequest.Builder requestBuilder = SendMessageRequest.newBuilder()
                .setSenderId(effectiveSenderId != null ? effectiveSenderId : "")
                .setConversationId(input.getConversationId())
                .addAllParticipantIds(input.getParticipantIds());

        if (input.getText() != null) requestBuilder.setText(input.getText());
        if (input.getReplyToId() != null) requestBuilder.setReplyToId(input.getReplyToId());
        if (input.getReplyToText() != null) requestBuilder.setReplyToText(input.getReplyToText());
        if (input.getReplyToSenderId() != null) requestBuilder.setReplyToSenderId(input.getReplyToSenderId());
        if (input.getImageUrl() != null) requestBuilder.setImageUrl(input.getImageUrl());
        if (input.getAudioUrl() != null) requestBuilder.setAudioUrl(input.getAudioUrl());
        if (input.getDuration() != null) requestBuilder.setDuration(input.getDuration());
        if (input.getFileUrl() != null) requestBuilder.setFileUrl(input.getFileUrl());
        if (input.getFileName() != null) requestBuilder.setFileName(input.getFileName());
        if (input.getFileSize() != null) requestBuilder.setFileSize(input.getFileSize().longValue());
        if (input.getLinkUrl() != null) requestBuilder.setLinkUrl(input.getLinkUrl());

        return executeGrpc(() -> chatMapper.grpcChatMessageToDgsChat(
                chatGrpcStub.sendMessage(requestBuilder.build()).getMessage()
        ), "Failed to send chat message via gRPC");
    }

    @DgsMutation
    public Boolean reactToChatMessage(
            @InputArgument String senderId,
            @InputArgument String conversationId,
            @InputArgument String messageId,
            @InputArgument String reactionEmoji,
            @InputArgument List<String> participantIds,
            @RequestHeader(name = "X-User-Id", required = false) String xUserId) {
        String effectiveSenderId = (xUserId != null && !xUserId.isBlank()) ? xUserId : senderId;
        try {
            return executeWithResilience(() -> {
                chatGrpcStub.reactToMessage(com.facebook.chat.grpc.ReactToMessageRequest.newBuilder()
                        .setSenderId(effectiveSenderId != null ? effectiveSenderId : "")
                        .setConversationId(conversationId != null ? conversationId : "")
                        .setMessageId(messageId != null ? messageId : "")
                        .setReactionEmoji(reactionEmoji != null ? reactionEmoji : "")
                        .addAllParticipantIds(participantIds != null ? participantIds : List.of())
                        .build());
                return true;
            });
        } catch (Exception e) {
            log.error("Failed to react to message via gRPC", e);
            return false;
        }
    }

    @DgsMutation
    public Boolean pinChatMessage(
            @InputArgument String conversationId,
            @InputArgument String messageId,
            @InputArgument Boolean isPinned,
            @InputArgument List<String> participantIds) {
        try {
            return executeWithResilience(() -> {
                chatGrpcStub.pinMessage(com.facebook.chat.grpc.PinMessageRequest.newBuilder()
                        .setConversationId(conversationId != null ? conversationId : "")
                        .setMessageId(messageId != null ? messageId : "")
                        .setIsPinned(isPinned != null && isPinned)
                        .addAllParticipantIds(participantIds != null ? participantIds : List.of())
                        .build());
                return true;
            });
        } catch (Exception e) {
            log.error("Failed to pin message via gRPC", e);
            return false;
        }
    }

    @DgsMutation
    public Boolean updateChatCustomization(
            @InputArgument String senderId,
            @InputArgument String conversationId,
            @InputArgument Integer themeId,
            @InputArgument String emoji,
            @InputArgument List<String> participantIds,
            @RequestHeader(name = "X-User-Id", required = false) String xUserId) {
        String effectiveSenderId = (xUserId != null && !xUserId.isBlank()) ? xUserId : senderId;
        try {
            return executeWithResilience(() -> {
                com.facebook.chat.grpc.SaveChatCustomizationRequest.Builder req = com.facebook.chat.grpc.SaveChatCustomizationRequest.newBuilder()
                        .setSenderId(effectiveSenderId != null ? effectiveSenderId : "")
                        .setConversationId(conversationId != null ? conversationId : "")
                        .addAllParticipantIds(participantIds != null ? participantIds : List.of());
                if (themeId != null) {
                    req.setThemeId(themeId).setHasThemeId(true);
                }
                if (emoji != null) {
                    req.setEmoji(emoji).setHasEmoji(true);
                }
                chatGrpcStub.saveChatCustomization(req.build());
                return true;
            });
        } catch (Exception e) {
            log.error("Failed to update chat customization via gRPC", e);
            return false;
        }
    }

    @DgsMutation
    public Boolean updateChatNickname(
            @InputArgument String senderId,
            @InputArgument String conversationId,
            @InputArgument String userId,
            @InputArgument String nickname,
            @InputArgument List<String> participantIds,
            @RequestHeader(name = "X-User-Id", required = false) String xUserId) {
        String effectiveSenderId = (xUserId != null && !xUserId.isBlank()) ? xUserId : senderId;
        try {
            return executeWithResilience(() -> {
                chatGrpcStub.saveChatNickname(com.facebook.chat.grpc.SaveChatNicknameRequest.newBuilder()
                        .setSenderId(effectiveSenderId != null ? effectiveSenderId : "")
                        .setConversationId(conversationId != null ? conversationId : "")
                        .setUserId(userId != null ? userId : "")
                        .setNickname(nickname != null ? nickname : "")
                        .addAllParticipantIds(participantIds != null ? participantIds : List.of())
                        .build());
                return true;
            });
        } catch (Exception e) {
            log.error("Failed to update chat nickname via gRPC", e);
            return false;
        }
    }

    @DgsMutation
    public boolean leaveChat(
            @InputArgument String userId,
            @InputArgument String conversationId,
            @RequestHeader(name = "X-User-Id", required = false) String xUserId) {
        String effectiveUserId = (xUserId != null && !xUserId.isBlank()) ? xUserId : userId;
        log.info("Edge: leaving chat for user: {}, conversation: {}", effectiveUserId, conversationId);
        try {
            return executeWithResilience(() -> {
                var response = chatGrpcStub.leaveChat(com.facebook.chat.grpc.LeaveChatRequest.newBuilder()
                        .setUserId(effectiveUserId != null ? effectiveUserId : "")
                        .setConversationId(conversationId != null ? conversationId : "")
                        .build());
                return response.getSuccess();
            });
        } catch (Exception e) {
            log.error("Failed to leave chat via gRPC", e);
            return false;
        }
    }

    @DgsQuery
    public com.facebook.ChatEdgeService.codegen.types.ChatSettings getChatSettings(@InputArgument String conversationId) {
        log.info("Edge: Fetching chat settings for conversation: {}", conversationId);
        try {
            return executeWithResilience(() -> {
                var response = chatGrpcStub.getChatSettings(com.facebook.chat.grpc.GetChatSettingsRequest.newBuilder()
                        .setConversationId(conversationId != null ? conversationId : "")
                        .build());
                
                var settings = new com.facebook.ChatEdgeService.codegen.types.ChatSettings();
                if (response.getHasThemeId()) {
                    settings.setThemeId(response.getThemeId());
                } else {
                    settings.setThemeId(null);
                }
                settings.setEmoji(response.getEmoji().isEmpty() ? null : response.getEmoji());
                
                var nicks = response.getNicknamesList().stream()
                        .map(dto -> {
                            var nick = new com.facebook.ChatEdgeService.codegen.types.ChatNickname();
                            nick.setUserId(dto.getUserId());
                            nick.setNickname(dto.getNickname());
                            return nick;
                        })
                        .toList();
                settings.setNicknames(nicks);

                java.util.List<com.facebook.ChatEdgeService.codegen.types.ChatUser> participants = new java.util.ArrayList<>();
                for (String partId : response.getParticipantIdsList()) {
                    try {
                        var user = userGrpcStub.getUserById(GetUserByIdRequest.newBuilder().setUserId(partId).build()).getUser();
                        com.facebook.ChatEdgeService.codegen.types.ChatUser chatUser = new com.facebook.ChatEdgeService.codegen.types.ChatUser();
                        chatUser.setId(user.getId());
                        chatUser.setFirstName(user.getFirstName());
                        chatUser.setLastName(user.getLastName());
                        chatUser.setAvatarId(user.getAvatarId());
                        participants.add(chatUser);
                    } catch (Exception e) {
                        log.warn("Failed to fetch participant user {} details", partId, e);
                        com.facebook.ChatEdgeService.codegen.types.ChatUser chatUser = new com.facebook.ChatEdgeService.codegen.types.ChatUser();
                        chatUser.setId(partId);
                        participants.add(chatUser);
                    }
                }
                settings.setParticipants(participants);

                boolean isGroup = "group".equalsIgnoreCase(response.getType());
                settings.setIsGroup(isGroup);

                return settings;
            });
        } catch (Exception e) {
            log.error("Failed to fetch chat settings via gRPC for {}", conversationId, e);
            var settings = new com.facebook.ChatEdgeService.codegen.types.ChatSettings();
            settings.setNicknames(List.of());
            settings.setParticipants(List.of());
            settings.setIsGroup(false);
            return settings;
        }
    }

    private List<ChatMessage> fetchMessages(String conversationId) {
        try {
            return executeWithResilience(() -> chatGrpcStub.getMessages(GetMessagesRequest.newBuilder()
                            .setConversationId(conversationId)
                            .build())
                    .getMessagesList()
                    .stream()
                    .map(chatMapper::grpcChatMessageToDgsChat)
                    .toList());
        } catch (Exception e) {
            log.error("Failed to fetch messages for conversation {}", conversationId, e);
            return List.of();
        }
    }

    private <T> T executeWithResilience(java.util.function.Supplier<T> action) {
        CircuitBreaker cb = circuitBreakerRegistry.circuitBreaker("chatService");
        Retry retry = retryRegistry.retry("chatService");
        Bulkhead bulkhead = bulkheadRegistry.bulkhead("chatService");
        return CircuitBreaker.decorateSupplier(cb, Retry.decorateSupplier(retry, Bulkhead.decorateSupplier(bulkhead, action))).get();
    }

    private <T> T executeGrpc(java.util.function.Supplier<T> action, String errorMsg) {
        try {
            return executeWithResilience(action);
        } catch (Exception e) {
            log.error(errorMsg, e);
            throw new RuntimeException("Chat service unavailable: " + e.getMessage());
        }
    }
}
