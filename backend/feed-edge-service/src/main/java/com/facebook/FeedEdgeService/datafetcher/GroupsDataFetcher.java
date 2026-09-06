package com.facebook.FeedEdgeService.datafetcher;

import com.facebook.groups.grpc.*;
import com.facebook.FeedEdgeService.codegen.types.Group;
import com.facebook.FeedEdgeService.codegen.types.CreateGroupInput;
import com.facebook.FeedEdgeService.codegen.types.GroupMember;
import com.netflix.graphql.dgs.*;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.List;
import java.util.stream.Collectors;

@DgsComponent
public class GroupsDataFetcher {

    private static final Logger log = LoggerFactory.getLogger(GroupsDataFetcher.class);

    @GrpcClient("groups-service")
    private GroupsGrpcServiceGrpc.GroupsGrpcServiceBlockingStub groupsGrpcStub;

    @GrpcClient("socialgraph-service")
    private com.facebook.socialgraph.grpc.SocialGraphGrpcServiceGrpc.SocialGraphGrpcServiceBlockingStub socialGraphGrpcStub;

    @GrpcClient("feed-service")
    private com.facebook.feed.grpc.FeedGrpcServiceGrpc.FeedGrpcServiceBlockingStub feedGrpcStub;

    @DgsQuery
    public List<Group> getGroups(@InputArgument Integer limit, @InputArgument Integer offset) {
        log.info("Edge: Fetching all groups via gRPC");
        try {
            var response = groupsGrpcStub.getGroups(GetGroupsRequest.newBuilder()
                    .setLimit(limit != null ? limit : 10)
                    .setOffset(offset != null ? offset : 0)
                    .build());
            return response.getGroupsList().stream()
                    .map(this::mapToGroup)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Failed to fetch groups list", e);
            throw new RuntimeException("Groups service unavailable: " + e.getMessage());
        }
    }

    @DgsQuery
    public Group getGroupById(@InputArgument String id) {
        log.info("Edge: Fetching group by ID: {}", id);
        try {
            var response = groupsGrpcStub.getGroupById(GetGroupByIdRequest.newBuilder()
                    .setId(id)
                    .build());
            return mapToGroup(response.getGroup());
        } catch (Exception e) {
            log.error("Failed to fetch group by ID " + id, e);
            throw new RuntimeException("Groups service unavailable: " + e.getMessage());
        }
    }

    @DgsQuery
    public List<Group> getUserGroups(@InputArgument String userId) {
        log.info("Edge: Fetching groups for user: {}", userId);
        try {
            var response = groupsGrpcStub.getUserGroups(GetUserGroupsRequest.newBuilder()
                    .setUserId(userId)
                    .build());
            return response.getGroupsList().stream()
                    .map(this::mapToGroup)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Failed to fetch user groups", e);
            throw new RuntimeException("Groups service unavailable: " + e.getMessage());
        }
    }

    @DgsMutation
    public Group createGroup(
            @InputArgument CreateGroupInput input,
            @RequestHeader(name = "X-User-Id", required = false) String xUserId) {
        String effectiveCreatorId = (xUserId != null && !xUserId.isBlank()) ? xUserId : input.getCreatorId();
        log.info("Edge: Creating group: {} for creator: {}", input.getName(), effectiveCreatorId);
        try {
            var response = groupsGrpcStub.createGroup(CreateGroupRequest.newBuilder()
                    .setName(input.getName())
                    .setDescription(input.getDescription() != null ? input.getDescription() : "")
                    .setPrivacy(input.getPrivacy())
                    .setImage(input.getImage() != null ? input.getImage() : "")
                    .setCreatorId(effectiveCreatorId)
                    .build());
            return mapToGroup(response.getGroup());
        } catch (Exception e) {
            log.error("Failed to create group", e);
            throw new RuntimeException("Groups service unavailable: " + e.getMessage());
        }
    }

    @DgsMutation
    public Boolean joinGroup(
            @InputArgument String groupId,
            @InputArgument String userId,
            @RequestHeader(name = "X-User-Id", required = false) String xUserId) {
        String effectiveUserId = (xUserId != null && !xUserId.isBlank()) ? xUserId : userId;
        log.info("Edge: User {} joining group {}", effectiveUserId, groupId);
        try {
            var response = groupsGrpcStub.joinGroup(JoinGroupRequest.newBuilder()
                    .setGroupId(groupId)
                    .setUserId(effectiveUserId)
                    .build());
            return response.getSuccess();
        } catch (Exception e) {
            log.error("Failed to join group", e);
            return false;
        }
    }

