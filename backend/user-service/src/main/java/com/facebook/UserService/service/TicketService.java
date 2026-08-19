package com.facebook.UserService.service;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class TicketService {

    private final StringRedisTemplate redisTemplate;

    public TicketService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public String generateTicket(String userId) {
        String ticket = UUID.randomUUID().toString();
        String key = "ticket:" + ticket;
        // Ticket is valid for 60 seconds
        redisTemplate.opsForValue().set(key, userId, 60, TimeUnit.SECONDS);
        return ticket;
    }

    public boolean validateTicket(String ticket) {
        if (ticket == null || ticket.trim().isEmpty()) {
            return false;
        }
        String key = "ticket:" + ticket;
        String userId = redisTemplate.opsForValue().get(key);
        if (userId != null) {
            redisTemplate.delete(key);
            return true;
        }
        return false;
    }

    public boolean validateTicketForUser(String ticket, String userId) {
        if (ticket == null || ticket.trim().isEmpty() || userId == null) {
            return false;
        }
        String key = "ticket:" + ticket;
        String storedUserId = redisTemplate.opsForValue().get(key);
        if (storedUserId != null && storedUserId.equals(userId)) {
            redisTemplate.delete(key);
            return true;
        }
        return false;
    }

    public String getUserIdForTicket(String ticket) {
        if (ticket == null || ticket.trim().isEmpty()) {
            return null;
        }
        String key = "ticket:" + ticket;
        String userId = redisTemplate.opsForValue().get(key);
        if (userId != null) {
            redisTemplate.delete(key);
            return userId;
        }
        return null;
    }
}
