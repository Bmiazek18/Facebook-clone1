package com.facebook.UserService.dto;

import java.util.UUID;

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

    public PageDto() {
    }

    public PageDto(UUID id, UUID ownerId, String name, String category, String bio, String website, String phoneCode, String phone, String email, String address, String city, String zip, String hours, String avatar, String cover, Boolean pageNotifications, Boolean promotionalEmails, String createdAt, String updatedAt) {
        this.id = id;
        this.ownerId = ownerId;
        this.name = name;
        this.category = category;
        this.bio = bio;
        this.website = website;
        this.phoneCode = phoneCode;
        this.phone = phone;
        this.email = email;
        this.address = address;
        this.city = city;
        this.zip = zip;
        this.hours = hours;
        this.avatar = avatar;
        this.cover = cover;
        this.pageNotifications = pageNotifications;
        this.promotionalEmails = promotionalEmails;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
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

        public Builder id(UUID id) {
            this.id = id;
            return this;
        }

        public Builder ownerId(UUID ownerId) {
            this.ownerId = ownerId;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder category(String category) {
            this.category = category;
            return this;
        }

        public Builder bio(String bio) {
            this.bio = bio;
            return this;
        }

        public Builder website(String website) {
            this.website = website;
            return this;
        }

        public Builder phoneCode(String phoneCode) {
            this.phoneCode = phoneCode;
            return this;
        }

        public Builder phone(String phone) {
            this.phone = phone;
            return this;
        }

        public Builder email(String email) {
            this.email = email;
            return this;
        }

        public Builder address(String address) {
            this.address = address;
            return this;
        }

        public Builder city(String city) {
            this.city = city;
            return this;
        }

        public Builder zip(String zip) {
            this.zip = zip;
            return this;
        }

        public Builder hours(String hours) {
            this.hours = hours;
            return this;
        }

        public Builder avatar(String avatar) {
            this.avatar = avatar;
            return this;
        }

        public Builder cover(String cover) {
            this.cover = cover;
            return this;
        }

        public Builder pageNotifications(Boolean pageNotifications) {
            this.pageNotifications = pageNotifications;
            return this;
        }

        public Builder promotionalEmails(Boolean promotionalEmails) {
            this.promotionalEmails = promotionalEmails;
            return this;
        }

        public Builder createdAt(String createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Builder updatedAt(String updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        public PageDto build() {
            return new PageDto(id, ownerId, name, category, bio, website, phoneCode, phone, email, address, city, zip, hours, avatar, cover, pageNotifications, promotionalEmails, createdAt, updatedAt);
        }
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(UUID ownerId) {
        this.ownerId = ownerId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public String getPhoneCode() {
        return phoneCode;
    }

    public void setPhoneCode(String phoneCode) {
        this.phoneCode = phoneCode;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getZip() {
        return zip;
    }

    public void setZip(String zip) {
        this.zip = zip;
    }

    public String getHours() {
        return hours;
    }

    public void setHours(String hours) {
        this.hours = hours;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getCover() {
        return cover;
    }

    public void setCover(String cover) {
        this.cover = cover;
    }

    public Boolean getPageNotifications() {
        return pageNotifications;
    }

    public void setPageNotifications(Boolean pageNotifications) {
        this.pageNotifications = pageNotifications;
    }

    public Boolean getPromotionalEmails() {
        return promotionalEmails;
    }

    public void setPromotionalEmails(Boolean promotionalEmails) {
        this.promotionalEmails = promotionalEmails;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }
}
