package com.facebook.analytics.kafka;

import com.facebook.analytics.config.KafkaConsumerConfig;
import com.facebook.analytics.dto.ImpressionEvent;
import com.facebook.analytics.dto.ReactionEvent;
import com.facebook.analytics.service.AnalyticsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class AnalyticsKafkaConsumer {

    private static final Logger log = LoggerFactory.getLogger(AnalyticsKafkaConsumer.class);

    private final AnalyticsService analyticsService;

    @Autowired
    public AnalyticsKafkaConsumer(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @KafkaListener(
            topics = KafkaConsumerConfig.IMPRESSIONS_TOPIC,
            groupId = "analytics-impressions-group",
            containerFactory = "impressionKafkaListenerContainerFactory"
    )
    public void consumeImpression(ImpressionEvent event) {
        try {
            analyticsService.processImpressionEvent(event);
        } catch (Exception e) {
            log.error("Failed to process impression from Kafka: {}", e.getMessage(), e);
        }
    }

    @KafkaListener(
            topics = KafkaConsumerConfig.REACTIONS_TOPIC,
            groupId = "analytics-reactions-group",
            containerFactory = "reactionKafkaListenerContainerFactory"
    )
    public void consumeReaction(ReactionEvent event) {
        try {
            analyticsService.processReactionEvent(event);
        } catch (Exception e) {
            log.error("Failed to process reaction from Kafka: {}", e.getMessage(), e);
        }
    }
}
