package com.facebook.SearchService.listener;

import com.facebook.SearchService.model.User;
import com.facebook.SearchService.service.SearchService;
import com.facebook.UserService.dto.UserIndexEvent;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import java.util.UUID;

@Component
public class UserEventListener {

    private final SearchService searchService;

    public UserEventListener(SearchService searchService) {
        this.searchService = searchService;
    }

    @RabbitListener(queues = "user.search-indexing")
    public void handleUserIndexEvent(UserIndexEvent event) {
        if (event.getUsername() != null && event.getUsername().startsWith("page_")) {
            System.out.println("RabbitMQ: Ignoring page-linked dummy user index event for username: " + event.getUsername());
            return;
        }
        System.out.println("RabbitMQ: Received user index event for user: " + event.getId());
        try {
            User user = new User();
            user.setId(UUID.fromString(event.getId()));
            user.setUsername(event.getUsername());
            user.setFirstName(event.getFirstName());
            user.setLastName(event.getLastName());
            user.setAvatarId(event.getAvatarId());
            
            searchService.indexUser(user);
        } catch (Exception e) {
            System.err.println("RabbitMQ: Failed to process user index event: " + e.getMessage());
        }
    }
}