    @DgsMutation
    public Boolean leaveGroup(
            @InputArgument String groupId,
            @InputArgument String userId,
            @RequestHeader(name = "X-User-Id", required = false) String xUserId) {
        String effectiveUserId = (xUserId != null && !xUserId.isBlank()) ? xUserId : userId;
        log.info("Edge: User {} leaving group {}", effectiveUserId, groupId);
        try {
            var response = groupsGrpcStub.leaveGroup(LeaveGroupRequest.newBuilder()
                    .setGroupId(groupId)
                    .setUserId(effectiveUserId)
                    .build());
            return response.getSuccess();
        } catch (Exception e) {
            log.error("Failed to leave group", e);
            return false;
        }
    }

    @DgsQuery
    public String getGroupMembership(@InputArgument String groupId, @InputArgument String userId) {
        log.info("Edge: Fetching membership for user {} in group {}", userId, groupId);
        try {
            var response = groupsGrpcStub.getGroupMembership(GetGroupMembershipRequest.newBuilder()
                    .setGroupId(groupId)
                    .setUserId(userId)
                    .build());
            return response.getRole();
        } catch (Exception e) {
            log.error("Failed to fetch group membership", e);
            return "";
        }
    }

    @DgsQuery
    public List<String> getPendingRequests(@InputArgument String groupId) {
        log.info("Edge: Fetching pending requests for group {}", groupId);
        try {
            var response = groupsGrpcStub.getPendingRequests(GetPendingRequestsRequest.newBuilder()
                    .setGroupId(groupId)
                    .build());
            return response.getUserIdsList();
        } catch (Exception e) {
            log.error("Failed to fetch pending requests", e);
            return List.of();
        }
    }

    @DgsMutation
    public Boolean approveGroupRequest(
            @InputArgument String groupId,
            @InputArgument String userId,
            @InputArgument(name = "adminId") String adminIdArg,
            @RequestHeader(name = "X-User-Id", required = false) String xUserId) {
        String effectiveAdminId = (xUserId != null && !xUserId.isEmpty()) ? xUserId : adminIdArg;
        log.info("Edge: Approving join request for user {} in group {} by admin {}", userId, groupId, effectiveAdminId);
        try {
            var response = groupsGrpcStub.approveGroupRequest(ApproveGroupRequestMsg.newBuilder()
                    .setGroupId(groupId)
                    .setUserId(userId)
                    .setAdminId(effectiveAdminId != null ? effectiveAdminId : "")
                    .build());
            return response.getSuccess();
        } catch (Exception e) {
            log.error("Failed to approve group request", e);
            return false;
        }
    }

    @DgsMutation
    public Boolean rejectGroupRequest(
            @InputArgument String groupId,
            @InputArgument String userId,
            @InputArgument(name = "adminId") String adminIdArg,
            @RequestHeader(name = "X-User-Id", required = false) String xUserId) {
        String effectiveAdminId = (xUserId != null && !xUserId.isEmpty()) ? xUserId : adminIdArg;
        log.info("Edge: Rejecting join request for user {} in group {} by admin {}", userId, groupId, effectiveAdminId);
        try {
            var response = groupsGrpcStub.rejectGroupRequest(RejectGroupRequestMsg.newBuilder()
                    .setGroupId(groupId)
                    .setUserId(userId)
                    .setAdminId(effectiveAdminId != null ? effectiveAdminId : "")
                    .build());
            return response.getSuccess();
        } catch (Exception e) {
            log.error("Failed to reject group request", e);
            return false;
        }
    }

    @DgsMutation
    public Boolean removeGroupMember(
            @InputArgument String groupId,
            @InputArgument String userId,
            @InputArgument(name = "adminId") String adminIdArg,
            @RequestHeader(name = "X-User-Id", required = false) String xUserId) {
        String effectiveAdminId = (xUserId != null && !xUserId.isEmpty()) ? xUserId : adminIdArg;
        log.info("Edge: Admin {} removing user {} from group {}", effectiveAdminId, userId, groupId);
        try {
            var response = groupsGrpcStub.removeGroupMember(RemoveGroupMemberRequest.newBuilder()
                    .setGroupId(groupId)
                    .setUserId(userId)
                    .setAdminId(effectiveAdminId != null ? effectiveAdminId : "")
                    .build());
            return response.getSuccess();
        } catch (Exception e) {
            log.error("Failed to remove group member", e);
            return false;
        }
    }

