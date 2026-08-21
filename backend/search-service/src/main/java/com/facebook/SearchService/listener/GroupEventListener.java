package com.facebook.SearchService.listener;

import com.facebook.GroupsService.event.GroupIndexEvent;
import com.facebook.SearchService.service.SearchService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class GroupEventListener {

    private final SearchService searchService;

    public GroupEventListener(SearchService searchService) {
        this.searchService = searchService;
    }

    @RabbitListener(queues = "groups.search-indexing")
    public void handleGroupIndexEvent(GroupIndexEvent event) {
        System.out.println("RabbitMQ: Received group index event for group: " + event.getId());
        try {
            searchService.indexGroup(event);
        } catch (Exception e) {
            System.err.println("RabbitMQ: Failed to process group index event: " + e.getMessage());
        }
    }
}
