package com.facebook.ChatService.service;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.BoundStatement;
import com.datastax.oss.driver.api.core.cql.PreparedStatement;
import com.datastax.oss.driver.api.core.uuid.Uuids;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.facebook.ChatService.dto.InboundMessagePayload;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatMessageHandler {

    private final CqlSession session;
    private final PreparedStatement insertMessageStmt;
    private final PreparedStatement insertInboxStmt;
    private final PreparedStatement selectMessagesStmt;
    private final PreparedStatement updateMessageReactionsStmt;
    private final PreparedStatement insertReadReceiptStmt;
    private final ObjectMapper objectMapper = new ObjectMapper()
            .configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    /**
     * Receives raw payload bytes from MQTT connection callback.
     */
    public void processInboundMessage(byte[] payloadBytes) {
        try {
            InboundMessagePayload payload = objectMapper.readValue(payloadBytes, InboundMessagePayload.class);
            log.info("Received message payload: messageId={}, conversationId={}", payload.messageId(), payload.conversationId());
            handleMessageAsync(payload);
        } catch (Exception e) {
            log.error("Failed to parse inbound message payload", e);
        }
    }

    /**
     * Main business logic: processes the message and updates ScyllaDB asynchronously.
     */
    public void handleMessageAsync(InboundMessagePayload payload) {
        log.info("MQTT Inbound: Ignoring incoming message processing in worker because REST controller manages database writes now.");
    }

    private UUID toUuid(String idStr) {
        if (idStr == null) return null;
        try {
            return UUID.fromString(idStr);
        } catch (IllegalArgumentException e) {
            return UUID.nameUUIDFromBytes(idStr.getBytes());
        }
    }

    private UUID toTimeUuid(String idStr) {
        if (idStr == null) return Uuids.timeBased();
        try {
            UUID uuid = UUID.fromString(idStr);
            if (uuid.version() == 1) {
                return uuid;
            }
        } catch (IllegalArgumentException e) {
            // Fallback to time-based UUID
        }
        return Uuids.timeBased();
    }
}
