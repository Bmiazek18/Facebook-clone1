package com.facebook.UserService.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterResponse {
    private java.util.UUID id;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private String message;
}
