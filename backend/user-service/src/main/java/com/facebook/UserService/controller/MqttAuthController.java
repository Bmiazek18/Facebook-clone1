package com.facebook.UserService.controller;

import com.facebook.UserService.service.TicketService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/internal/mqtt-auth")
public class MqttAuthController {

    private final TicketService ticketService;
    private final StringRedisTemplate redisTemplate;

    @Value("${mqtt.service-token:chat-service-internal}")
    private String serviceToken;

    public MqttAuthController(TicketService ticketService, StringRedisTemplate redisTemplate) {
        this.ticketService = ticketService;
        this.redisTemplate = redisTemplate;
    }

    @PostMapping("/user")
    public ResponseEntity<Void> authenticate(@RequestBody Map<String, String> body) {
        String ticket = body.get("username");
        if (ticket == null || ticket.isBlank()) {
            ticket = body.get("password");
        }
        if (serviceToken != null && !serviceToken.isBlank() && serviceToken.equals(ticket)) {
            return ResponseEntity.ok().build();
        }
        String userId = ticketService.getUserIdForTicket(ticket);
        if (userId != null) {
            redisTemplate.opsForValue().set("mqtt_session:" + ticket, userId, 2, TimeUnit.HOURS);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }

    @PostMapping("/acl")
    public ResponseEntity<Void> authorize(@RequestBody Map<String, Object> body) {
        String username = (String) body.get("username");
        String topic = (String) body.get("topic");
        Object accObj = body.get("acc");
        int acc = 1;
        if (accObj instanceof Number number) {
            acc = number.intValue();
        } else if (accObj instanceof String str) {
            try {
                acc = Integer.parseInt(str);
            } catch (NumberFormatException ignored) {}
        }

        if (serviceToken != null && !serviceToken.isBlank() && serviceToken.equals(username)) {
            return ResponseEntity.ok().build();
        }

        if (username != null) {
            String userId = redisTemplate.opsForValue().get("mqtt_session:" + username);
            if (userId != null && topic != null) {
                if (acc == 1 || acc == 4) { // Subscribe
                    if (topic.equals("chat/messages/user/" + userId)) {
                        return ResponseEntity.ok().build();
                    }
                } else if (acc == 2) { // Publish
                    if (topic.startsWith("chat/messages/user/")) {
                        return ResponseEntity.ok().build();
                    }
                }
            }
        }

        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }

    @PostMapping("/superuser")
    public ResponseEntity<Void> superuser() {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }
}
