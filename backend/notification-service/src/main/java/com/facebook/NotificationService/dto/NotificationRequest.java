package com.facebook.NotificationService.dto;

import lombok.Data;

@Data
public class NotificationRequest {
    private String userId;
    private String title;
    private String message;
    private String targetId;
}
