package com.facebook.ChatService.dto;

import java.util.List;

public record InboundMessagePayload(
        String messageId,
        String conversationId,
        String senderId,
        String text,
        List<String> participantIds,
        String type,
        String reactionEmoji,
        String targetMessageId,
        String lastReadMessageId,
        String replyToId,
        String replyToText,
        String replyToSenderId,
        String imageUrl,
        String audioUrl,
        Integer duration,
        String fileUrl,
        String fileName,
        Long fileSize,
        String linkUrl
) {
    public InboundMessagePayload(String messageId, String conversationId, String senderId, String text, List<String> participantIds) {
        this(messageId, conversationId, senderId, text, participantIds, "text", null, null, null, null, null, null, null, null, null, null, null, null, null);
    }
}
