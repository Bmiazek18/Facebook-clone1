package com.facebook.GroupsService.grpc;

import com.facebook.GroupsService.grpc.handler.GroupManagementGrpcHandler;
import com.facebook.GroupsService.grpc.handler.GroupMembershipGrpcHandler;
import com.facebook.groups.grpc.*;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;

@GrpcService
@RequiredArgsConstructor
@Slf4j
public class GroupsGrpcServiceImpl extends GroupsGrpcServiceGrpc.GroupsGrpcServiceImplBase {

    private final GroupManagementGrpcHandler groupManagementGrpcHandler;
    private final GroupMembershipGrpcHandler groupMembershipGrpcHandler;

    // --- GROUP MANAGEMENT ---
    @Override
    public void createGroup(CreateGroupRequest request, StreamObserver<CreateGroupResponse> responseObserver) {
        groupManagementGrpcHandler.createGroup(request, responseObserver);
    }

    @Override
    public void getGroupById(GetGroupByIdRequest request, StreamObserver<GetGroupByIdResponse> responseObserver) {
        groupManagementGrpcHandler.getGroupById(request, responseObserver);
    }

    @Override
    public void getGroups(GetGroupsRequest request, StreamObserver<GetGroupsResponse> responseObserver) {
        groupManagementGrpcHandler.getGroups(request, responseObserver);
    }

    @Override
    public void getUserGroups(GetUserGroupsRequest request, StreamObserver<GetUserGroupsResponse> responseObserver) {
        groupManagementGrpcHandler.getUserGroups(request, responseObserver);
    }

    @Override
    public void getGroupRules(GetGroupRulesRequest request, StreamObserver<GetGroupRulesResponse> responseObserver) {
        groupManagementGrpcHandler.getGroupRules(request, responseObserver);
    }

    @Override
    public void createGroupRule(CreateGroupRuleRequest request, StreamObserver<CreateGroupRuleResponse> responseObserver) {
        groupManagementGrpcHandler.createGroupRule(request, responseObserver);
    }

    @Override
    public void updateGroupRulesOrder(UpdateGroupRulesOrderRequest request, StreamObserver<UpdateGroupRulesOrderResponse> responseObserver) {
        groupManagementGrpcHandler.updateGroupRulesOrder(request, responseObserver);
    }

    @Override
    public void deleteGroupRule(DeleteGroupRuleRequest request, StreamObserver<DeleteGroupRuleResponse> responseObserver) {
        groupManagementGrpcHandler.deleteGroupRule(request, responseObserver);
    }

    @Override
    public void getGroupActivityLogs(GetGroupActivityLogsRequest request, StreamObserver<GetGroupActivityLogsResponse> responseObserver) {
        groupManagementGrpcHandler.getGroupActivityLogs(request, responseObserver);
    }

    @Override
    public void logGroupActivity(LogGroupActivityRequest request, StreamObserver<LogGroupActivityResponse> responseObserver) {
        groupManagementGrpcHandler.logGroupActivity(request, responseObserver);
    }

    // --- GROUP MEMBERSHIP ---
    @Override
    public void joinGroup(JoinGroupRequest request, StreamObserver<JoinGroupResponse> responseObserver) {
        groupMembershipGrpcHandler.joinGroup(request, responseObserver);
    }

    @Override
    public void leaveGroup(LeaveGroupRequest request, StreamObserver<LeaveGroupResponse> responseObserver) {
        groupMembershipGrpcHandler.leaveGroup(request, responseObserver);
    }

    @Override
    public void getGroupMembers(GetGroupMembersRequest request, StreamObserver<GetGroupMembersResponse> responseObserver) {
        groupMembershipGrpcHandler.getGroupMembers(request, responseObserver);
    }

    @Override
    public void getGroupMembership(GetGroupMembershipRequest request, StreamObserver<GetGroupMembershipResponse> responseObserver) {
        groupMembershipGrpcHandler.getGroupMembership(request, responseObserver);
    }

    @Override
    public void approveGroupRequest(ApproveGroupRequestMsg request, StreamObserver<ApproveGroupResponseMsg> responseObserver) {
        groupMembershipGrpcHandler.approveGroupRequest(request, responseObserver);
    }

    @Override
    public void rejectGroupRequest(RejectGroupRequestMsg request, StreamObserver<RejectGroupResponseMsg> responseObserver) {
        groupMembershipGrpcHandler.rejectGroupRequest(request, responseObserver);
    }

    @Override
    public void getPendingRequests(GetPendingRequestsRequest request, StreamObserver<GetPendingRequestsResponse> responseObserver) {
        groupMembershipGrpcHandler.getPendingRequests(request, responseObserver);
    }

    @Override
    public void removeGroupMember(RemoveGroupMemberRequest request, StreamObserver<RemoveGroupMemberResponse> responseObserver) {
        groupMembershipGrpcHandler.removeGroupMember(request, responseObserver);
    }

    @Override
    public void updateMemberRole(UpdateMemberRoleRequest request, StreamObserver<UpdateMemberRoleResponse> responseObserver) {
        groupMembershipGrpcHandler.updateMemberRole(request, responseObserver);
    }
}
