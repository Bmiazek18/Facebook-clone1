package com.facebook.UserService.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserIndexEvent {
    private String id;
    private String username;
    private String firstName;
    private String lastName;
    private String avatarId;
}
