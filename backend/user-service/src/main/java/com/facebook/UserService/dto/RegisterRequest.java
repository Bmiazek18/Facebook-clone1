package com.facebook.UserService.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {
    @com.fasterxml.jackson.annotation.JsonAlias({"userId", "keycloakUserId"})
    private String userId; // Dodane pole do odbierania UUID z Keycloaka
    private String username;
    private String email;
    private String password;
    private String firstName;
    private String lastName;
    private String birthDate;
}