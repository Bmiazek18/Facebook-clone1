package com.facebook.GroupsService.grpc.handler;

import com.facebook.GroupsService.entity.GroupEntity;
import com.facebook.GroupsService.entity.GroupMemberEntity;
import com.facebook.GroupsService.entity.GroupRole;
import com.facebook.GroupsService.event.JoinRequestCreatedEvent;
import com.facebook.GroupsService.repository.GroupMemberRepository;
import com.facebook.GroupsService.repository.GroupRepository;
import com.facebook.groups.grpc.*;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class GroupMembershipGrpcHandler {

    private final GroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public void joinGroup(JoinGroupRequest request, StreamObserver<JoinGroupResponse> responseObserver) {
        log.info("gRPC: User {} joining group {}", request.getUserId(), request.getGroupId());
        try {
            Optional<GroupEntity> groupOpt = groupRepository.findById(request.getGroupId());
            if (groupOpt.isEmpty()) {
                responseObserver.onError(io.grpc.Status.NOT_FOUND
                        .withDescription("Group not found with ID: " + request.getGroupId())
                        .asRuntimeException());
                return;
            }

            GroupEntity group = groupOpt.get();
            if (!groupMemberRepository.existsByGroupIdAndUserId(request.getGroupId(), request.getUserId())) {
                boolean isPrivate = group.getPrivacy() != null && group.getPrivacy().equalsIgnoreCase("private");
                GroupRole role = isPrivate ? GroupRole.PENDING : GroupRole.MEMBER;

                GroupMemberEntity membership = GroupMemberEntity.builder()
                        .groupId(request.getGroupId())
                        .userId(request.getUserId())
                        .role(role)
                        .build();
                groupMemberRepository.save(membership);

                if (isPrivate) {
                    eventPublisher.publishEvent(new JoinRequestCreatedEvent(
                            request.getGroupId(), request.getUserId()
                    ));
                } else {
                    groupRepository.incrementMembersCount(request.getGroupId());
                }
            }

            responseObserver.onNext(JoinGroupResponse.newBuilder().setSuccess(true).build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("Failed to join group", e);
            responseObserver.onError(io.grpc.Status.INTERNAL
                    .withDescription("Internal error: " + e.getMessage())
                    .asRuntimeException());
        }
    }

    @Transactional
    public void leaveGroup(LeaveGroupRequest request, StreamObserver<LeaveGroupResponse> responseObserver) {
        log.info("gRPC: User {} leaving group {}", request.getUserId(), request.getGroupId());
        try {
            Optional<GroupMemberEntity> membershipOpt = groupMemberRepository.findByGroupIdAndUserId(request.getGroupId(), request.getUserId());
            if (membershipOpt.isPresent()) {
                GroupMemberEntity membership = membershipOpt.get();
                if (membership.getRole() == GroupRole.ADMIN) {
                    List<GroupMemberEntity> allMembers = groupMemberRepository.findAllByGroupId(request.getGroupId());
                    long totalMembers = allMembers.stream().filter(m -> m.getRole() != GroupRole.PENDING).count();
                    long adminCount = allMembers.stream().filter(m -> m.getRole() == GroupRole.ADMIN).count();
                    if (adminCount <= 1 && totalMembers > 1) {
                        responseObserver.onError(io.grpc.Status.FAILED_PRECONDITION
                                .withDescription("Jesteś jedynym administratorem. Mianuj nowego administratora przed opuszczeniem grupy.")
                                .asRuntimeException());
                        return;
                    }
                }
                groupMemberRepository.delete(membership);
                groupRepository.decrementMembersCount(request.getGroupId());
            }

            responseObserver.onNext(LeaveGroupResponse.newBuilder().setSuccess(true).build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("Failed to leave group", e);
            responseObserver.onError(io.grpc.Status.INTERNAL
                    .withDescription("Internal error: " + e.getMessage())
                    .asRuntimeException());
        }
    }

    public void getGroupMembers(GetGroupMembersRequest request, StreamObserver<GetGroupMembersResponse> responseObserver) {
        log.info("gRPC: Fetching members of group {}", request.getGroupId());
        try {
            List<GroupMemberEntity> memberships = groupMemberRepository.findAllByGroupId(request.getGroupId());
            List<String> userIds = memberships.stream()
                    .filter(m -> m.getRole() != GroupRole.PENDING)
                    .map(GroupMemberEntity::getUserId)
                    .collect(Collectors.toList());
            List<GroupMemberDto> memberDtos = memberships.stream()
                    .filter(m -> m.getRole() != GroupRole.PENDING)
                    .map(m -> GroupMemberDto.newBuilder()
                            .setUserId(m.getUserId())
                            .setRole(m.getRole() != null ? m.getRole().name() : GroupRole.MEMBER.name())
                            .setJoinedAt(m.getJoinedAt() != null ? m.getJoinedAt().toString() : "")
                            .build())
                    .collect(Collectors.toList());
            responseObserver.onNext(GetGroupMembersResponse.newBuilder()
                    .addAllUserIds(userIds)
                    .addAllMembers(memberDtos)
                    .build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("Failed to fetch group members", e);
            responseObserver.onError(io.grpc.Status.INTERNAL
                    .withDescription("Internal error: " + e.getMessage())
                    .asRuntimeException());
        }
    }

    public void getGroupMembership(GetGroupMembershipRequest request, StreamObserver<GetGroupMembershipResponse> responseObserver) {
        log.info("gRPC: Fetching membership for user {} in group {}", request.getUserId(), request.getGroupId());
        try {
            Optional<GroupMemberEntity> membershipOpt = groupMemberRepository.findByGroupIdAndUserId(request.getGroupId(), request.getUserId());
            String role = membershipOpt.map(m -> m.getRole() != null ? m.getRole().name() : "").orElse("");
            responseObserver.onNext(GetGroupMembershipResponse.newBuilder().setRole(role).build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("Failed to fetch group membership", e);
            responseObserver.onError(io.grpc.Status.INTERNAL
                    .withDescription("Internal error: " + e.getMessage())
                    .asRuntimeException());
        }
    }

    @Transactional
    public void approveGroupRequest(ApproveGroupRequestMsg request, StreamObserver<ApproveGroupResponseMsg> responseObserver) {
        log.info("gRPC: Approving join request for user {} in group {} by admin {}", request.getUserId(), request.getGroupId(), request.getAdminId());
        try {
            Optional<GroupMemberEntity> adminMembership = groupMemberRepository.findByGroupIdAndUserId(request.getGroupId(), request.getAdminId());
            if (adminMembership.isEmpty() || adminMembership.get().getRole() != GroupRole.ADMIN) {
                responseObserver.onError(io.grpc.Status.PERMISSION_DENIED
                        .withDescription("Only admins can approve membership requests")
                        .asRuntimeException());
                return;
            }

            Optional<GroupMemberEntity> pendingMembership = groupMemberRepository.findByGroupIdAndUserId(request.getGroupId(), request.getUserId());
            if (pendingMembership.isPresent() && pendingMembership.get().getRole() == GroupRole.PENDING) {
                GroupMemberEntity membership = pendingMembership.get();
                membership.setRole(GroupRole.MEMBER);
                groupMemberRepository.save(membership);
                groupRepository.incrementMembersCount(request.getGroupId());
                responseObserver.onNext(ApproveGroupResponseMsg.newBuilder().setSuccess(true).build());
            } else {
                responseObserver.onNext(ApproveGroupResponseMsg.newBuilder().setSuccess(false).build());
            }
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("Failed to approve group request", e);
            responseObserver.onError(io.grpc.Status.INTERNAL
                    .withDescription("Internal error: " + e.getMessage())
                    .asRuntimeException());
        }
    }

    @Transactional
    public void rejectGroupRequest(RejectGroupRequestMsg request, StreamObserver<RejectGroupResponseMsg> responseObserver) {
        log.info("gRPC: Rejecting join request for user {} in group {} by admin {}", request.getUserId(), request.getGroupId(), request.getAdminId());
        try {
            Optional<GroupMemberEntity> adminMembership = groupMemberRepository.findByGroupIdAndUserId(request.getGroupId(), request.getAdminId());
            if (adminMembership.isEmpty() || adminMembership.get().getRole() != GroupRole.ADMIN) {
                responseObserver.onError(io.grpc.Status.PERMISSION_DENIED
                        .withDescription("Only admins can reject membership requests")
                        .asRuntimeException());
                return;
            }

            Optional<GroupMemberEntity> pendingMembership = groupMemberRepository.findByGroupIdAndUserId(request.getGroupId(), request.getUserId());
            if (pendingMembership.isPresent() && pendingMembership.get().getRole() == GroupRole.PENDING) {
                groupMemberRepository.delete(pendingMembership.get());
                responseObserver.onNext(RejectGroupResponseMsg.newBuilder().setSuccess(true).build());
            } else {
                responseObserver.onNext(RejectGroupResponseMsg.newBuilder().setSuccess(false).build());
            }
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("Failed to reject group request", e);
            responseObserver.onError(io.grpc.Status.INTERNAL
                    .withDescription("Internal error: " + e.getMessage())
                    .asRuntimeException());
        }
    }

    public void getPendingRequests(GetPendingRequestsRequest request, StreamObserver<GetPendingRequestsResponse> responseObserver) {
        log.info("gRPC: Fetching pending join requests for group {}", request.getGroupId());
        try {
            List<GroupMemberEntity> memberships = groupMemberRepository.findAllByGroupId(request.getGroupId());
            List<String> pendingUserIds = memberships.stream()
                    .filter(m -> m.getRole() == GroupRole.PENDING)
                    .map(GroupMemberEntity::getUserId)
                    .collect(Collectors.toList());
            responseObserver.onNext(GetPendingRequestsResponse.newBuilder().addAllUserIds(pendingUserIds).build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("Failed to fetch pending requests", e);
            responseObserver.onError(io.grpc.Status.INTERNAL
                    .withDescription("Internal error: " + e.getMessage())
                    .asRuntimeException());
        }
    }

    @Transactional
    public void removeGroupMember(RemoveGroupMemberRequest request, StreamObserver<RemoveGroupMemberResponse> responseObserver) {
        log.info("gRPC: Admin {} removing user {} from group {}", request.getAdminId(), request.getUserId(), request.getGroupId());
        try {
            Optional<GroupMemberEntity> adminMembership = groupMemberRepository.findByGroupIdAndUserId(request.getGroupId(), request.getAdminId());
            if (adminMembership.isEmpty() || adminMembership.get().getRole() != GroupRole.ADMIN) {
                responseObserver.onError(io.grpc.Status.PERMISSION_DENIED
                        .withDescription("Only group admins can remove members")
                        .asRuntimeException());
                return;
            }

            Optional<GroupMemberEntity> targetOpt = groupMemberRepository.findByGroupIdAndUserId(request.getGroupId(), request.getUserId());
            if (targetOpt.isPresent()) {
                GroupMemberEntity target = targetOpt.get();
                if (target.getRole() == GroupRole.ADMIN) {
                    List<GroupMemberEntity> allMembers = groupMemberRepository.findAllByGroupId(request.getGroupId());
                    long adminCount = allMembers.stream().filter(m -> m.getRole() == GroupRole.ADMIN).count();
                    if (adminCount <= 1) {
                        responseObserver.onError(io.grpc.Status.FAILED_PRECONDITION
                                .withDescription("Nie można usunąć jedynego administratora grupy. Mianuj najpierw innego administratora.")
                                .asRuntimeException());
                        return;
                    }
                }
                groupMemberRepository.delete(target);
                groupRepository.decrementMembersCount(request.getGroupId());
                responseObserver.onNext(RemoveGroupMemberResponse.newBuilder()
                        .setSuccess(true)
                        .setMessage("Member removed successfully")
                        .build());
            } else {
                responseObserver.onNext(RemoveGroupMemberResponse.newBuilder()
                        .setSuccess(false)
                        .setMessage("User is not a member of this group")
                        .build());
            }
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("Failed to remove group member", e);
            responseObserver.onError(io.grpc.Status.INTERNAL
                    .withDescription("Internal error: " + e.getMessage())
                    .asRuntimeException());
        }
    }

    @Transactional
    public void updateMemberRole(UpdateMemberRoleRequest request, StreamObserver<UpdateMemberRoleResponse> responseObserver) {
        log.info("gRPC: Admin {} updating role of user {} in group {} to {}", request.getAdminId(), request.getUserId(), request.getGroupId(), request.getNewRole());
        try {
            Optional<GroupMemberEntity> adminMembership = groupMemberRepository.findByGroupIdAndUserId(request.getGroupId(), request.getAdminId());
            if (adminMembership.isEmpty() || adminMembership.get().getRole() != GroupRole.ADMIN) {
                responseObserver.onError(io.grpc.Status.PERMISSION_DENIED
                        .withDescription("Only group admins can change member roles")
                        .asRuntimeException());
                return;
            }

            Optional<GroupMemberEntity> targetMembership = groupMemberRepository.findByGroupIdAndUserId(request.getGroupId(), request.getUserId());
            if (targetMembership.isPresent()) {
                GroupMemberEntity member = targetMembership.get();
                GroupRole newRoleEnum = GroupRole.fromString(request.getNewRole());

                if (member.getRole() == GroupRole.ADMIN && newRoleEnum != GroupRole.ADMIN) {
                    List<GroupMemberEntity> allMembers = groupMemberRepository.findAllByGroupId(request.getGroupId());
                    long adminCount = allMembers.stream().filter(m -> m.getRole() == GroupRole.ADMIN).count();
                    if (adminCount <= 1) {
                        responseObserver.onError(io.grpc.Status.FAILED_PRECONDITION
                                .withDescription("Nie można odebrać uprawnień jedynemu administratorowi grupy. Mianuj najpierw innego administratora.")
                                .asRuntimeException());
                        return;
                    }
                }

                member.setRole(newRoleEnum);
                groupMemberRepository.save(member);
                responseObserver.onNext(UpdateMemberRoleResponse.newBuilder()
                        .setSuccess(true)
                        .setMessage("Role updated successfully")
                        .build());
            } else {
                responseObserver.onNext(UpdateMemberRoleResponse.newBuilder()
                        .setSuccess(false)
                        .setMessage("User is not a member of this group")
                        .build());
            }
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("Failed to update member role", e);
            responseObserver.onError(io.grpc.Status.INTERNAL
                    .withDescription("Internal error: " + e.getMessage())
                    .asRuntimeException());
        }
    }
}
