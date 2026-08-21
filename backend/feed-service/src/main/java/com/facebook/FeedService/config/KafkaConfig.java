package com.facebook.FeedService.config;

import com.facebook.FeedService.dto.ReactionEvent;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.*;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;

import org.springframework.kafka.annotation.EnableKafka;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableKafka
public class KafkaConfig {

    @Value("${spring.kafka.bootstrap-servers:localhost:9092}")
    private String bootstrapServers;

    public static final String REACTIONS_TOPIC = "reactions-topic";

    @Bean
    public NewTopic reactionsTopic() {
        return TopicBuilder.name(REACTIONS_TOPIC)
                .partitions(3)
                .replicas(1)
                .build();
    }

    // Producer Configuration
    @Bean
    public ProducerFactory<String, ReactionEvent> producerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        
        // Optimizing for high throughput and batching
        configProps.put(ProducerConfig.LINGER_MS_CONFIG, 20); // wait 20ms for batching before sending
        configProps.put(ProducerConfig.BATCH_SIZE_CONFIG, 65536); // 64KB batch size
        configProps.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, "snappy"); // compression to reduce bandwidth
        configProps.put(ProducerConfig.ACKS_CONFIG, "1"); // fast write confirmation
        
        return new DefaultKafkaProducerFactory<>(configProps);
    }

    @Bean
    public KafkaTemplate<String, ReactionEvent> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }

    // Consumer Configuration for Database Sync Worker
    @Bean
    public ConsumerFactory<String, ReactionEvent> consumerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
        configProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
        configProps.put(ErrorHandlingDeserializer.KEY_DESERIALIZER_CLASS, StringDeserializer.class);
        configProps.put(ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, JsonDeserializer.class);
        
        // Deserializer properties
        configProps.put(JsonDeserializer.TRUSTED_PACKAGES, "com.facebook.FeedService.dto");
        configProps.put(JsonDeserializer.VALUE_DEFAULT_TYPE, "com.facebook.FeedService.dto.ReactionEvent");
        
        // Batch properties for DB sync worker
        configProps.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 1000); // Poll up to 1000 records
        configProps.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false); // Manual commit for data safety
        
        return new DefaultKafkaConsumerFactory<>(configProps);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, ReactionEvent> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, ReactionEvent> factory = 
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        
        // Enable batch listening mode
        factory.setBatchListener(true);
        
        // Use manual immediate acknowledgment
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);
        
        return factory;
    }
}
