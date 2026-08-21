package com.facebook.analytics.kafka;

import com.facebook.analytics.config.KafkaProducerConfig;
import com.facebook.analytics.dto.ImpressionEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class AnalyticsKafkaProducer {

    private static final Logger log = LoggerFactory.getLogger(AnalyticsKafkaProducer.class);

    private final KafkaTemplate<String, ImpressionEvent> kafkaTemplate;

    @Autowired
    private org.springframework.kafka.core.KafkaTemplate<String, String> stringKafkaTemplate;

    private final com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();

    @Autowired
    public AnalyticsKafkaProducer(KafkaTemplate<String, ImpressionEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendImpression(ImpressionEvent event) {
        String key = event.getPageId() != null ? event.getPageId() : (event.getPostId() != null ? event.getPostId() : "anon");
        kafkaTemplate.send(KafkaProducerConfig.IMPRESSIONS_TOPIC, key, event)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Failed to send impression event to Kafka: {}", ex.getMessage());
                    } else {
                        log.debug("Sent impression event to Kafka topic: {}", KafkaProducerConfig.IMPRESSIONS_TOPIC);
                    }
                });
    }

    public void sendTelemetry(com.facebook.analytics.dto.TelemetryEvent event) {
        try {
            String json = objectMapper.writeValueAsString(event);
            String key = event.getUserId() != null ? event.getUserId() : "anon";
            stringKafkaTemplate.send("telemetry-events", key, json)
                    .whenComplete((result, ex) -> {
                        if (ex != null) {
                            log.error("Failed to send telemetry event to Kafka: {}", ex.getMessage());
                        } else {
                            log.debug("Sent telemetry event to Kafka topic: telemetry-events");
                        }
                    });
        } catch (Exception e) {
            log.error("Failed to serialize telemetry event for Kafka: {}", e.getMessage());
        }
    }
}
