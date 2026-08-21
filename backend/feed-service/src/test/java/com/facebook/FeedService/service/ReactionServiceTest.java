package com.facebook.FeedService.service;

import com.facebook.FeedService.dto.ReactionRequest;
import com.facebook.FeedService.kafka.ReactionKafkaProducer;
import com.facebook.FeedService.repository.ReactionRepository;
import com.github.benmanes.caffeine.cache.Cache;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.Optional;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReactionServiceTest {

    private Cache<String, java.util.Map<String, Long>> l1Cache;
    @Mock
    private StringRedisTemplate stringRedisTemplate;
    @Mock
    private ReactionKafkaProducer reactionKafkaProducer;
    @Mock
    private ReactionRepository reactionRepository;

    private ReactionService reactionService;

    @BeforeEach
    void setUp() {
        l1Cache = com.github.benmanes.caffeine.cache.Caffeine.newBuilder().build();
        reactionService = new ReactionService(l1Cache, stringRedisTemplate, reactionKafkaProducer, reactionRepository);
    }

    @Test
    void testReactNewReaction() {
        ReactionRequest request = ReactionRequest.builder()
                .userId("user1")
                .reactionType("LIKE")
                .build();

        when(reactionRepository.findByUserIdAndPostId("user1", "post1")).thenReturn(Optional.empty());

        reactionService.react("post1", request);

        verify(reactionRepository, times(1)).findByUserIdAndPostId("user1", "post1");
        verify(reactionKafkaProducer, times(1)).sendReactionEvent(any());
    }
}
