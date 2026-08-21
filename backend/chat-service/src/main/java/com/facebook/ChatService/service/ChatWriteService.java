package com.facebook.ChatService.service;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.PreparedStatement;
import com.datastax.oss.driver.api.core.uuid.Uuids;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.eclipse.paho.client.mqttv3.IMqttClient;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class ChatWriteService {

    private final CqlSession session;
    private final PreparedStatement insertMessageStmt;
    private final PreparedStatement insertInboxStmt;
    private final PreparedStatement insertReadReceiptStmt;
    private final PreparedStatement insertParticipantStmt;
    private final PreparedStatement selectParticipantsStmt;
    private final PreparedStatement insertConversationStmt;
    private final PreparedStatement selectConversationStmt;
    private final IMqttClient mqttClient;
    private final ObjectMapper objectMapper;
    private final MediaUrlClientService mediaUrlClientService;

    public Map<String, Object> sendMessage(
            String senderIdStr,
            String conversationIdStr,
            String text,
            String replyToIdStr,
            String replyToText,
            String replyToSenderIdStr,
            String imageUrl,
            String audioUrl,
            Integer duration,
            String fileUrl,
            String fileName,
            Long fileSize,
            String linkUrl,
            List<String> participantIds) {
        return sendMessage(senderIdStr, conversationIdStr, text, replyToIdStr, replyToText, replyToSenderIdStr,
                imageUrl, audioUrl, duration, fileUrl, fileName, fileSize, linkUrl, participantIds, null, null);
    }

    public Map<String, Object> sendMessage(
            String senderIdStr,
            String conversationIdStr,
            String text,
            String replyToIdStr,
            String replyToText,
            String replyToSenderIdStr,
            String imageUrl,
            String audioUrl,
            Integer duration,
            String fileUrl,
            String fileName,
            Long fileSize,
            String linkUrl,
            List<String> participantIds,
            String systemActionType,
            String systemActionPayload) {

        UUID conversationId = toUuid(conversationIdStr);
        UUID messageId = Uuids.timeBased();
        UUID senderUuid = toUuid(senderIdStr);

        String storedImageUrl = com.facebook.ChatService.util.MediaReferenceNormalizer.normalize(imageUrl);
        String storedAudioUrl = com.facebook.ChatService.util.MediaReferenceNormalizer.normalize(audioUrl);
        String storedFileUrl = com.facebook.ChatService.util.MediaReferenceNormalizer.normalize(fileUrl);

        session.execute(insertMessageStmt.bind(
                conversationId,
                messageId,
                senderUuid,
                text,
                replyToIdStr == null || replyToIdStr.trim().isEmpty() ? null : UUID.fromString(replyToIdStr),
                replyToText,
                replyToSenderIdStr == null || replyToSenderIdStr.trim().isEmpty() ? null : toUuid(replyToSenderIdStr),
                storedImageUrl,
                storedAudioUrl,
                duration,
                storedFileUrl,
                fileName,
                fileSize,
                linkUrl,
                systemActionType,
                systemActionPayload
        ));

        // Save/merge participantIds into conversation_participants
        if (participantIds != null && participantIds.size() > 1) {
            for (String pid : participantIds) {
                UUID pUuid = toUuid(pid);
                if (pUuid != null) {
                    session.execute(insertParticipantStmt.bind(conversationId, pUuid));
                }
            }
        }

        // Fetch all participants of the conversation
        List<String> resolvedParticipants = new ArrayList<>();
        var participantsResult = session.execute(selectParticipantsStmt.bind(conversationId));
        for (var row : participantsResult) {
            resolvedParticipants.add(row.getUuid("user_id").toString());
        }

        // If no stored participants found, use the ones from request
        if (resolvedParticipants.isEmpty()) {
            if (participantIds != null) {
                resolvedParticipants.addAll(participantIds);
            } else {
                resolvedParticipants.add(senderIdStr);
            }
        }

        String lastMessageText = (text != null && !text.isBlank())
                ? text
                : ((imageUrl != null && !imageUrl.isBlank())
                ? "Zdjęcie"
                : ((audioUrl != null && !audioUrl.isBlank())
                ? "Nagranie głosowe"
                : ((fileUrl != null && !fileUrl.isBlank())
                ? "Plik: " + fileName
                : ((linkUrl != null && !linkUrl.isBlank()) ? "Link: " + linkUrl : ""))));

        boolean isGroup = false;
        if (resolvedParticipants == null || resolvedParticipants.size() > 2) {
            isGroup = true;
        } else if (resolvedParticipants.size() == 2) {
            String u1 = resolvedParticipants.get(0);
            String u2 = resolvedParticipants.get(1);
            String symmetricId = getSymmetricUuid(u1, u2);
            if (!symmetricId.equals(conversationIdStr)) {
                isGroup = true;
            }
        } else {
            isGroup = true;
        }

        var existingConv = session.execute(selectConversationStmt.bind(conversationId)).one();
        if (existingConv == null) {
            String convType = isGroup ? "group" : "private";
            session.execute(insertConversationStmt.bind(conversationId, convType, null, java.time.Instant.now()));
        }

        UUID recipientUuid = null;
        if (!isGroup && resolvedParticipants != null) {
            for (String pid : resolvedParticipants) {
                if (!pid.equals(senderIdStr)) {
                    recipientUuid = toUuid(pid);
                    break;
                }
            }
        }

        session.execute(insertInboxStmt.bind(
                senderUuid,
                messageId,
                conversationId,
                lastMessageText,
                false,
                recipientUuid,
                senderUuid
        ));

        session.execute(insertReadReceiptStmt.bind(
                conversationId,
                senderUuid,
                messageId
        ));

        if (resolvedParticipants != null) {
            for (String pid : resolvedParticipants) {
                UUID partUuid = toUuid(pid);
                if (partUuid != null && !partUuid.equals(senderUuid)) {
                    session.execute(insertInboxStmt.bind(
                            partUuid,
                            messageId,
                            conversationId,
                            lastMessageText,
                            true,
                            isGroup ? null : senderUuid,
                            senderUuid
                    ));
                }
            }
        }

        Map<String, Object> payload = new HashMap<>();
        payload.put("type", "message");
        payload.put("messageId", messageId.toString());
        payload.put("conversationId", conversationIdStr);
        payload.put("senderId", senderIdStr);
        payload.put("text", text);
        payload.put("replyToId", replyToIdStr);
        payload.put("replyToText", replyToText);
        payload.put("replyToSenderId", replyToSenderIdStr);
        payload.put("imageUrl", mediaUrlClientService.resolveForClient(storedImageUrl));
        payload.put("audioUrl", mediaUrlClientService.resolveForClient(storedAudioUrl));
        payload.put("duration", duration);
        payload.put("fileUrl", mediaUrlClientService.resolveForClient(storedFileUrl));
        payload.put("fileName", fileName);
        payload.put("fileSize", fileSize);
        payload.put("linkUrl", linkUrl);
        payload.put("participantIds", resolvedParticipants);
        payload.put("systemActionType", systemActionType);
        payload.put("systemActionPayload", systemActionPayload);
        payload.put("time", Uuids.unixTimestamp(messageId));

        publishMqttEvent(payload, resolvedParticipants);
        return payload;
    }

    private String getSymmetricUuid(String id1, String id2) {
        java.util.List<String> list = new java.util.ArrayList<>();
        list.add(id1);
        list.add(id2);
        java.util.Collections.sort(list);
        String hex1 = list.get(0).replace("-", "");
        String hex2 = list.get(1).replace("-", "");
        byte[] bytes = new byte[16];
        for (int i = 0; i < 16; i++) {
            int b1 = Integer.parseInt(hex1.substring(i * 2, i * 2 + 2), 16);
            int b2 = Integer.parseInt(hex2.substring(i * 2, i * 2 + 2), 16);
            bytes[i] = (byte) (b1 ^ b2);
        }
        bytes[6] = (byte) ((bytes[6] & 0x0f) | 0x40);
        bytes[8] = (byte) ((bytes[8] & 0x3f) | 0x80);
        
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        String hex = sb.toString();
        return hex.substring(0, 8) + "-" + hex.substring(8, 12) + "-" + hex.substring(12, 16) + "-" + hex.substring(16, 20) + "-" + hex.substring(20);
    }

    private UUID toUuid(String idStr) {
        if (idStr == null) return null;
        try {
            return UUID.fromString(idStr);
        } catch (IllegalArgumentException e) {
            return UUID.nameUUIDFromBytes(idStr.getBytes());
        }
    }

    private void publishMqttEvent(Map<String, Object> payload, List<String> participantIds) {
        try {
            byte[] bytes = objectMapper.writeValueAsBytes(payload);
            if (participantIds != null && !participantIds.isEmpty()) {
                for (String pid : participantIds) {
                    mqttClient.publish("chat/messages/user/" + pid, new MqttMessage(bytes));
                }
            } else {
                Object convIdObj = payload.get("conversationId");
                if (convIdObj != null) {
                    mqttClient.publish("chat/messages/user/" + convIdObj.toString(), new MqttMessage(bytes));
                }
            }
        } catch (Exception e) {
            System.err.println("Failed to publish event to MQTT: " + e.getMessage());
        }
    }
}
