package com.facebook.marketplace.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    public static final String EXCHANGE_NAME = "marketplace.events";
    public static final String QUEUE_NAME = "marketplace.search-indexing";
    public static final String ROUTING_KEY = "marketplace.index";

    @Bean
    public TopicExchange marketplaceEventsExchange() {
        return new TopicExchange(EXCHANGE_NAME, true, false);
    }

    @Bean
    public Queue marketplaceSearchIndexingQueue() {
        return new Queue(QUEUE_NAME, true);
    }

    @Bean
    public Binding marketplaceSearchIndexingBinding(Queue marketplaceSearchIndexingQueue, TopicExchange marketplaceEventsExchange) {
        return BindingBuilder.bind(marketplaceSearchIndexingQueue).to(marketplaceEventsExchange).with(ROUTING_KEY);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