    @DgsMutation
    public Boolean updateGroupMemberRole(
            @InputArgument String groupId,
            @InputArgument String userId,
            @InputArgument com.facebook.FeedEdgeService.codegen.types.GroupRole role,
            @InputArgument(name = "adminId") String adminIdArg,
            @RequestHeader(name = "X-User-Id", required = false) String xUserId) {
        String effectiveAdminId = (xUserId != null && !xUserId.isEmpty()) ? xUserId : adminIdArg;
        String roleStr = role != null ? role.name() : "MEMBER";
        log.info("Edge: Admin {} updating role for user {} in group {} to {}", effectiveAdminId, userId, groupId, roleStr);
        try {
            var response = groupsGrpcStub.updateMemberRole(UpdateMemberRoleRequest.newBuilder()
                    .setGroupId(groupId)
                    .setUserId(userId)
                    .setNewRole(roleStr)
                    .setAdminId(effectiveAdminId != null ? effectiveAdminId : "")
                    .build());
            return response.getSuccess();
        } catch (Exception e) {
            log.error("Failed to update member role", e);
            return false;
        }
    }

    @DgsQuery
    public List<GroupMember> getGroupMembers(
            @InputArgument String groupId,
            @RequestHeader(name = "X-User-Id", required = false) String xUserId) {
        log.info("Edge: Fetching members for group {} with xUserId from Kong: {}", groupId, xUserId);
        try {
            var response = groupsGrpcStub.getGroupMembers(GetGroupMembersRequest.newBuilder()
                    .setGroupId(groupId)
                    .build());
            
            List<com.facebook.groups.grpc.GroupMemberDto> grpcMembers = response.getMembersList();
            
            java.util.Map<String, Boolean> relationsMap = new java.util.HashMap<>();
            if (xUserId != null && !xUserId.isEmpty() && !grpcMembers.isEmpty()) {
                try {
                    List<String> targetIds = grpcMembers.stream()
                            .map(com.facebook.groups.grpc.GroupMemberDto::getUserId)
                            .collect(Collectors.toList());
                    var relationsResponse = socialGraphGrpcStub.getRelations(
                            com.facebook.socialgraph.grpc.GetRelationsRequest.newBuilder()
                                     .setUserId(xUserId)
                                     .addAllTargetUserIds(targetIds)
                                     .build());
                    for (var rel : relationsResponse.getRelationsList()) {
                        relationsMap.put(rel.getTargetUserId(), rel.getFriend());
                    }
                } catch (Exception e) {
                    log.error("Failed to fetch social graph relations for user " + xUserId, e);
                }
            }

            return grpcMembers.stream()
                    .map(m -> {
                        GroupMember member = new GroupMember();
                        member.setUserId(m.getUserId());
                        try {
                            member.setRole(com.facebook.FeedEdgeService.codegen.types.GroupRole.valueOf(m.getRole().toUpperCase()));
                        } catch (Exception ex) {
                            member.setRole(com.facebook.FeedEdgeService.codegen.types.GroupRole.MEMBER);
                        }
                        member.setJoinedAt(m.getJoinedAt());
                        member.setIsFriend(relationsMap.getOrDefault(m.getUserId(), false));
                        return member;
                    })
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Failed to fetch group members", e);
            return List.of();
        }
    }

