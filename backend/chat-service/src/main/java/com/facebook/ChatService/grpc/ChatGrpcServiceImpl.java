package com.facebook.ChatService.grpc;

import com.facebook.ChatService.service.ChatQueryService;
import com.facebook.ChatService.service.ChatWriteService;
import com.facebook.chat.grpc.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.PreparedStatement;
import org.eclipse.paho.client.mqttv3.IMqttClient;

import java.util.List;
import java.util.Map;

@Slf4j
@GrpcService
@RequiredArgsConstructor
public class ChatGrpcServiceImpl extends ChatGrpcServiceGrpc.ChatGrpcServiceImplBase {

    private final ChatQueryService chatQueryService;
    private final ChatWriteService chatWriteService;
    private final ObjectMapper objectMapper;
    private final CqlSession session;
    private final PreparedStatement selectInboxStmt;
    private final PreparedStatement updateInboxReadStmt;
    private final IMqttClient mqttClient;
    private final PreparedStatement updateMessagePinnedStmt;
    private final PreparedStatement insertCustomizationStmt;
    private final PreparedStatement selectCustomizationStmt;
    private final PreparedStatement insertNicknameStmt;
    private final PreparedStatement selectNicknamesStmt;
    private final PreparedStatement selectParticipantsStmt;
    private final PreparedStatement updateMessageReactionsStmt;
    private final PreparedStatement insertInboxStmt;
    private final PreparedStatement insertReactionStmt;
    private final PreparedStatement deleteReactionStmt;
    private final PreparedStatement selectReactionsForConversationStmt;
    private final PreparedStatement selectReactionStmt;
    private final PreparedStatement selectConversationStmt;
    private final PreparedStatement deleteParticipantStmt;
    private final PreparedStatement deleteInboxStmt;

