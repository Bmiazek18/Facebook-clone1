package com.facebook.analytics.service;

import com.facebook.analytics.dto.BotDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class BotDetectionService {

    private static final Logger log = LoggerFactory.getLogger(BotDetectionService.class);

    private final JdbcTemplate jdbcTemplate;
    private final List<String> cachedBotIds = new java.util.concurrent.CopyOnWriteArrayList<>();

    @Autowired
    public BotDetectionService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @jakarta.annotation.PostConstruct
    public void init() {
        log.info("Loading initial bot blacklist from ClickHouse...");
        try {
            List<String> botIds = jdbcTemplate.queryForList(
                    "SELECT user_id FROM detected_bots FINAL",
                    String.class
            );
            cachedBotIds.addAll(botIds);
            log.info("Loaded {} blacklisted bot users.", cachedBotIds.size());
        } catch (Exception e) {
            log.error("Failed to load bot blacklist: {}", e.getMessage());
        }
    }

    public boolean isBot(String userId) {
        if (userId == null) return false;
        return cachedBotIds.contains(userId);
    }

    /**
     * Scan ClickHouse user interactions every 10 seconds to detect anomalies.
     */
    @Scheduled(fixedRate = 10000)
    public void detectAndStoreBots() {
        log.info("Running real-time bot and spam detection scan...");
        try {
            // Find bots based on interaction count (> 100) or unique targets (> 50) or average dwell time (< 200ms with > 10 targets)
            String scanQuery = "SELECT user_id, " +
                    "count() as target_count, " +
                    "sum(interaction_count) as total_interactions, " +
                    "avg(total_dwell_time_ms) as avg_dwell " +
                    "FROM user_item_interactions " +
                    "WHERE last_interacted_at >= now() - INTERVAL 10 MINUTE " +
                    "GROUP BY user_id " +
                    "HAVING target_count > 50 OR total_interactions > 100 OR (target_count > 10 AND avg_dwell < 200)";

            List<Map<String, Object>> suspiciousUsers = jdbcTemplate.queryForList(scanQuery);

            for (Map<String, Object> user : suspiciousUsers) {
                String userId = (String) user.get("user_id");
                Long targetCount = ((Number) user.get("target_count")).longValue();
                Long totalInteractions = ((Number) user.get("total_interactions")).longValue();
                Double avgDwell = ((Number) user.get("avg_dwell")).doubleValue();

                String reason;
                long count;
                if (targetCount > 50) {
                    reason = "High target density: " + targetCount + " unique items in 10 minutes";
                    count = targetCount;
                } else if (totalInteractions > 100) {
                    reason = "High rate of actions: " + totalInteractions + " interactions in 10 minutes";
                    count = totalInteractions;
                } else {
                    reason = "Rapid skipping: average dwell time " + Math.round(avgDwell) + "ms across " + targetCount + " items";
                    count = targetCount;
                }

                log.warn("Bot detected! User: {}, Reason: {}", userId, reason);

                if (!cachedBotIds.contains(userId)) {
                    cachedBotIds.add(userId);
                }

                // Insert/Upsert into detected_bots table in ClickHouse
                // Since detected_bots uses ReplacingMergeTree, inserting duplicates will be merged automatically
                jdbcTemplate.update(
                        "INSERT INTO detected_bots (user_id, reason, event_count, detected_at) VALUES (?, ?, ?, now())",
                        userId, reason, count
                );
            }
        } catch (Exception e) {
            log.error("Error during bot detection scan: {}", e.getMessage(), e);
        }
    }

    /**
     * Get all detected bots from ClickHouse
     */
    public List<BotDto> getDetectedBots() {
        List<BotDto> bots = new ArrayList<>();
        try {
            // Using FINAL to collapse duplicate user_ids under ReplacingMergeTree
            String query = "SELECT user_id, reason, event_count, detected_at FROM detected_bots FINAL ORDER BY detected_at DESC";
            List<Map<String, Object>> rows = jdbcTemplate.queryForList(query);
            for (Map<String, Object> row : rows) {
                bots.add(new BotDto(
                        (String) row.get("user_id"),
                        (String) row.get("reason"),
                        ((Number) row.get("event_count")).longValue(),
                        (LocalDateTime) row.get("detected_at")
                ));
            }
        } catch (Exception e) {
            log.error("Error retrieving detected bots: {}", e.getMessage(), e);
        }
        return bots;
    }

    /**
     * Register a bot detected by Kafka Streams
     */
    public void registerDetectedBot(String userId, String reason, long count) {
        try {
            if (!cachedBotIds.contains(userId)) {
                cachedBotIds.add(userId);
            }
            jdbcTemplate.update(
                    "INSERT INTO detected_bots (user_id, reason, event_count, detected_at) VALUES (?, ?, ?, now())",
                    userId, reason, count
            );
            log.info("Bot {} registered successfully from Kafka Streams.", userId);
        } catch (Exception e) {
            log.error("Failed to register bot {} from Kafka Streams: {}", userId, e.getMessage());
        }
    }
}
