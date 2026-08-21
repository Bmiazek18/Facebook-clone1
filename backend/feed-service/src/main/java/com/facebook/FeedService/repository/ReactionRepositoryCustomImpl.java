package com.facebook.FeedService.repository;

import com.facebook.FeedService.entity.ReactionEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;

@RequiredArgsConstructor
public class ReactionRepositoryCustomImpl implements ReactionRepositoryCustom {

    private final JdbcTemplate jdbcTemplate;

    @Override
    @Transactional
    public void batchUpsert(List<ReactionEntity> reactions) {
        if (reactions == null || reactions.isEmpty()) {
            return;
        }

        String sql = "INSERT INTO reactions (user_id, post_id, reaction_type, created_at) " +
                     "VALUES (?, ?, ?, ?) " +
                     "ON CONFLICT (user_id, post_id) " +
                     "DO UPDATE SET reaction_type = EXCLUDED.reaction_type, created_at = EXCLUDED.created_at";

        jdbcTemplate.batchUpdate(sql, reactions, 1000, (PreparedStatement ps, ReactionEntity reaction) -> {
            ps.setString(1, reaction.getUserId());
            ps.setString(2, reaction.getPostId());
            ps.setString(3, reaction.getReactionType());
            ps.setTimestamp(4, Timestamp.from(reaction.getCreatedAt() != null ? reaction.getCreatedAt() : Instant.now()));
        });
    }
}
