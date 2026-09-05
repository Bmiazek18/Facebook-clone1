package com.facebook.GroupsService.rule;

import com.facebook.user.grpc.UserDto;

public class RuleContext {
    private final String groupId;
    private final String userId;
    private final UserDto userDetails;

    public RuleContext(String groupId, String userId, UserDto userDetails) {
        this.groupId = groupId;
        this.userId = userId;
        this.userDetails = userDetails;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String groupId;
        private String userId;
        private UserDto userDetails;

        public Builder groupId(String groupId) {
            this.groupId = groupId;
            return this;
        }

        public Builder userId(String userId) {
            this.userId = userId;
            return this;
        }

        public Builder userDetails(UserDto userDetails) {
            this.userDetails = userDetails;
            return this;
        }

        public RuleContext build() {
            return new RuleContext(groupId, userId, userDetails);
        }
    }

    public String getGroupId() {
        return groupId;
    }

    public String getUserId() {
        return userId;
    }

    public UserDto getUserDetails() {
        return userDetails;
    }
}
