package com.facebook.FeedService.config;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.Map;
import java.util.concurrent.TimeUnit;

@Configuration
public class CacheConfig {

    // L1 Local Cache Bean with 3 seconds TTL to protect against Cache Stampede on hot posts
    @Bean
    public Cache<String, Map<String, Long>> l1Cache() {
        return Caffeine.newBuilder()
                .expireAfterWrite(3, TimeUnit.SECONDS)
                .maximumSize(50_000) // limit memory usage
                .build();
    }

    // StringRedisTemplate for low-level high-performance Redis access (like pipelining)
    @Bean
    public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory connectionFactory) {
        return new StringRedisTemplate(connectionFactory);
    }
}
