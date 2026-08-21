package com.facebook.GroupsService.entity;

public enum GroupRole {
    ADMIN,
    MODERATOR,
    MEMBER,
    PENDING;

    public static GroupRole fromString(String role) {
        if (role == null || role.trim().isEmpty()) {
            return MEMBER;
        }
        try {
            return GroupRole.valueOf(role.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return MEMBER;
        }
    }
}
