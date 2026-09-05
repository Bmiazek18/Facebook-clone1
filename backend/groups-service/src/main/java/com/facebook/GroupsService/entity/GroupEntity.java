package com.facebook.GroupsService.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "groups")
public class GroupEntity {
    @Id
    private String id;

    private String name;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    private String privacy; // public, private
    private String image;
    private String lastActive;

    private Integer membersCount = 0;
    private Integer newPostsToday = 0;
    private Integer newPostsMonth = 0;
    private String newMembersWeek = "";
    private String createdAge = "";

    public GroupEntity() {
    }

    public GroupEntity(String id, String name, String description, String privacy, String image, String lastActive,
                       Integer membersCount, Integer newPostsToday, Integer newPostsMonth, String newMembersWeek, String createdAge) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.privacy = privacy;
        this.image = image;
        this.lastActive = lastActive;
        this.membersCount = membersCount != null ? membersCount : 0;
        this.newPostsToday = newPostsToday != null ? newPostsToday : 0;
        this.newPostsMonth = newPostsMonth != null ? newPostsMonth : 0;
        this.newMembersWeek = newMembersWeek != null ? newMembersWeek : "";
        this.createdAge = createdAge != null ? createdAge : "";
    }

    public static GroupEntityBuilder builder() {
        return new GroupEntityBuilder();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getPrivacy() { return privacy; }
    public void setPrivacy(String privacy) { this.privacy = privacy; }

    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }

    public String getLastActive() { return lastActive; }
    public void setLastActive(String lastActive) { this.lastActive = lastActive; }

    public Integer getMembersCount() { return membersCount; }
    public void setMembersCount(Integer membersCount) { this.membersCount = membersCount; }

    public Integer getNewPostsToday() { return newPostsToday; }
    public void setNewPostsToday(Integer newPostsToday) { this.newPostsToday = newPostsToday; }

    public Integer getNewPostsMonth() { return newPostsMonth; }
    public void setNewPostsMonth(Integer newPostsMonth) { this.newPostsMonth = newPostsMonth; }

    public String getNewMembersWeek() { return newMembersWeek; }
    public void setNewMembersWeek(String newMembersWeek) { this.newMembersWeek = newMembersWeek; }

    public String getCreatedAge() { return createdAge; }
    public void setCreatedAge(String createdAge) { this.createdAge = createdAge; }

    public static class GroupEntityBuilder {
        private String id;
        private String name;
        private String description;
        private String privacy;
        private String image;
        private String lastActive;
        private Integer membersCount = 0;
        private Integer newPostsToday = 0;
        private Integer newPostsMonth = 0;
        private String newMembersWeek = "";
        private String createdAge = "";

        public GroupEntityBuilder id(String id) { this.id = id; return this; }
        public GroupEntityBuilder name(String name) { this.name = name; return this; }
        public GroupEntityBuilder description(String description) { this.description = description; return this; }
        public GroupEntityBuilder privacy(String privacy) { this.privacy = privacy; return this; }
        public GroupEntityBuilder image(String image) { this.image = image; return this; }
        public GroupEntityBuilder lastActive(String lastActive) { this.lastActive = lastActive; return this; }
        public GroupEntityBuilder membersCount(Integer membersCount) { this.membersCount = membersCount; return this; }
        public GroupEntityBuilder newPostsToday(Integer newPostsToday) { this.newPostsToday = newPostsToday; return this; }
        public GroupEntityBuilder newPostsMonth(Integer newPostsMonth) { this.newPostsMonth = newPostsMonth; return this; }
        public GroupEntityBuilder newMembersWeek(String newMembersWeek) { this.newMembersWeek = newMembersWeek; return this; }
        public GroupEntityBuilder createdAge(String createdAge) { this.createdAge = createdAge; return this; }

        public GroupEntity build() {
            return new GroupEntity(id, name, description, privacy, image, lastActive, membersCount, newPostsToday, newPostsMonth, newMembersWeek, createdAge);
        }
    }
}