    @DgsQuery
    public com.facebook.FeedEdgeService.codegen.types.GroupOverview getGroupOverview(
            @InputArgument String groupId,
            @RequestHeader(name = "X-User-Id", required = false) String xUserId) {
        log.info("Edge: Fetching overview for group {} by user {}", groupId, xUserId);

        if (xUserId == null || xUserId.isEmpty()) {
            throw new RuntimeException("Unauthorized: User not logged in");
        }

        String role = getGroupMembership(groupId, xUserId);
        if (!"ADMIN".equalsIgnoreCase(role)) {
            throw new RuntimeException("Access Denied: Only group administrators can access this data");
        }

        int pendingRequestsCount = 0;
        try {
            var pendingResponse = groupsGrpcStub.getPendingRequests(GetPendingRequestsRequest.newBuilder()
                    .setGroupId(groupId)
                    .build());
            pendingRequestsCount = pendingResponse.getUserIdsCount();
        } catch (Exception e) {
            log.error("Failed to fetch pending requests count", e);
        }

        int postsCount = 0;
        int commentsCount = 0;
        int reactionsCount = 0;
        try {
            var feedResponse = feedGrpcStub.getGroupFeed(com.facebook.feed.grpc.GetGroupFeedRequest.newBuilder()
                    .setGroupId(groupId)
                    .setLimit(100)
                    .setOffset(0)
                    .build());
            var postsList = feedResponse.getPostsList();
            postsCount = postsList.size();
            for (var p : postsList) {
                commentsCount += p.getCommentCount();
                for (var r : p.getReactionsList()) {
                    reactionsCount += r.getUserIdsCount();
                }
            }
        } catch (Exception e) {
            log.error("Failed to fetch group feed stats", e);
        }

        com.facebook.FeedEdgeService.codegen.types.GroupOverview overview = new com.facebook.FeedEdgeService.codegen.types.GroupOverview();
        overview.setGroupId(groupId);
        overview.setReportedItemsCount(0);
        overview.setModerationAlertsCount(0);
        overview.setPendingPostsCount(0);
        overview.setPendingRequestsCount(pendingRequestsCount);
        overview.setGroupStatusViolationCount(0);

        overview.setPostsCount7Days(postsCount);
        overview.setCommentsCount7Days(commentsCount);
        overview.setReactionsCount7Days(reactionsCount);

        overview.setPostsTrend(postsCount > 0 ? "+" + (postsCount * 10) + "%" : "0%");
        overview.setCommentsTrend(commentsCount > 0 ? "+" + (commentsCount * 10) + "%" : "0%");
        overview.setReactionsTrend(reactionsCount > 0 ? "+" + (reactionsCount * 10) + "%" : "0%");

        overview.setActiveMembersChart(List.of(0, 0, 0, 0, 0, 0, postsCount > 0 ? postsCount : 1));
        overview.setChartCategories(List.of("6 sie", "7 sie", "8 sie", "9 sie", "10 sie", "11 sie", "12 sie"));

        return overview;
    }

    @DgsQuery
    public List<com.facebook.FeedEdgeService.codegen.types.GroupRule> getGroupRules(@InputArgument String groupId) {
        log.info("Edge: Fetching group rules for group: {}", groupId);
        try {
            var response = groupsGrpcStub.getGroupRules(GetGroupRulesRequest.newBuilder()
                    .setGroupId(groupId)
                    .build());
            return response.getRulesList().stream()
                    .map(this::mapToGroupRule)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Failed to fetch group rules", e);
            throw new RuntimeException("Groups service unavailable: " + e.getMessage());
        }
    }

    @DgsData(parentType = "Group", field = "rules")
    public List<com.facebook.FeedEdgeService.codegen.types.GroupRule> getGroupRulesForGroup(DgsDataFetchingEnvironment dfe) {
        Group group = dfe.getSource();
        return getGroupRules(group.getId());
    }

    @DgsMutation
    public com.facebook.FeedEdgeService.codegen.types.GroupRule createGroupRule(
            @InputArgument String groupId, 
            @InputArgument String title, 
            @InputArgument String description) {
        log.info("Edge: Creating group rule for group: {}", groupId);
        try {
            var response = groupsGrpcStub.createGroupRule(CreateGroupRuleRequest.newBuilder()
                    .setGroupId(groupId)
                    .setTitle(title)
                    .setDescription(description)
                    .build());
            return mapToGroupRule(response.getRule());
        } catch (Exception e) {
            log.error("Failed to create group rule", e);
            throw new RuntimeException("Groups service unavailable: " + e.getMessage());
        }
    }

    @DgsMutation
    public Boolean updateGroupRulesOrder(@InputArgument String groupId, @InputArgument List<String> ruleIds) {
        log.info("Edge: Updating group rules order for group: {}", groupId);
        try {
            var response = groupsGrpcStub.updateGroupRulesOrder(UpdateGroupRulesOrderRequest.newBuilder()
                    .setGroupId(groupId)
                    .addAllRuleIds(ruleIds)
                    .build());
            return response.getSuccess();
        } catch (Exception e) {
            log.error("Failed to update group rules order", e);
            throw new RuntimeException("Groups service unavailable: " + e.getMessage());
        }
    }

