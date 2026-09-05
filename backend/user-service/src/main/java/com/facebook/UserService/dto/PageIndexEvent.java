package com.facebook.UserService.dto;

import java.io.Serializable;

public class PageIndexEvent implements Serializable {
    private String id;
    private String name;
    private String category;
    private String avatarUrl;
    private Boolean delete;

    public PageIndexEvent() {
    }

    public PageIndexEvent(String id, String name, String category, String avatarUrl, Boolean delete) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.avatarUrl = avatarUrl;
        this.delete = delete;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String id;
        private String name;
        private String category;
        private String avatarUrl;
        private Boolean delete;

        public Builder id(String id) {
            this.id = id;
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

        public Builder avatarUrl(String avatarUrl) {
            this.avatarUrl = avatarUrl;
            return this;
        }

        public Builder delete(Boolean delete) {
            this.delete = delete;
            return this;
        }

        public PageIndexEvent build() {
            return new PageIndexEvent(id, name, category, avatarUrl, delete);
        }
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public Boolean getDelete() {
        return delete;
    }

    public void setDelete(Boolean delete) {
        this.delete = delete;
    }
}
