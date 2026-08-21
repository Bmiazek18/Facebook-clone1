package com.facebook.GroupsService.rule;

import com.facebook.user.grpc.UserDto;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class RuleContext {
    String groupId;
    String userId;
    UserDto userDetails;
}
