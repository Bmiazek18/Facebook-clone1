package com.facebook.ChatService.service;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.PreparedStatement;
import com.datastax.oss.driver.api.core.cql.Row;
import com.datastax.oss.driver.api.core.uuid.Uuids;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ChatQueryService {

    private final CqlSession session;
    private final PreparedStatement selectMessagesStmt;
    private final PreparedStatement selectReactionsForConversationStmt;
    private final ObjectMapper objectMapper;
    private final MediaUrlClientService mediaUrlClientService;

    public ChatQueryService(CqlSession session,
                            PreparedStatement selectMessagesStmt,
                            PreparedStatement selectReactionsForConversationStmt,
                            ObjectMapper objectMapper,
                            MediaUrlClientService mediaUrlClientService) {
        this.session = session;
        this.selectMessagesStmt = selectMessagesStmt;
        this.selectReactionsForConversationStmt = selectReactionsForConversationStmt;
        this.objectMapper = objectMapper;
        this.mediaUrlClientService = mediaUrlClientService;
    }

    public List<Map<String, Object>> getMessages(String conversationIdStr) {
        UUID conversationId = toUuid(conversationIdStr);

        var reactionsResultSet = session.execute(selectReactionsForConversationStmt.bind(conversationId));
        Map<UUID, Map<String, List<String>>> messageReactionsMap = new HashMap<>();
        for (Row row : reactionsResultSet) {
            UUID msgId = row.getUuid("message_id");
            UUID uId = row.getUuid("user_id");
            String emoji = row.getString("reaction_emoji");
            if (msgId != null && uId != null && emoji != null) {
                messageReactionsMap
                        .computeIfAbsent(msgId, k -> new HashMap<>())
                        .computeIfAbsent(emoji, k -> new ArrayList<>())
                        .add("user_" + uId.toString());
            }
        }

        var resultSet = session.execute(selectMessagesStmt.bind(conversationId));
        List<Map<String, Object>> result = new ArrayList<>();

        for (Row row : resultSet) {
            Map<String, Object> map = new HashMap<>();
            UUID messageId = row.getUuid("message_id");
            UUID senderId = row.getUuid("sender_id");
            String messageText = row.getString("message_text");
            Map<String, List<String>> parsedReactions = messageReactionsMap.getOrDefault(messageId, new HashMap<>());

            UUID replyToId = row.isNull("reply_to_message_id") ? null : row.getUuid("reply_to_message_id");
            String replyToText = row.isNull("reply_to_text") ? null : row.getString("reply_to_text");
            UUID replyToSenderId = row.isNull("reply_to_sender_id") ? null : row.getUuid("reply_to_sender_id");
            String imageUrl = row.isNull("image_url") ? null : row.getString("image_url");
            String audioUrl = row.isNull("audio_url") ? null : row.getString("audio_url");
            Integer duration = row.isNull("duration") ? null : row.getInt("duration");
            Boolean isPinned = row.isNull("is_pinned") ? false : row.getBoolean("is_pinned");
            String fileUrl = row.isNull("file_url") ? null : row.getString("file_url");
            String fileName = row.isNull("file_name") ? null : row.getString("file_name");
            Long fileSize = row.isNull("file_size") ? null : row.getLong("file_size");
            String linkUrl = row.isNull("link_url") ? null : row.getString("link_url");
            String systemActionType = row.isNull("system_action_type") ? null : row.getString("system_action_type");
            String systemActionPayload = row.isNull("system_action_payload") ? null : row.getString("system_action_payload");

            map.put("messageId", messageId.toString());
            map.put("time", Uuids.unixTimestamp(messageId));
            map.put("senderId", senderId.toString());
            map.put("text", messageText);
            map.put("isPinned", isPinned);
            map.put("reactions", parsedReactions);
            map.put("imageUrl", mediaUrlClientService.resolveForClient(imageUrl));
            map.put("audioUrl", mediaUrlClientService.resolveForClient(audioUrl));
            map.put("duration", duration);
            map.put("fileUrl", mediaUrlClientService.resolveForClient(fileUrl));
            map.put("fileName", fileName);
            map.put("fileSize", fileSize);
            map.put("linkUrl", linkUrl);
            map.put("replyToId", replyToId != null ? replyToId.toString() : null);
            map.put("replyToText", replyToText);
            map.put("replyToSenderId", replyToSenderId != null ? replyToSenderId.toString() : null);
            map.put("systemActionType", systemActionType);
            map.put("systemActionPayload", systemActionPayload);
            result.add(map);
        }

        result.sort((a, b) -> Long.compare((Long) a.get("time"), (Long) b.get("time")));
        return result;
    }

    private UUID toUuid(String idStr) {
        if (idStr == null) {
            return null;
        }
        try {
            return UUID.fromString(idStr);
        } catch (IllegalArgumentException e) {
            return UUID.nameUUIDFromBytes(idStr.getBytes());
        }
    }
}
