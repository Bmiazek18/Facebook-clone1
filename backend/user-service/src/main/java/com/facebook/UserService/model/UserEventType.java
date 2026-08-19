package com.facebook.UserService.model;

public enum UserEventType {
    USER_CREATED("user.created"),
    USER_UPDATED("user.updated");

    private final String routingKey;

    UserEventType(String routingKey) {
        this.routingKey = routingKey;
    }

    public String getRoutingKey() {
        return routingKey;
    }
}
