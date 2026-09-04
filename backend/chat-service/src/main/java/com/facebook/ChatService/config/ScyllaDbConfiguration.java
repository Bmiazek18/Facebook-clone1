package com.facebook.ChatService.config;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.PreparedStatement;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.InetSocketAddress;

@Configuration
public class ScyllaDbConfiguration {

    @Value("${scylladb.contact-points:localhost:9042}")
    private String contactPoints;

    @Value("${scylladb.local-datacenter:datacenter1}")
    private String localDatacenter;

    @Bean(destroyMethod = "close")
    public CqlSession cqlSession() {
        String[] parts = contactPoints.split(",");
        var builder = CqlSession.builder()
                .withLocalDatacenter(localDatacenter);

        for (String cp : parts) {
            String[] hostPort = cp.trim().split(":");
            String host = hostPort[0];
            int port = hostPort.length > 1 ? Integer.parseInt(hostPort[1]) : 9042;
            builder.addContactPoint(new InetSocketAddress(host, port));
        }

        CqlSession session = builder.build();

        session.execute("CREATE KEYSPACE IF NOT EXISTS chat WITH replication = {'class': 'SimpleStrategy', 'replication_factor': 1}");
        session.execute("CREATE TABLE IF NOT EXISTS chat.messages (conversation_id uuid, message_id timeuuid, sender_id uuid, message_text text, reactions map<text, text>, reply_to_message_id timeuuid, reply_to_text text, reply_to_sender_id uuid, image_url text, audio_url text, duration int, is_pinned boolean, PRIMARY KEY ((conversation_id), message_id))");
        
        try {
            session.execute("ALTER TABLE chat.messages ADD file_url text");
        } catch (Exception e) {
            // Ignore if column already exists
        }
        try {
            session.execute("ALTER TABLE chat.messages ADD file_name text");
        } catch (Exception e) {
            // Ignore if column already exists
        }
        try {
            session.execute("ALTER TABLE chat.messages ADD file_size bigint");
        } catch (Exception e) {
            // Ignore if column already exists
        }
        try {
            session.execute("ALTER TABLE chat.messages ADD link_url text");
        } catch (Exception e) {
            // Ignore if column already exists
        }
        try {
            session.execute("ALTER TABLE chat.messages ADD system_action_type text");
        } catch (Exception e) {
            // Ignore if column already exists
        }
        try {
            session.execute("ALTER TABLE chat.messages ADD system_action_payload text");
        } catch (Exception e) {
            // Ignore if column already exists
        }

        try {
            session.execute("ALTER TABLE chat.user_inbox ADD last_message_sender_id uuid");
        } catch (Exception e) {
            // Ignore if column already exists
        }

        session.execute("CREATE TABLE IF NOT EXISTS chat.user_inbox (user_id uuid, conversation_id uuid, last_activity timeuuid, last_message_text text, is_unread boolean, recipient_id uuid, last_message_sender_id uuid, PRIMARY KEY ((user_id), conversation_id))");
        session.execute("CREATE TABLE IF NOT EXISTS chat.read_receipts (conversation_id uuid, user_id uuid, last_read_message_id timeuuid, PRIMARY KEY ((conversation_id), user_id))");
        session.execute("CREATE TABLE IF NOT EXISTS chat.conversation_customization (conversation_id uuid, theme_id int, emoji text, PRIMARY KEY (conversation_id))");
        session.execute("CREATE TABLE IF NOT EXISTS chat.conversation_nicknames (conversation_id uuid, user_id uuid, nickname text, PRIMARY KEY ((conversation_id), user_id))");
        session.execute("CREATE TABLE IF NOT EXISTS chat.message_reactions (conversation_id uuid, message_id timeuuid, user_id uuid, reaction_emoji text, PRIMARY KEY ((conversation_id), message_id, user_id, reaction_emoji))");
        session.execute("CREATE TABLE IF NOT EXISTS chat.conversation_participants (conversation_id uuid, user_id uuid, PRIMARY KEY (conversation_id, user_id))");
        session.execute("CREATE TABLE IF NOT EXISTS chat.conversations (conversation_id uuid, type text, name text, created_at timestamp, PRIMARY KEY (conversation_id))");

        return session;
    }

    @Bean
    public PreparedStatement insertParticipantStmt(CqlSession session) {
        return session.prepare(
                "INSERT INTO chat.conversation_participants (conversation_id, user_id) VALUES (?, ?)"
        );
    }

    @Bean
    public PreparedStatement selectParticipantsStmt(CqlSession session) {
        return session.prepare(
                "SELECT user_id FROM chat.conversation_participants WHERE conversation_id = ?"
        );
    }

    @Bean
    public PreparedStatement insertMessageStmt(CqlSession session) {
        return session.prepare(
                "INSERT INTO chat.messages (conversation_id, message_id, sender_id, message_text, reply_to_message_id, reply_to_text, reply_to_sender_id, image_url, audio_url, duration, file_url, file_name, file_size, link_url, system_action_type, system_action_payload) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"
        );
    }

