package com.facebook.UserService.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreatePageRequest {
    private String ownerId;
    private String pageName;
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
    private String profileImage;
    private String coverImage;
    private Boolean pageNotifications;
    private Boolean promotionalEmails;

    public String getEffectiveName() {
        if (pageName != null && !pageName.trim().isEmpty()) {
            return pageName.trim();
        }
        if (name != null && !name.trim().isEmpty()) {
            return name.trim();
        }
        return "Nowa Strona";
    }
}
