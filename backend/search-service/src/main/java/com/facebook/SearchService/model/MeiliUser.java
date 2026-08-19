package com.facebook.SearchService.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MeiliUser {
    private String id;
    private String username;
    private String firstName;
    private String lastName;
    private String avatarId;
}