    @Override
    public void getMessages(GetMessagesRequest request, StreamObserver<GetMessagesResponse> responseObserver) {
        try {
            List<Map<String, Object>> messages = chatQueryService.getMessages(request.getConversationId());
            GetMessagesResponse.Builder responseBuilder = GetMessagesResponse.newBuilder();

            for (Map<String, Object> msg : messages) {
                responseBuilder.addMessages(toProtoMessage(msg));
            }

            responseObserver.onNext(responseBuilder.build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(io.grpc.Status.INTERNAL
                    .withDescription("Failed to fetch chat messages: " + e.getMessage())
                    .asRuntimeException());
        }
    }

    @Override
    public void sendMessage(SendMessageRequest request, StreamObserver<SendMessageResponse> responseObserver) {
        try {
            Map<String, Object> sent = chatWriteService.sendMessage(
                    request.getSenderId(),
                    request.getConversationId(),
                    request.getText(),
                    request.getReplyToId(),
                    request.getReplyToText(),
                    request.getReplyToSenderId(),
                    request.getImageUrl(),
                    request.getAudioUrl(),
                    request.getDuration() > 0 ? request.getDuration() : null,
                    request.getFileUrl(),
                    request.getFileName(),
                    request.getFileSize() > 0 ? request.getFileSize() : null,
                    request.getLinkUrl(),
                    request.getParticipantIdsList()
            );

            responseObserver.onNext(SendMessageResponse.newBuilder()
                    .setMessage(toProtoMessage(sent))
                    .build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(io.grpc.Status.INTERNAL
                    .withDescription("Failed to send chat message: " + e.getMessage())
                    .asRuntimeException());
        }
    }

    private ChatMessageDto toProtoMessage(Map<String, Object> msg) {
        ChatMessageDto.Builder builder = ChatMessageDto.newBuilder()
                .setMessageId(stringVal(msg.get("messageId")))
                .setSenderId(stringVal(msg.get("senderId")))
                .setText(stringVal(msg.get("text")))
                .setReplyToId(stringVal(msg.get("replyToId")))
                .setReplyToText(stringVal(msg.get("replyToText")))
                .setReplyToSenderId(stringVal(msg.get("replyToSenderId")))
                .setImageUrl(stringVal(msg.get("imageUrl")))
                .setAudioUrl(stringVal(msg.get("audioUrl")))
                .setFileUrl(stringVal(msg.get("fileUrl")))
                .setFileName(stringVal(msg.get("fileName")))
                .setLinkUrl(stringVal(msg.get("linkUrl")))
                .setTime(doubleVal(msg.get("time")))
                .setDuration(intVal(msg.get("duration")))
                .setIsPinned(boolVal(msg.get("isPinned")))
                .setSystemActionType(stringVal(msg.get("systemActionType")))
                .setSystemActionPayload(stringVal(msg.get("systemActionPayload")));

        Object fileSize = msg.get("fileSize");
        if (fileSize instanceof Number number) {
            builder.setFileSize(number.doubleValue());
        }

        Object reactions = msg.get("reactions");
        if (reactions instanceof Map<?, ?> reactionsMap) {
            for (Map.Entry<?, ?> entry : reactionsMap.entrySet()) {
                try {
                    builder.putReactions(String.valueOf(entry.getKey()),
                            objectMapper.writeValueAsString(entry.getValue()));
                } catch (Exception e) {
                    builder.putReactions(String.valueOf(entry.getKey()), "[]");
                }
            }
        }

        return builder.build();
    }

    private String stringVal(Object value) {
        return value != null ? String.valueOf(value) : "";
    }

    private double doubleVal(Object value) {
        return value instanceof Number number ? number.doubleValue() : 0.0;
    }

    private int intVal(Object value) {
        return value instanceof Number number ? number.intValue() : 0;
    }

    private boolean boolVal(Object value) {
        return value instanceof Boolean bool && bool;
    }

    @Override
    public void getInbox(GetInboxRequest request, StreamObserver<GetInboxResponse> responseObserver) {
        try {
            java.util.UUID userId = toUuid(request.getUserId());
            var resultSet = session.execute(selectInboxStmt.bind(userId));
            GetInboxResponse.Builder responseBuilder = GetInboxResponse.newBuilder();

            java.util.List<InboxItemDto> items = new java.util.ArrayList<>();
            for (com.datastax.oss.driver.api.core.cql.Row row : resultSet) {
                java.util.UUID conversationId = row.getUuid("conversation_id");
                java.util.UUID lastActivityUuid = row.getUuid("last_activity");
                long lastActivityTs = lastActivityUuid != null ? com.datastax.oss.driver.api.core.uuid.Uuids.unixTimestamp(lastActivityUuid) : 0L;
                String lastMessageText = row.getString("last_message_text");
                boolean isUnread = row.getBoolean("is_unread");
                java.util.UUID recipientId = row.isNull("recipient_id") ? null : row.getUuid("recipient_id");
                java.util.UUID lastMessageSenderId = row.isNull("last_message_sender_id") ? null : row.getUuid("last_message_sender_id");

                items.add(InboxItemDto.newBuilder()
                        .setConversationId(conversationId.toString())
                        .setLastActivity(lastActivityTs)
                        .setLastMessageText(lastMessageText != null ? lastMessageText : "")
                        .setIsUnread(isUnread)
                        .setRecipientId(recipientId != null ? recipientId.toString() : "")
                        .setLastMessageSenderId(lastMessageSenderId != null ? lastMessageSenderId.toString() : "")
                        .build());
            }

            items.sort((a, b) -> Long.compare(b.getLastActivity(), a.getLastActivity()));
            responseBuilder.addAllItems(items);

            responseObserver.onNext(responseBuilder.build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(io.grpc.Status.INTERNAL
                    .withDescription("Failed to fetch inbox: " + e.getMessage())
                    .asRuntimeException());
        }
    }

    @Override
    public void markAsRead(MarkAsReadRequest request, StreamObserver<MarkAsReadResponse> responseObserver) {
        try {
            java.util.UUID userId = toUuid(request.getUserId());
            java.util.UUID conversationId = toUuid(request.getConversationId());
            session.execute(updateInboxReadStmt.bind(userId, conversationId));

            responseObserver.onNext(MarkAsReadResponse.newBuilder().setSuccess(true).build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(io.grpc.Status.INTERNAL
                    .withDescription("Failed to mark inbox as read: " + e.getMessage())
                    .asRuntimeException());
        }
    }

    @Override
    public void reactToMessage(ReactToMessageRequest request, StreamObserver<ReactToMessageResponse> responseObserver) {
        try {
            java.util.UUID conversationUuid = toUuid(request.getConversationId());
            java.util.UUID targetMessageUuid = java.util.UUID.fromString(request.getMessageId());
            java.util.UUID senderUuid = toUuid(request.getSenderId());
            java.util.UUID messageUuid = com.datastax.oss.driver.api.core.uuid.Uuids.timeBased();

            var checkRow = session.execute(selectReactionStmt.bind(conversationUuid, targetMessageUuid, senderUuid, request.getReactionEmoji())).one();
            if (checkRow != null) {
                session.execute(deleteReactionStmt.bind(conversationUuid, targetMessageUuid, senderUuid, request.getReactionEmoji()));
            } else {
                session.execute(insertReactionStmt.bind(conversationUuid, targetMessageUuid, senderUuid, request.getReactionEmoji()));
            }

            java.util.UUID recipientUuid = null;
            java.util.List<String> participantIds = request.getParticipantIdsList();
            if (participantIds != null) {
                for (String pid : participantIds) {
                    if (!pid.equals(request.getSenderId())) {
                        recipientUuid = toUuid(pid);
                        break;
                    }
                }
            }

            if (recipientUuid != null) {
                String recipientText = "Zareagował " + request.getReactionEmoji() + " na Twoją wiadomość";
                session.execute(insertInboxStmt.bind(
                        recipientUuid,
                        messageUuid,
                        conversationUuid,
                        recipientText,
                        true,
                        senderUuid
                ));
            }

            java.util.Map<String, Object> payload = new java.util.HashMap<>();
            payload.put("type", "reaction");
            payload.put("conversationId", request.getConversationId());
            payload.put("targetMessageId", request.getMessageId());
            payload.put("senderId", request.getSenderId());
            payload.put("reactionEmoji", request.getReactionEmoji());
            payload.put("participantIds", participantIds);
            payload.put("time", com.datastax.oss.driver.api.core.uuid.Uuids.unixTimestamp(messageUuid));

            publishMqttEvent(payload, participantIds);

            responseObserver.onNext(ReactToMessageResponse.newBuilder().setSuccess(true).build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(io.grpc.Status.INTERNAL
                    .withDescription("Failed to react to message: " + e.getMessage())
                    .asRuntimeException());
        }
    }

    @Override
    public void pinMessage(PinMessageRequest request, StreamObserver<PinMessageResponse> responseObserver) {
        try {
            java.util.UUID conversationId = toUuid(request.getConversationId());
            java.util.UUID messageId = java.util.UUID.fromString(request.getMessageId());

            session.execute(updateMessagePinnedStmt.bind(request.getIsPinned(), conversationId, messageId));

            java.util.Map<String, Object> payload = new java.util.HashMap<>();
            payload.put("type", "message_pinned");
            payload.put("conversationId", request.getConversationId());
            payload.put("messageId", request.getMessageId());
            payload.put("isPinned", request.getIsPinned());
            payload.put("participantIds", request.getParticipantIdsList());
            publishMqttEvent(payload, request.getParticipantIdsList());

            responseObserver.onNext(PinMessageResponse.newBuilder().setSuccess(true).build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(io.grpc.Status.INTERNAL
                    .withDescription("Failed to pin message: " + e.getMessage())
                    .asRuntimeException());
        }
    }

    @Override
    public void saveChatCustomization(SaveChatCustomizationRequest request, StreamObserver<SaveChatCustomizationResponse> responseObserver) {
        try {
            java.util.UUID conversationId = toUuid(request.getConversationId());
            java.util.UUID senderUuid = toUuid(request.getSenderId());
            var existing = session.execute(selectCustomizationStmt.bind(conversationId)).one();
            
            Integer finalThemeId = request.getHasThemeId() ? request.getThemeId() : null;
            String finalEmoji = request.getHasEmoji() ? request.getEmoji() : null;

            if (existing != null) {
                if (finalThemeId == null && !existing.isNull("theme_id")) finalThemeId = existing.getInt("theme_id");
                if (finalEmoji == null && !existing.isNull("emoji")) finalEmoji = existing.getString("emoji");
            }

            session.execute(insertCustomizationStmt.bind(conversationId, finalThemeId, finalEmoji));

            String systemText = "";
            String actionType = null;
            String actionPayload = null;
            if (request.getHasThemeId()) {
                String[] themeIds = {
                    "winter", "dune", "cyberpunk", "matrix",
                    "space", "magic", "candy", "ocean",
                    "jungle", "gotham", "retro", "gold"
                };
                int themeId = request.getThemeId();
                String themeIdStr = (themeId >= 0 && themeId < themeIds.length) ? themeIds[themeId] : "classic";
                systemText = "SYSTEM_ACTION:CHANGE_THEME:" + themeIdStr;
                actionType = "CHANGE_THEME";
                actionPayload = themeIdStr;
            } else if (request.getHasEmoji()) {
                systemText = "SYSTEM_ACTION:CHANGE_E:" + request.getEmoji();
                actionType = "CHANGE_E";
                actionPayload = request.getEmoji();
            }

            if (!systemText.isEmpty()) {
                chatWriteService.sendMessage(
                        request.getSenderId(),
                        request.getConversationId(),
                        systemText,
                        null, null, null, null, null, null, null, null, null, null,
                        request.getParticipantIdsList(),
                        actionType,
                        actionPayload
                );
            }

            responseObserver.onNext(SaveChatCustomizationResponse.newBuilder().setSuccess(true).build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(io.grpc.Status.INTERNAL
                    .withDescription("Failed to save chat customization: " + e.getMessage())
                    .asRuntimeException());
        }
    }

    @Override
    public void saveChatNickname(SaveChatNicknameRequest request, StreamObserver<SaveChatNicknameResponse> responseObserver) {
        try {
            java.util.UUID conversationId = toUuid(request.getConversationId());
            java.util.UUID userId = toUuid(request.getUserId());

            String nickname = request.getNickname();
            session.execute(insertNicknameStmt.bind(conversationId, userId, nickname));

            String systemText = "SYSTEM_ACTION:CHANGE_NICKNAME:" + (nickname == null ? "" : nickname);
            String actionType = "CHANGE_NICKNAME";
            String actionPayload = nickname == null ? "" : nickname;

            chatWriteService.sendMessage(
                    request.getSenderId(),
                    request.getConversationId(),
                    systemText,
                    null, null, null, null, null, null, null, null, null, null,
                    request.getParticipantIdsList(),
                    actionType,
                    actionPayload
            );

            responseObserver.onNext(SaveChatNicknameResponse.newBuilder().setSuccess(true).build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(io.grpc.Status.INTERNAL
                    .withDescription("Failed to save chat nickname: " + e.getMessage())
                    .asRuntimeException());
        }
    }

    @Override
    public void getChatSettings(GetChatSettingsRequest request, StreamObserver<GetChatSettingsResponse> responseObserver) {
        try {
            java.util.UUID conversationId = toUuid(request.getConversationId());
            GetChatSettingsResponse.Builder response = GetChatSettingsResponse.newBuilder();
            var convRow = session.execute(selectConversationStmt.bind(conversationId)).one();
            if (convRow != null) {
                String type = convRow.getString("type");
                if (type != null) {
                    response.setType(type);
                }
            } else {
                response.setType("private");
            }

            var custRow = session.execute(selectCustomizationStmt.bind(conversationId)).one();
            if (custRow != null) {
                if (!custRow.isNull("theme_id")) {
                    response.setThemeId(custRow.getInt("theme_id")).setHasThemeId(true);
                }
                String emoji = custRow.getString("emoji");
                if (emoji != null) {
                    response.setEmoji(emoji);
                }
            }

            var nickResultSet = session.execute(selectNicknamesStmt.bind(conversationId));
            for (var row : nickResultSet) {
                java.util.UUID userId = row.getUuid("user_id");
                String nickname = row.getString("nickname");
                if (userId != null && nickname != null) {
                    response.addNicknames(ChatNicknameDto.newBuilder()
                            .setUserId(userId.toString())
                            .setNickname(nickname)
                            .build());
                }
            }

            var partResultSet = session.execute(selectParticipantsStmt.bind(conversationId));
            for (var row : partResultSet) {
                java.util.UUID userId = row.getUuid("user_id");
                if (userId != null) {
                    response.addParticipantIds(userId.toString());
                }
            }

            responseObserver.onNext(response.build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(io.grpc.Status.INTERNAL
                    .withDescription("Failed to get chat settings: " + e.getMessage())
                    .asRuntimeException());
        }
    }

    @Override
    public void leaveChat(LeaveChatRequest request, StreamObserver<LeaveChatResponse> responseObserver) {
        try {
            java.util.UUID userId = toUuid(request.getUserId());
            java.util.UUID conversationId = toUuid(request.getConversationId());

            // 1. Get all current participants before removing
            var partResultSet = session.execute(selectParticipantsStmt.bind(conversationId));
            java.util.List<String> resolvedParticipants = new java.util.ArrayList<>();
            for (var row : partResultSet) {
                java.util.UUID pId = row.getUuid("user_id");
                if (pId != null) {
                    resolvedParticipants.add(pId.toString());
                }
            }

            // 2. Send the leave action system message
            chatWriteService.sendMessage(
                request.getUserId(),
                request.getConversationId(),
                "SYSTEM_ACTION:LEAVE_MEMBER:" + request.getUserId(),
                null, null, null, null, null, null, null, null, null, null,
                resolvedParticipants,
                "leave_member",
                request.getUserId()
            );

            // 3. Remove the participant from ScyllaDB conversation_participants
            session.execute(deleteParticipantStmt.bind(conversationId, userId));

            // 4. Delete the inbox record for the leaving user
            session.execute(deleteInboxStmt.bind(userId, conversationId));

            responseObserver.onNext(LeaveChatResponse.newBuilder().setSuccess(true).build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("Failed to leave chat: {}", e.getMessage(), e);
            responseObserver.onError(io.grpc.Status.INTERNAL
                    .withDescription("Failed to leave chat: " + e.getMessage())
                    .asRuntimeException());
        }
    }

    private void publishMqttEvent(java.util.Map<String, Object> payload, java.util.List<String> participantIds) {
        try {
            byte[] bytes = objectMapper.writeValueAsBytes(payload);
            if (participantIds != null && !participantIds.isEmpty()) {
                for (String pid : participantIds) {
                    mqttClient.publish("chat/messages/user/" + pid, new org.eclipse.paho.client.mqttv3.MqttMessage(bytes));
                }
            } else {
                Object senderIdObj = payload.get("senderId");
                Object convIdObj = payload.get("conversationId");
                if (convIdObj != null) {
                    mqttClient.publish("chat/messages/user/" + convIdObj.toString(), new org.eclipse.paho.client.mqttv3.MqttMessage(bytes));
                    if (senderIdObj != null && !senderIdObj.toString().equals(convIdObj.toString())) {
                        mqttClient.publish("chat/messages/user/" + senderIdObj.toString(), new org.eclipse.paho.client.mqttv3.MqttMessage(bytes));
                    }
                }
            }
        } catch (Exception e) {
            log.error("Failed to publish event to MQTT: {}", e.getMessage());
        }
    }

    private java.util.UUID toUuid(String idStr) {
        if (idStr == null) return null;
        try {
            return java.util.UUID.fromString(idStr);
        } catch (IllegalArgumentException e) {
            return java.util.UUID.nameUUIDFromBytes(idStr.getBytes());
        }
    }
}
