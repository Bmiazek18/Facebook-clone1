package com.facebook.analytics.config;

import com.facebook.analytics.dto.TelemetryEvent;
import com.facebook.analytics.service.BotDetectionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.Grouped;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.kstream.TimeWindows;
import org.apache.kafka.streams.kstream.Windowed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafkaStreams;

import java.time.Duration;

@Configuration
@EnableKafkaStreams
public class KafkaStreamsConfig {

    private static final Logger log = LoggerFactory.getLogger(KafkaStreamsConfig.class);
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private BotDetectionService botDetectionService;

    @Bean
    public KStream<String, String> kStream(StreamsBuilder kStreamBuilder) {
        // 1. Consume from "telemetry-events" topic
        KStream<String, String> telemetryStream = kStreamBuilder.stream(
                "telemetry-events",
                Consumed.with(Serdes.String(), Serdes.String())
        );

        // 2. Parse JSON strings into TelemetryEvent objects
        KStream<String, TelemetryEvent> parsedStream = telemetryStream
                .map((key, json) -> {
                    try {
                        TelemetryEvent event = objectMapper.readValue(json, TelemetryEvent.class);
                        return new KeyValue<>(event.getUserId(), event);
                    } catch (Exception e) {
                        log.error("Failed to parse telemetry event JSON in Kafka Streams: {}", e.getMessage());
                        return new KeyValue<>(null, null);
                    }
                })
                .filter((key, event) -> event != null && event.getUserId() != null && !event.getUserId().equals("0") && !event.getUserId().equals("1"));

        // 3. Define a sliding time window (e.g. 1 minute tumbling window)
        Duration windowSize = Duration.ofMinutes(1);
        TimeWindows timeWindows = TimeWindows.ofSizeWithNoGrace(windowSize);

        // 4. Group by user_id, count events in the window
        parsedStream
                .groupByKey(Grouped.with(Serdes.String(), new org.springframework.kafka.support.serializer.JsonSerde<>(TelemetryEvent.class, objectMapper)))
                .windowedBy(timeWindows)
                .count(Materialized.as("telemetry-counts-store"))
                .toStream()
                .filter((windowedKey, count) -> count != null && count > 50) // If user performs >50 actions in 1 minute, they are a bot!
                .map((Windowed<String> windowedKey, Long count) -> {
                    String userId = windowedKey.key();
                    String reason = "Kafka Streams detection: " + count + " telemetry actions within 1 minute";
                    log.warn("KAFKA STREAMS DETECTED BOT! User: {}, Actions count: {}", userId, count);
                    
                    // Directly register the bot in our DB and in-memory cache
                    botDetectionService.registerDetectedBot(userId, reason, count);
                    
                    return new KeyValue<>(userId, reason);
                });

        return telemetryStream;
    }
}
