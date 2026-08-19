package com.facebook.UserService.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageDto {
    private UUID id;
    private UUID ownerId;
    private String name;
    private String category;
    private String bio;
    private String website;
    private String phoneCode;
    private String phone;
    private String email;
    private String address;
    private String city;
    private String zip;
    private String hours;
    private String avatar;
    private String cover;
    private Boolean pageNotifications;
    private Boolean promotionalEmails;
    private String createdAt;
    private String updatedAt;
}
