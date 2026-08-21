package com.facebook.NotificationService.dto;

import lombok.Data;

@Data
public class WebPushSubscriptionRequest {
    private String endpoint;
    private String p256dh;
    private String auth;
}
