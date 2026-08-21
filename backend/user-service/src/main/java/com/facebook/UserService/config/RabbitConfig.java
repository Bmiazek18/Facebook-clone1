package com.facebook.UserService.config;

import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    public static final String EXCHANGE_NAME = "user.events";
    public static final String PAGE_QUEUE = "page.search-indexing";
    public static final String PAGE_ROUTING_KEY = "page.index";

    @Bean
    public TopicExchange userEventsExchange() {
        return new TopicExchange(EXCHANGE_NAME, true, false);
    }

    @Bean
    public org.springframework.amqp.core.Queue pageSearchIndexingQueue() {
        return new org.springframework.amqp.core.Queue(PAGE_QUEUE, true);
    }

    @Bean
    public org.springframework.amqp.core.Binding pageSearchIndexingBinding(org.springframework.amqp.core.Queue pageSearchIndexingQueue, TopicExchange userEventsExchange) {
        return org.springframework.amqp.core.BindingBuilder.bind(pageSearchIndexingQueue).to(userEventsExchange).with(PAGE_ROUTING_KEY);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
