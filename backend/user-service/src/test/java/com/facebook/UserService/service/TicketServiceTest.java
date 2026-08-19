package com.facebook.UserService.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TicketServiceTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    private TicketService ticketService;

    @BeforeEach
    void setUp() {
        ticketService = new TicketService(redisTemplate);
    }

    @Test
    void testGenerateTicket() {
        String userId = "user-123";
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        String ticket = ticketService.generateTicket(userId);

        assertNotNull(ticket);
        assertFalse(ticket.isEmpty());
        verify(valueOperations).set(eq("ticket:" + ticket), eq(userId), eq(60L), eq(TimeUnit.SECONDS));
    }

    @Test
    void testValidateTicket_Success() {
        String ticket = "ticket-123";
        String userId = "user-123";
        String key = "ticket:" + ticket;

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(key)).thenReturn(userId);

        boolean isValid = ticketService.validateTicket(ticket);

        assertTrue(isValid);
        verify(redisTemplate).delete(key);
    }

    @Test
    void testValidateTicket_ExpiredOrInvalid() {
        String ticket = "expired-ticket";
        String key = "ticket:" + ticket;

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(key)).thenReturn(null);

        boolean isValid = ticketService.validateTicket(ticket);

        assertFalse(isValid);
        verify(redisTemplate, never()).delete(anyString());
    }

    @Test
    void testValidateTicketForUser_Success() {
        String ticket = "ticket-123";
        String userId = "user-123";
        String key = "ticket:" + ticket;

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(key)).thenReturn(userId);

        boolean isValid = ticketService.validateTicketForUser(ticket, userId);

        assertTrue(isValid);
        verify(redisTemplate).delete(key);
    }

    @Test
    void testValidateTicketForUser_WrongUser() {
        String ticket = "ticket-123";
        String userId = "user-123";
        String wrongUserId = "user-999";
        String key = "ticket:" + ticket;

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(key)).thenReturn(userId);

        boolean isValid = ticketService.validateTicketForUser(ticket, wrongUserId);

        assertFalse(isValid);
        verify(redisTemplate, never()).delete(anyString());
    }

    @Test
    void testGetUserIdForTicket_Success() {
        String ticket = "ticket-123";
        String userId = "user-123";
        String key = "ticket:" + ticket;

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(key)).thenReturn(userId);

        String result = ticketService.getUserIdForTicket(ticket);

        assertEquals(userId, result);
        verify(redisTemplate).delete(key);
    }
}
