package com.facebook.UserService.service;

import com.facebook.user.grpc.UserActiveStatusMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class UserActiveService {

    private static final String REDIS_PREFIX = "user:active:";
    private final StringRedisTemplate redisTemplate;

    @Autowired
    public UserActiveService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void setUserActive(UUID userId) {
        if (userId == null) {
            return;
        }
        String key = REDIS_PREFIX + userId;
        String val = String.valueOf(System.currentTimeMillis());
        redisTemplate.opsForValue().set(key, val, 20, TimeUnit.MINUTES);
    }

    public UserActiveStatusMessage getActiveStatus(UUID userId) {
        if (userId == null) {
            return UserActiveStatusMessage.newBuilder()
                    .setActive(false)
                    .setLastActiveText("offline")
                    .build();
        }

        String key = REDIS_PREFIX + userId;
        String val = redisTemplate.opsForValue().get(key);
        return mapToProto(userId, val);
    }

    public List<UserActiveStatusMessage> getActiveStatuses(List<UUID> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return Collections.emptyList();
        }

        List<String> keys = userIds.stream()
                .map(id -> REDIS_PREFIX + id)
                .collect(Collectors.toList());

        List<String> values = redisTemplate.opsForValue().multiGet(keys);
        if (values == null) {
            return Collections.emptyList();
        }

        List<UserActiveStatusMessage> results = new ArrayList<>();
        for (int i = 0; i < userIds.size(); i++) {
            UUID userId = userIds.get(i);
            String val = i < values.size() ? values.get(i) : null;
            if (val != null) {
                results.add(mapToProto(userId, val));
            }
        }

        return results;
    }

    private UserActiveStatusMessage mapToProto(UUID userId, String val) {
        if (val == null) {
            return UserActiveStatusMessage.newBuilder()
                    .setUserId(String.valueOf(userId))
                    .setActive(false)
                    .setLastActiveText("offline")
                    .build();
        }

        try {
            long lastActive = Long.parseLong(val);
            long diffMs = System.currentTimeMillis() - lastActive;
            long diffMins = diffMs / 60000;

            boolean active = diffMins < 5;
            String text;
            if (diffMins < 5) {
                text = "aktywny";
            } else if (diffMins < 10) {
                text = "5 min temu";
            } else if (diffMins < 15) {
                text = "10 min temu";
            } else if (diffMins <= 20) {
                text = "15 min temu";
            } else {
                text = "offline";
            }

            return UserActiveStatusMessage.newBuilder()
                    .setUserId(String.valueOf(userId))
                    .setActive(active)
                    .setLastActiveText(text)
                    .setLastActiveTimestamp(lastActive)
                    .build();
        } catch (NumberFormatException e) {
            return UserActiveStatusMessage.newBuilder()
                    .setUserId(String.valueOf(userId))
                    .setActive(false)
                    .setLastActiveText("offline")
                    .build();
        }
    }
}