    @DgsMutation
    public Boolean deleteGroupRule(@InputArgument String ruleId) {
        log.info("Edge: Deleting group rule: {}", ruleId);
        try {
            var response = groupsGrpcStub.deleteGroupRule(DeleteGroupRuleRequest.newBuilder()
                    .setRuleId(ruleId)
                    .build());
            return response.getSuccess();
        } catch (Exception e) {
            log.error("Failed to delete group rule", e);
            throw new RuntimeException("Groups service unavailable: " + e.getMessage());
        }
    }

    @DgsQuery
    public List<com.facebook.FeedEdgeService.codegen.types.GroupActivityLog> getGroupActivityLogs(@InputArgument String groupId) {
        log.info("Edge: Fetching activity logs for group: {}", groupId);
        try {
            var response = groupsGrpcStub.getGroupActivityLogs(GetGroupActivityLogsRequest.newBuilder()
                    .setGroupId(groupId)
                    .build());
            return response.getLogsList().stream()
                    .map(this::mapToGroupActivityLog)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Failed to fetch activity logs", e);
            throw new RuntimeException("Groups service unavailable: " + e.getMessage());
        }
    }

    @DgsMutation
    public com.facebook.FeedEdgeService.codegen.types.GroupActivityLog logGroupActivity(
            @InputArgument String groupId,
            @InputArgument String text,
            @InputArgument String note,
            @InputArgument String actorId,
            @InputArgument String actorName,
            @RequestHeader(name = "X-User-Id", required = false) String xUserId) {
        String effectiveActorId = (xUserId != null && !xUserId.isBlank()) ? xUserId : actorId;
        log.info("Edge: Logging group activity for group: {} by {}", groupId, actorName);
        try {
            var response = groupsGrpcStub.logGroupActivity(LogGroupActivityRequest.newBuilder()
                    .setGroupId(groupId)
                    .setText(text)
                    .setNote(note != null ? note : "")
                    .setActorId(effectiveActorId)
                    .setActorName(actorName)
                    .build());
            return mapToGroupActivityLog(response.getLog());
        } catch (Exception e) {
            log.error("Failed to log activity", e);
            throw new RuntimeException("Groups service unavailable: " + e.getMessage());
        }
    }

    private com.facebook.FeedEdgeService.codegen.types.GroupActivityLog mapToGroupActivityLog(GroupActivityLogDto dto) {
        com.facebook.FeedEdgeService.codegen.types.GroupActivityLog l = new com.facebook.FeedEdgeService.codegen.types.GroupActivityLog();
        l.setId(dto.getId());
        l.setGroupId(dto.getGroupId());
        l.setActorId(dto.getActorId());
        l.setActorName(dto.getActorName());
        l.setText(dto.getText());
        l.setNote(dto.getNote());
        l.setTime(dto.getTime());
        l.setDate(dto.getDate());
        return l;
    }

    private com.facebook.FeedEdgeService.codegen.types.GroupRule mapToGroupRule(GroupRuleDto dto) {
        com.facebook.FeedEdgeService.codegen.types.GroupRule rule = new com.facebook.FeedEdgeService.codegen.types.GroupRule();
        rule.setId(dto.getId());
        rule.setTitle(dto.getTitle());
        rule.setDescription(dto.getDescription());
        rule.setOrderIndex(dto.getOrderIndex());
        return rule;
    }

    private Group mapToGroup(GroupDto dto) {
        Group group = new Group();
        group.setId(dto.getId());
        group.setName(dto.getName());
        group.setDescription(dto.getDescription());
        group.setPrivacy(dto.getPrivacy());
        group.setImage(dto.getImage());
        group.setMembersCount(dto.getMembersCount());
        group.setLastActive(dto.getLastActive());
        group.setNewPostsToday(dto.getNewPostsToday());
        group.setNewPostsMonth(dto.getNewPostsMonth());
        group.setNewMembersWeek(dto.getNewMembersWeek());
        group.setCreatedAge(dto.getCreatedAge());
        return group;
    }
}
