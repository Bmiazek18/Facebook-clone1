package com.facebook.UserService.dto;

public class UserIndexEvent {
    private String id;
    private String username;
    private String firstName;
    private String lastName;
    private String avatarId;

    public UserIndexEvent() {
    }

    public UserIndexEvent(String id, String username, String firstName, String lastName, String avatarId) {
        this.id = id;
        this.username = username;
        this.firstName = firstName;
        this.lastName = lastName;
        this.avatarId = avatarId;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String id;
        private String username;
        private String firstName;
        private String lastName;
        private String avatarId;

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder username(String username) {
            this.username = username;
            return this;
        }

        public Builder firstName(String firstName) {
            this.firstName = firstName;
            return this;
        }

        public Builder lastName(String lastName) {
            this.lastName = lastName;
            return this;
        }

        public Builder avatarId(String avatarId) {
            this.avatarId = avatarId;
            return this;
        }

        public UserIndexEvent build() {
            return new UserIndexEvent(id, username, firstName, lastName, avatarId);
        }
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getAvatarId() {
        return avatarId;
    }

    public void setAvatarId(String avatarId) {
        this.avatarId = avatarId;
    }
}