    @Bean
    public PreparedStatement insertInboxStmt(CqlSession session) {
        return session.prepare(
                "INSERT INTO chat.user_inbox (user_id, last_activity, conversation_id, last_message_text, is_unread, recipient_id, last_message_sender_id) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)"
        );
    }

    @Bean
    public PreparedStatement selectInboxStmt(CqlSession session) {
        return session.prepare(
                "SELECT conversation_id, last_activity, last_message_text, is_unread, recipient_id, last_message_sender_id " +
                "FROM chat.user_inbox WHERE user_id = ?"
        );
    }

    @Bean
    public PreparedStatement selectMessagesStmt(CqlSession session) {
        return session.prepare(
                "SELECT conversation_id, message_id, sender_id, message_text, reactions, reply_to_message_id, reply_to_text, reply_to_sender_id, image_url, audio_url, duration, is_pinned, file_url, file_name, file_size, link_url, system_action_type, system_action_payload " +
                "FROM chat.messages WHERE conversation_id = ?"
        );
    }

    @Bean
    public PreparedStatement updateInboxReadStmt(CqlSession session) {
        return session.prepare(
                "UPDATE chat.user_inbox SET is_unread = false WHERE user_id = ? AND conversation_id = ?"
        );
    }

    @Bean
    public PreparedStatement updateMessageReactionsStmt(CqlSession session) {
        return session.prepare(
                "UPDATE chat.messages SET reactions = ? WHERE conversation_id = ? AND message_id = ?"
        );
    }

    @Bean
    public PreparedStatement updateMessagePinnedStmt(CqlSession session) {
        return session.prepare(
                "UPDATE chat.messages SET is_pinned = ? WHERE conversation_id = ? AND message_id = ?"
        );
    }

    @Bean
    public PreparedStatement insertReadReceiptStmt(CqlSession session) {
        return session.prepare(
                "INSERT INTO chat.read_receipts (conversation_id, user_id, last_read_message_id) VALUES (?, ?, ?)"
        );
    }

    @Bean
    public PreparedStatement deleteParticipantStmt(CqlSession session) {
        return session.prepare(
                "DELETE FROM chat.conversation_participants WHERE conversation_id = ? AND user_id = ?"
        );
    }

    @Bean
    public PreparedStatement deleteInboxStmt(CqlSession session) {
        return session.prepare(
                "DELETE FROM chat.user_inbox WHERE user_id = ? AND conversation_id = ?"
        );
    }

    @Bean
    public PreparedStatement selectReadReceiptsStmt(CqlSession session) {
        return session.prepare(
                "SELECT user_id, last_read_message_id FROM chat.read_receipts WHERE conversation_id = ?"
        );
    }

    @Bean
    public PreparedStatement insertCustomizationStmt(CqlSession session) {
        return session.prepare(
                "INSERT INTO chat.conversation_customization (conversation_id, theme_id, emoji) VALUES (?, ?, ?)"
        );
    }

    @Bean
    public PreparedStatement selectCustomizationStmt(CqlSession session) {
        return session.prepare(
                "SELECT theme_id, emoji FROM chat.conversation_customization WHERE conversation_id = ?"
        );
    }

    @Bean
    public PreparedStatement insertNicknameStmt(CqlSession session) {
        return session.prepare(
                "INSERT INTO chat.conversation_nicknames (conversation_id, user_id, nickname) VALUES (?, ?, ?)"
        );
    }

    @Bean
    public PreparedStatement selectNicknamesStmt(CqlSession session) {
        return session.prepare(
                "SELECT user_id, nickname FROM chat.conversation_nicknames WHERE conversation_id = ?"
        );
    }

    @Bean
    public PreparedStatement insertReactionStmt(CqlSession session) {
        return session.prepare(
                "INSERT INTO chat.message_reactions (conversation_id, message_id, user_id, reaction_emoji) VALUES (?, ?, ?, ?)"
        );
    }

    @Bean
    public PreparedStatement deleteReactionStmt(CqlSession session) {
        return session.prepare(
                "DELETE FROM chat.message_reactions WHERE conversation_id = ? AND message_id = ? AND user_id = ? AND reaction_emoji = ?"
        );
    }

    @Bean
    public PreparedStatement selectReactionsForConversationStmt(CqlSession session) {
        return session.prepare(
                "SELECT message_id, user_id, reaction_emoji FROM chat.message_reactions WHERE conversation_id = ?"
        );
    }

    @Bean
    public PreparedStatement selectReactionStmt(CqlSession session) {
        return session.prepare(
                "SELECT user_id FROM chat.message_reactions WHERE conversation_id = ? AND message_id = ? AND user_id = ? AND reaction_emoji = ?"
        );
    }

    @Bean
    public PreparedStatement insertConversationStmt(CqlSession session) {
        return session.prepare(
                "INSERT INTO chat.conversations (conversation_id, type, name, created_at) VALUES (?, ?, ?, ?)"
        );
    }

    @Bean
    public PreparedStatement selectConversationStmt(CqlSession session) {
        return session.prepare(
                "SELECT type, name, created_at FROM chat.conversations WHERE conversation_id = ?"
        );
    }
}
