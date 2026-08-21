package com.facebook.ChatService.service;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.AsyncResultSet;
import com.datastax.oss.driver.api.core.cql.BoundStatement;
import com.datastax.oss.driver.api.core.cql.PreparedStatement;
import com.facebook.ChatService.dto.InboundMessagePayload;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ChatMessageHandlerTest {

    @Mock
    private CqlSession session;

    @Mock
    private PreparedStatement insertMessageStmt;

    @Mock
    private PreparedStatement insertInboxStmt;

    @Mock
    private PreparedStatement selectMessagesStmt;

    @Mock
    private PreparedStatement updateMessageReactionsStmt;

    @Mock
    private PreparedStatement insertReadReceiptStmt;

    @Mock
    private BoundStatement mockBoundStatement;

    @Mock
    private CompletionStage<AsyncResultSet> mockCompletionStage;

    @Mock
    private AsyncResultSet mockAsyncResultSet;

    private ChatMessageHandler handler;

    @BeforeEach
    public void setUp() {
        handler = new ChatMessageHandler(session, insertMessageStmt, insertInboxStmt, selectMessagesStmt, updateMessageReactionsStmt, insertReadReceiptStmt);
    }

    @Test
    public void testHandleMessageAsync() {
        UUID messageId = UUID.randomUUID();
        UUID conversationId = UUID.randomUUID();
        UUID senderId = UUID.randomUUID();
        UUID participantId = UUID.randomUUID();

        InboundMessagePayload payload = new InboundMessagePayload(
                messageId.toString(),
                conversationId.toString(),
                senderId.toString(),
                "Hello, world!",
                List.of(senderId.toString(), participantId.toString())
        );

        // Execute handler
        handler.handleMessageAsync(payload);

        // Verify that no database operations are performed since REST controller handles it
        verifyNoInteractions(insertMessageStmt, insertInboxStmt, insertReadReceiptStmt, session);
    }
}
