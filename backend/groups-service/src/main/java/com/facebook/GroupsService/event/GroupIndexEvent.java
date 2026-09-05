package com.facebook.GroupsService.event;

import java.io.Serializable;

public class GroupIndexEvent implements Serializable {
    private String id;
    private String name;
    private String image;
    private Integer newPostsCount;
    private Boolean delete; // true to remove from Meilisearch

    public GroupIndexEvent() {
    }

    public GroupIndexEvent(String id, String name, String image, Integer newPostsCount, Boolean delete) {
        this.id = id;
        this.name = name;
        this.image = image;
        this.newPostsCount = newPostsCount;
        this.delete = delete;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String id;
        private String name;
        private String image;
        private Integer newPostsCount;
        private Boolean delete;

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder image(String image) {
            this.image = image;
            return this;
        }

        public Builder newPostsCount(Integer newPostsCount) {
            this.newPostsCount = newPostsCount;
            return this;
        }

        public Builder delete(Boolean delete) {
            this.delete = delete;
            return this;
        }

        public GroupIndexEvent build() {
            return new GroupIndexEvent(id, name, image, newPostsCount, delete);
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

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public Integer getNewPostsCount() {
        return newPostsCount;
    }

    public void setNewPostsCount(Integer newPostsCount) {
        this.newPostsCount = newPostsCount;
    }

    public Boolean getDelete() {
        return delete;
    }

    public void setDelete(Boolean delete) {
        this.delete = delete;
    }
}
