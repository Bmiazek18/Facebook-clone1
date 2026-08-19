package com.facebook.SearchService.model;

import lombok.Data;
import java.util.UUID;

@Data
public class User {
    private UUID id;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private String avatarId;
}
