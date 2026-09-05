package com.facebook.UserService.dto;

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

    public CreatePageRequest() {
    }

    public CreatePageRequest(String ownerId, String pageName, String name, String category, String bio, String website, String phoneCode, String phone, String email, String address, String city, String zip, String hours, String profileImage, String coverImage, Boolean pageNotifications, Boolean promotionalEmails) {
        this.ownerId = ownerId;
        this.pageName = pageName;
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
        this.profileImage = profileImage;
        this.coverImage = coverImage;
        this.pageNotifications = pageNotifications;
        this.promotionalEmails = promotionalEmails;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
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

        public Builder ownerId(String ownerId) {
            this.ownerId = ownerId;
            return this;
        }

        public Builder pageName(String pageName) {
            this.pageName = pageName;
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

        public Builder profileImage(String profileImage) {
            this.profileImage = profileImage;
            return this;
        }

        public Builder coverImage(String coverImage) {
            this.coverImage = coverImage;
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

        public CreatePageRequest build() {
            return new CreatePageRequest(ownerId, pageName, name, category, bio, website, phoneCode, phone, email, address, city, zip, hours, profileImage, coverImage, pageNotifications, promotionalEmails);
        }
    }

    public String getEffectiveName() {
        if (pageName != null && !pageName.trim().isEmpty()) {
            return pageName.trim();
        }
        if (name != null && !name.trim().isEmpty()) {
            return name.trim();
        }
        return "Nowa Strona";
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    public String getPageName() {
        return pageName;
    }

    public void setPageName(String pageName) {
        this.pageName = pageName;
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

    public String getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }

    public String getCoverImage() {
        return coverImage;
    }

    public void setCoverImage(String coverImage) {
        this.coverImage = coverImage;
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
}
