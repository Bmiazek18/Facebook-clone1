package com.facebook.SearchService.listener;

import com.facebook.marketplace.event.ListingIndexEvent;
import com.facebook.SearchService.service.SearchService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class ListingEventListener {

    private final SearchService searchService;

    public ListingEventListener(SearchService searchService) {
        this.searchService = searchService;
    }

    @RabbitListener(queues = "marketplace.search-indexing")
    public void handleListingIndexEvent(ListingIndexEvent event) {
        System.out.println("RabbitMQ: Received listing index event for listing: " + event.getId());
        try {
            searchService.indexListing(event);
        } catch (Exception e) {
            System.err.println("RabbitMQ: Failed to process listing index event: " + e.getMessage());
        }
    }
}
