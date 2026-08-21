package com.facebook.UserService.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageIndexEvent implements Serializable {
    private String id;
    private String name;
    private String category;
    private String avatarUrl;
    private Boolean delete;
}
