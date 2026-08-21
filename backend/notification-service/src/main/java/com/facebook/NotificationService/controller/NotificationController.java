package com.facebook.NotificationService.controller;

import com.facebook.NotificationService.dto.WebPushSubscriptionRequest;
import com.facebook.NotificationService.model.WebPushSubscription;
import com.facebook.NotificationService.service.NotificationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/notifications")
@CrossOrigin(origins = "*")
public class NotificationController {

      private final NotificationService notificationService;

      public NotificationController(NotificationService notificationService) {
          this.notificationService = notificationService;
      }

      @PostMapping("/wp-subscription")
      public ResponseEntity<WebPushSubscription> subscribe(
              @RequestHeader(value = "X-User-Id", required = false) String headerUserId,
              @RequestParam(value = "userId", required = false) String paramUserId,
              @RequestBody WebPushSubscriptionRequest request) {
          
          String userId = (headerUserId != null) ? headerUserId : paramUserId;
          if (userId == null || userId.trim().isEmpty()) {
              throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User ID must be provided in X-User-Id header or query parameter");
          }

          WebPushSubscription subscription = notificationService.registerSubscription(
                  userId,
                  request.getEndpoint(),
                  request.getP256dh(),
                  request.getAuth()
          );
          return ResponseEntity.status(HttpStatus.CREATED).body(subscription);
      }

      @DeleteMapping("/wp-subscription")
      public ResponseEntity<Void> unsubscribe(@RequestParam("endpoint") String endpoint) {
          notificationService.unsubscribe(endpoint);
          return ResponseEntity.noContent().build();
      }

      @GetMapping("/health")
      public String health() {
          return "OK";
      }
}
