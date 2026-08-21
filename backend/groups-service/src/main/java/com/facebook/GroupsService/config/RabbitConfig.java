package com.facebook.GroupsService.config;

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

    public static final String EXCHANGE_NAME = "groups.events";
    public static final String QUEUE_NAME = "groups.search-indexing";
    public static final String ROUTING_KEY = "groups.index";

    @Bean
    public TopicExchange groupsEventsExchange() {
        return new TopicExchange(EXCHANGE_NAME, true, false);
    }

    @Bean
    public Queue groupsSearchIndexingQueue() {
        return new Queue(QUEUE_NAME, true);
    }

    @Bean
    public Binding groupsSearchIndexingBinding(Queue groupsSearchIndexingQueue, TopicExchange groupsEventsExchange) {
        return BindingBuilder.bind(groupsSearchIndexingQueue).to(groupsEventsExchange).with(ROUTING_KEY);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
