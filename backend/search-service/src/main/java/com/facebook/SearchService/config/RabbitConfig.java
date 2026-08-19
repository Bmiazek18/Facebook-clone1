package com.facebook.SearchService.config;

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

    public static final String EXCHANGE_NAME = "user.events";
    public static final String QUEUE_NAME = "user.search-indexing";
    public static final String ROUTING_KEY_PATTERN = "user.*";

    @Bean
    public TopicExchange userEventsExchange() {
        return new TopicExchange(EXCHANGE_NAME, true, false);
    }

    @Bean
    public Queue userSearchIndexingQueue() {
        return new Queue(QUEUE_NAME, true);
    }

    @Bean
    public Binding userSearchIndexingBinding(Queue userSearchIndexingQueue, TopicExchange userEventsExchange) {
        return BindingBuilder.bind(userSearchIndexingQueue).to(userEventsExchange).with(ROUTING_KEY_PATTERN);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
