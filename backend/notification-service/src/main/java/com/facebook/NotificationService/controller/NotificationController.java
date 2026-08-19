package com.facebook.NotificationService.controller;

import com.facebook.NotificationService.service.NotificationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/notifications")
@CrossOrigin(origins = "*")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping(value = "/subscribe/{userId}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribe(@PathVariable String userId, @RequestParam(value = "ticket", required = false) String ticket) {
        if (!notificationService.validateTicket(userId, ticket)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid or expired ticket");
        }
        return notificationService.subscribe(userId);
    }

    @GetMapping("/health")
    public String health() {
        return "OK";
    }
}
