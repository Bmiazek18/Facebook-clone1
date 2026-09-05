package com.facebook.GroupsService.service;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class RedisActivityTracker {

    private final StringRedisTemplate redisTemplate;

    public RedisActivityTracker(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * Records a new activity event (e.g. comment posted) inside the sliding window log.
     *
     * @param key                 The Redis key for this activity log (e.g. "activity:group:123:user:456" or "post:789:comments")
     * @param windowSizeSeconds   The size of the sliding window in seconds (used to set key expiration TTL)
     */
    public void recordActivity(String key, long windowSizeSeconds) {
        long now = System.currentTimeMillis();
        String memberId = UUID.randomUUID().toString(); // unique member identifier to prevent collisions

        // Add element to Sorted Set (ZADD key score member)
        redisTemplate.opsForZSet().add(key, memberId, now);

        // Keep elements in the sorted set for at least the window duration
        redisTemplate.expire(key, windowSizeSeconds + 60, TimeUnit.SECONDS);
    }

    /**
     * Prunes expired elements and returns the count of active items in the sliding window.
     *
     * @param key               The Redis key for this activity log
     * @param windowSizeSeconds The size of the sliding window in seconds
     * @return The count of events that occurred within the sliding window
     */
    public long getActivityCount(String key, long windowSizeSeconds) {
        long now = System.currentTimeMillis();
        long threshold = now - (windowSizeSeconds * 1000);

        // Remove elements older than threshold (ZREMRANGEBYSCORE key -inf threshold)
        redisTemplate.opsForZSet().removeRangeByScore(key, 0, threshold);

        // Return count of remaining elements (ZCARD key)
        Long count = redisTemplate.opsForZSet().zCard(key);
        return count != null ? count : 0;
    }
}
