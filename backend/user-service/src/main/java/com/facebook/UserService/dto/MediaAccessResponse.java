package com.facebook.UserService.dto;

public record MediaAccessResponse(
        String objectKey,
        String stableUrl,
        String presignedUrl,
        long expiresAt
) {}
