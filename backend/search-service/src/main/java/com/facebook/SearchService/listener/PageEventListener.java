package com.facebook.SearchService.listener;

import com.facebook.UserService.dto.PageIndexEvent;
import com.facebook.SearchService.service.SearchService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class PageEventListener {

    private final SearchService searchService;

    public PageEventListener(SearchService searchService) {
        this.searchService = searchService;
    }

    @RabbitListener(queues = "page.search-indexing")
    public void handlePageIndexEvent(PageIndexEvent event) {
        System.out.println("RabbitMQ: Received page index event for page: " + event.getId());
        try {
            searchService.indexPage(event);
        } catch (Exception e) {
            System.err.println("RabbitMQ: Failed to process page index event: " + e.getMessage());
        }
    }
}
