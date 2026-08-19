package com.facebook.UserService.client;

import com.facebook.socialgraph.grpc.UserRelation;

import java.util.List;
import java.util.UUID;

public interface SocialGraphClient {
    List<UserRelation> getRelations(UUID userId, List<UUID> targetUserIds);
}
