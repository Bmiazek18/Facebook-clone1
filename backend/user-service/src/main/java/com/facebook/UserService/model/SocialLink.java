package com.facebook.UserService.model;

import jakarta.persistence.Embeddable;

@Embeddable
public class SocialLink {
    private String name;
    private String url;

    public SocialLink() {
    }

    public SocialLink(String name, String url) {
        this.name = name;
        this.url = url;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String name;
        private String url;

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder url(String url) {
            this.url = url;
            return this;
        }

        public SocialLink build() {
            return new SocialLink(name, url);
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
