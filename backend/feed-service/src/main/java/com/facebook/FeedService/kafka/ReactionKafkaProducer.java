package com.facebook.FeedService.kafka;

import com.facebook.FeedService.config.KafkaConfig;
import com.facebook.FeedService.dto.ReactionEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReactionKafkaProducer {

    private final KafkaTemplate<String, ReactionEvent> kafkaTemplate;

    public void sendReactionEvent(ReactionEvent event) {
        // Use postId as the partition key. This guarantees that all reactions for a specific post
        // are processed in the order they were produced by any consumer.
        String key = event.getPostId();
        
        kafkaTemplate.send(KafkaConfig.REACTIONS_TOPIC, key, event)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Failed to send reaction event to Kafka for post: {}", event.getPostId(), ex);
                    } else {
                        log.debug("Reaction event sent successfully to partition: {} with offset: {}", 
                                result.getRecordMetadata().partition(), 
                                result.getRecordMetadata().offset());
                    }
                });
    }
}
