package com.facebook.FeedService.service;

import com.facebook.FeedService.dto.ReactionEvent;
import com.facebook.FeedService.dto.ReactionRequest;
import com.facebook.FeedService.kafka.ReactionKafkaProducer;
import com.facebook.FeedService.repository.ReactionRepository;
import com.github.benmanes.caffeine.cache.Cache;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.LongAdder;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReactionService {

    private final Cache<String, Map<String, Long>> l1Cache;
    private final Cache<String, Map<String, List<String>>> l1DetailsCache = com.github.benmanes.caffeine.cache.Caffeine.newBuilder()
            .expireAfterWrite(3, TimeUnit.SECONDS)
            .maximumSize(50_000)
            .build();
    private final StringRedisTemplate stringRedisTemplate;
    private final ReactionKafkaProducer reactionKafkaProducer;
    private final ReactionRepository reactionRepository;

    // Buffer to aggregate increments in RAM. Swapped atomically every 2 seconds.
    // Key format: "postId:reactionType" -> Increment
    private final AtomicReference<ConcurrentHashMap<String, LongAdder>> bufferRef = 
            new AtomicReference<>(new ConcurrentHashMap<>());

    private static final String REDIS_PREFIX = "post:%s:reactions";
    private static final String INIT_MARKER = "_init";

    /**
     * Accepts a user reaction, publishes it to Kafka for PostgreSQL sync,
     * and updates the RAM buffers to shield Redis from high write volume.
     */
    public void react(String postId, ReactionRequest request) {
        log.info("Received reaction request for post: {}, user: {}, new: {}", 
                postId, request.getUserId(), request.getReactionType());

        Optional<com.facebook.FeedService.entity.ReactionEntity> existing = 
                reactionRepository.findByUserIdAndPostId(request.getUserId(), postId);

        String previousReactionType = existing.isPresent() ? existing.get().getReactionType() : null;
        String newReactionType = request.getReactionType();

        // If the new reaction type is the same as the previous one, it means toggle off
        if (newReactionType != null && newReactionType.equalsIgnoreCase(previousReactionType)) {
            newReactionType = null;
        }

        if (Objects.equals(newReactionType, previousReactionType)) {
            return;
        }

        final String finalPrevType = previousReactionType;
        final String finalNewType = newReactionType;

        // 1. Asynchronously propagate the event to Kafka
        ReactionEvent event = ReactionEvent.builder()
                .userId(request.getUserId())
                .postId(postId)
                .reactionType(finalNewType)
                .timestamp(System.currentTimeMillis())
                .build();
        reactionKafkaProducer.sendReactionEvent(event);

        // 2. Buffer increments in RAM (shields Redis L2 Cache from high write load)
        ConcurrentHashMap<String, LongAdder> currentBuffer = bufferRef.get();
        
        if (finalNewType != null) {
            currentBuffer.computeIfAbsent(postId + ":" + finalNewType, k -> new LongAdder()).increment();
        }
        if (finalPrevType != null) {
            currentBuffer.computeIfAbsent(postId + ":" + finalPrevType, k -> new LongAdder()).decrement();
        }

        // 3. Immediately update L1 Cache for instance read consistency (RYOW)
        l1Cache.asMap().computeIfPresent(postId, (id, currentCounts) -> {
            Map<String, Long> updated = new ConcurrentHashMap<>(currentCounts);
            if (finalNewType != null) {
                updated.put(finalNewType, updated.getOrDefault(finalNewType, 0L) + 1);
            }
            if (finalPrevType != null) {
                updated.put(finalPrevType, Math.max(0, updated.getOrDefault(finalPrevType, 0L) - 1));
            }
            return updated;
        });

        // 4. Immediately update L1 Details Cache for read consistency (RYOW)
        l1DetailsCache.asMap().computeIfPresent(postId, (id, currentDetails) -> {
            Map<String, List<String>> updated = new ConcurrentHashMap<>();
            currentDetails.forEach((type, ids) -> updated.put(type, new java.util.concurrent.CopyOnWriteArrayList<>(ids)));
            
            if (finalPrevType != null) {
                List<String> ids = updated.get(finalPrevType);
                if (ids != null) {
                    ids.remove(request.getUserId());
                }
            }
            if (finalNewType != null) {
                updated.computeIfAbsent(finalNewType, k -> new java.util.concurrent.CopyOnWriteArrayList<>()).add(request.getUserId());
            }
            return updated;
        });
    }

    /**
     * Reads reaction user lists grouped by reaction type with 3 seconds L1 cache.
     */
    public Map<String, List<String>> getReactionDetails(String postId) {
        return l1DetailsCache.get(postId, id -> {
            List<com.facebook.FeedService.entity.ReactionEntity> list = reactionRepository.findByPostId(id);
            Map<String, List<String>> details = new ConcurrentHashMap<>();
            for (com.facebook.FeedService.entity.ReactionEntity r : list) {
                details.computeIfAbsent(r.getReactionType(), k -> new java.util.concurrent.CopyOnWriteArrayList<>()).add(r.getUserId());
            }
            return details;
        });
    }

    /**
     * Reads reaction counts with multi-level caching (L1 Caffeine -> L2 Redis -> DB).
     * Thread-safe Caffeine load protects the system from Cache Stampede.
     */
    public Map<String, Long> getReactionCounts(String postId) {
        // Caffeine's get is thread-safe and collapses concurrent requests for the same postId
        return l1Cache.get(postId, id -> fetchFromL2OrDb(id));
    }

    /**
     * Internal helper to fetch reaction counts from Redis (L2) or PostgreSQL (DB).
     */
    private Map<String, Long> fetchFromL2OrDb(String postId) {
        String redisKey = String.format(REDIS_PREFIX, postId);
        
        // 1. Try to fetch from Redis (L2)
        Map<Object, Object> redisHash = stringRedisTemplate.opsForHash().entries(redisKey);
        if (redisHash != null && !redisHash.isEmpty()) {
            log.debug("L2 Cache Hit for post: {}", postId);
            return parseRedisHash(redisHash);
        }

        // 2. Cache Miss in Redis -> Query PostgreSQL (DB)
        log.info("L2 Cache Miss for post: {}. Querying DB.", postId);
        List<Object[]> dbResults = reactionRepository.countReactionsGroupByPostId(postId);
        
        Map<String, Long> dbCounts = dbResults.stream()
                .collect(Collectors.toMap(
                        row -> (String) row[0],
                        row -> (Long) row[1],
                        (existing, replacement) -> existing,
                        ConcurrentHashMap::new
                ));

        // 3. Populate Redis (L2) to prevent subsequent DB hits
        Map<String, String> redisWriteMap = dbCounts.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> String.valueOf(entry.getValue())
                ));
        
        // Add special marker to signify that the cache is initialized (prevents Cache Penetration)
        redisWriteMap.put(INIT_MARKER, "1");
        
        stringRedisTemplate.opsForHash().putAll(redisKey, redisWriteMap);
        stringRedisTemplate.expire(redisKey, 24, TimeUnit.HOURS); // TTL to clean up old post caches

        return dbCounts;
    }

    /**
     * Convert Redis Hash representation to Map<String, Long>
     */
    private Map<String, Long> parseRedisHash(Map<Object, Object> hash) {
        Map<String, Long> counts = new ConcurrentHashMap<>();
        hash.forEach((k, v) -> {
            String key = (String) k;
            if (!INIT_MARKER.equals(key)) {
                counts.put(key, Long.parseLong((String) v));
            }
        });
        return counts;
    }

    /**
     * Background task to flush in-memory write buffer to Redis using a Pipeline.
     * Swaps the buffer reference to avoid locking application threads.
     */
    @Scheduled(fixedRate = 2000) // Runs every 2 seconds
    public void flushBufferToRedis() {
        // Swap buffer with a fresh one to minimize thread blocking
        ConcurrentHashMap<String, LongAdder> oldBuffer = bufferRef.getAndSet(new ConcurrentHashMap<>());
        
        if (oldBuffer.isEmpty()) {
            return;
        }

        // Aggregate values to flush
        Map<String, Long> aggregatedUpdates = new HashMap<>();
        oldBuffer.forEach((key, adder) -> {
            long count = adder.sum();
            if (count != 0) {
                aggregatedUpdates.put(key, count);
            }
        });

        if (aggregatedUpdates.isEmpty()) {
            return;
        }

        log.info("Flushing {} reaction aggregates to Redis...", aggregatedUpdates.size());

        try {
            stringRedisTemplate.executePipelined(new SessionCallback<Object>() {
                @Override
                @SuppressWarnings("unchecked")
                public <K, V> Object execute(RedisOperations<K, V> operations) throws DataAccessException {
                    StringRedisTemplate template = (StringRedisTemplate) operations;
                    aggregatedUpdates.forEach((compositeKey, count) -> {
                        String[] parts = compositeKey.split(":");
                        if (parts.length == 2) {
                            String postId = parts[0];
                            String reactionType = parts[1];
                            String redisKey = String.format(REDIS_PREFIX, postId);
                            
                            // Perform atomic HINCRBY
                            template.opsForHash().increment(redisKey, reactionType, count);
                        }
                    });
                    return null;
                }
            });
            log.info("Successfully flushed aggregates to Redis via Pipeline.");
        } catch (Exception e) {
            log.error("Error executing Redis Pipeline to flush reaction aggregates", e);
            // In a production system, we could restore values back to the current buffer on failure.
        }
    }
}
