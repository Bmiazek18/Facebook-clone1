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

    public static final String MARKETPLACE_EXCHANGE = "marketplace.events";
    public static final String MARKETPLACE_QUEUE = "marketplace.search-indexing";
    public static final String MARKETPLACE_ROUTING_KEY = "marketplace.index";

    public static final String GROUPS_EXCHANGE = "groups.events";
    public static final String GROUPS_QUEUE = "groups.search-indexing";
    public static final String GROUPS_ROUTING_KEY = "groups.index";

    public static final String PAGE_QUEUE = "page.search-indexing";
    public static final String PAGE_ROUTING_KEY = "page.index";

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
    public TopicExchange marketplaceEventsExchange() {
        return new TopicExchange(MARKETPLACE_EXCHANGE, true, false);
    }

    @Bean
    public Queue marketplaceSearchIndexingQueue() {
        return new Queue(MARKETPLACE_QUEUE, true);
    }

    @Bean
    public Binding marketplaceSearchIndexingBinding(Queue marketplaceSearchIndexingQueue, TopicExchange marketplaceEventsExchange) {
        return BindingBuilder.bind(marketplaceSearchIndexingQueue).to(marketplaceEventsExchange).with(MARKETPLACE_ROUTING_KEY);
    }

    @Bean
    public TopicExchange groupsEventsExchange() {
        return new TopicExchange(GROUPS_EXCHANGE, true, false);
    }

    @Bean
    public Queue groupsSearchIndexingQueue() {
        return new Queue(GROUPS_QUEUE, true);
    }

    @Bean
    public Binding groupsSearchIndexingBinding(Queue groupsSearchIndexingQueue, TopicExchange groupsEventsExchange) {
        return BindingBuilder.bind(groupsSearchIndexingQueue).to(groupsEventsExchange).with(GROUPS_ROUTING_KEY);
    }

    @Bean
    public Queue pageSearchIndexingQueue() {
        return new Queue(PAGE_QUEUE, true);
    }

    @Bean
    public Binding pageSearchIndexingBinding(Queue pageSearchIndexingQueue, TopicExchange userEventsExchange) {
        return BindingBuilder.bind(pageSearchIndexingQueue).to(userEventsExchange).with(PAGE_ROUTING_KEY);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
