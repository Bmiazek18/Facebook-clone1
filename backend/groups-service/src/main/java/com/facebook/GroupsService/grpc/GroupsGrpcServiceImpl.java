package com.facebook.GroupsService.grpc;

import com.facebook.groups.grpc.*;
import com.facebook.GroupsService.entity.GroupEntity;
import com.facebook.GroupsService.entity.GroupMemberEntity;
import com.facebook.GroupsService.entity.GroupRole;
import com.facebook.GroupsService.repository.GroupMemberRepository;
import com.facebook.GroupsService.repository.GroupRepository;
import io.grpc.stub.StreamObserver;
import com.facebook.GroupsService.event.GroupIndexEvent;
import com.facebook.GroupsService.config.RabbitConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@GrpcService
public class GroupsGrpcServiceImpl extends GroupsGrpcServiceGrpc.GroupsGrpcServiceImplBase {

    private static final Logger log = LoggerFactory.getLogger(GroupsGrpcServiceImpl.class);

    private final GroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final org.springframework.context.ApplicationEventPublisher eventPublisher;
    private final com.facebook.GroupsService.repository.GroupRuleRepository groupRuleRepository;
    private final com.facebook.GroupsService.repository.GroupActivityLogRepository groupActivityLogRepository;
    private final RabbitTemplate rabbitTemplate;

    public GroupsGrpcServiceImpl(GroupRepository groupRepository,
                                  GroupMemberRepository groupMemberRepository,
                                  org.springframework.context.ApplicationEventPublisher eventPublisher,
                                  com.facebook.GroupsService.repository.GroupRuleRepository groupRuleRepository,
                                  com.facebook.GroupsService.repository.GroupActivityLogRepository groupActivityLogRepository,
                                  RabbitTemplate rabbitTemplate) {
        this.groupRepository = groupRepository;
        this.groupMemberRepository = groupMemberRepository;
        this.eventPublisher = eventPublisher;
        this.groupRuleRepository = groupRuleRepository;
        this.groupActivityLogRepository = groupActivityLogRepository;
        this.rabbitTemplate = rabbitTemplate;
    }

    @net.devh.boot.grpc.client.inject.GrpcClient("feed-service")
    private com.facebook.feed.grpc.FeedGrpcServiceGrpc.FeedGrpcServiceBlockingStub feedGrpcStub;

    @jakarta.persistence.PersistenceContext
    private jakarta.persistence.EntityManager entityManager;

    @Override
    @Transactional
    public void createGroup(CreateGroupRequest request, StreamObserver<CreateGroupResponse> responseObserver) {
        log.info("gRPC: Creating group: {} for creator: {}", request.getName(), request.getCreatorId());
        try {
            GroupEntity entity = GroupEntity.builder()
                    .id(UUID.randomUUID().toString())
                    .name(request.getName())
                    .description(request.getDescription())
                    .privacy(request.getPrivacy())
                    .image(request.getImage() != null && !request.getImage().isEmpty() 
                            ? request.getImage() 
                            : "https://ui-avatars.com/api/?name=" + request.getName().replace(" ", "+") + "&background=1F2937&color=fff&size=128")
                    .lastActive("1 min temu")
                    .membersCount(1)
                    .build();

            GroupEntity saved = groupRepository.save(entity);

            GroupMemberEntity creatorMembership = GroupMemberEntity.builder()
                    .groupId(saved.getId())
                    .userId(request.getCreatorId())
                    .role(GroupRole.ADMIN)
                    .build();
            groupMemberRepository.save(creatorMembership);

            // Index group name in search-service via RabbitMQ (under appropriate visibility conditions)
            publishGroupIndex(saved);

            // Automatically create post about group creation
            try {
                if (feedGrpcStub != null) {
                    log.info("Sending gRPC request to feed-service to create post for group creation: {}", saved.getName());
                    com.facebook.feed.grpc.CreatePostRequest postRequest = com.facebook.feed.grpc.CreatePostRequest.newBuilder()
                            .setContent(saved.getName())
                            .setAuthorId(request.getCreatorId())
                            .setTargetId(saved.getId())
                            .setTargetType("GroupCreated")
                            .setVisibility("PUBLIC")
                            .build();
                    feedGrpcStub.createPost(postRequest);
                } else {
                    log.warn("feedGrpcStub is null, cannot automatically create post for group: {}", saved.getName());
                }
            } catch (Exception e) {
                log.error("Failed to automatically create post in feed-service for group: {}", saved.getName(), e);
            }

            GroupDto dto = mapToDto(saved);
            responseObserver.onNext(CreateGroupResponse.newBuilder().setGroup(dto).build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("Failed to create group", e);
            responseObserver.onError(io.grpc.Status.INTERNAL
                    .withDescription("Internal error: " + e.getMessage())
                    .asRuntimeException());
        }
    }

    @Override
    public void getGroupById(GetGroupByIdRequest request, StreamObserver<GetGroupByIdResponse> responseObserver) {
        log.info("gRPC: Fetching group: {}", request.getId());
        try {
            java.util.Optional<GroupEntity> entityOpt = groupRepository.findById(request.getId());
            if (entityOpt.isEmpty()) {
                responseObserver.onError(io.grpc.Status.NOT_FOUND
                        .withDescription("Group not found with ID: " + request.getId())
                        .asRuntimeException());
                return;
            }

            GroupDto dto = mapToDto(entityOpt.get());
            responseObserver.onNext(GetGroupByIdResponse.newBuilder().setGroup(dto).build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("Failed to fetch group", e);
            responseObserver.onError(io.grpc.Status.INTERNAL
                    .withDescription("Internal error: " + e.getMessage())
                    .asRuntimeException());
        }
    }

    @Override
    @Transactional
    public void joinGroup(JoinGroupRequest request, StreamObserver<JoinGroupResponse> responseObserver) {
        log.info("gRPC: User {} joining group {}", request.getUserId(), request.getGroupId());
        try {
            java.util.Optional<GroupEntity> groupOpt = groupRepository.findById(request.getGroupId());
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
                    eventPublisher.publishEvent(new com.facebook.GroupsService.event.JoinRequestCreatedEvent(
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

    @Override
    @Transactional
    public void leaveGroup(LeaveGroupRequest request, StreamObserver<LeaveGroupResponse> responseObserver) {
        log.info("gRPC: User {} leaving group {}", request.getUserId(), request.getGroupId());
        try {
            java.util.Optional<GroupMemberEntity> membershipOpt = groupMemberRepository.findByGroupIdAndUserId(request.getGroupId(), request.getUserId());
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

    @Override
    public void getGroups(GetGroupsRequest request, StreamObserver<GetGroupsResponse> responseObserver) {
        log.info("gRPC: Fetching all groups");
        try {
            List<GroupEntity> entities = groupRepository.findAll();
            List<GroupDto> dtos = entities.stream().map(this::mapToDto).collect(Collectors.toList());
            responseObserver.onNext(GetGroupsResponse.newBuilder().addAllGroups(dtos).build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("Failed to fetch groups list", e);
            responseObserver.onError(io.grpc.Status.INTERNAL
                    .withDescription("Internal error: " + e.getMessage())
                    .asRuntimeException());
        }
    }

    @Override
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

    @Override
    public void getUserGroups(GetUserGroupsRequest request, StreamObserver<GetUserGroupsResponse> responseObserver) {
        log.info("gRPC: Fetching groups for user {}", request.getUserId());
        try {
            List<GroupMemberEntity> memberships = groupMemberRepository.findAllByUserId(request.getUserId());
            List<String> groupIds = memberships.stream()
                    .filter(m -> m.getRole() != GroupRole.PENDING)
                    .map(GroupMemberEntity::getGroupId)
                    .collect(Collectors.toList());
            List<GroupEntity> groups = groupRepository.findAllById(groupIds);
            List<GroupDto> dtos = groups.stream().map(this::mapToDto).collect(Collectors.toList());
            responseObserver.onNext(GetUserGroupsResponse.newBuilder().addAllGroups(dtos).build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("Failed to fetch user groups", e);
            responseObserver.onError(io.grpc.Status.INTERNAL
                    .withDescription("Internal error: " + e.getMessage())
                    .asRuntimeException());
        }
    }

    private int getNewPostsCount(String groupId, int daysAgo) {
        if (entityManager == null) {
            return 0;
        }
        try {
            long sinceMillis = java.time.LocalDate.now()
                    .minusDays(daysAgo)
                    .atStartOfDay(java.time.ZoneId.systemDefault())
                    .toInstant()
                    .toEpochMilli();

            String sql = "SELECT COUNT(*) FROM posts WHERE target_type = 'Group' AND target_id = :groupId AND timestamp >= :sinceMillis";
            Number count = (Number) entityManager.createNativeQuery(sql)
                    .setParameter("groupId", groupId)
                    .setParameter("sinceMillis", sinceMillis)
                    .getSingleResult();
            return count != null ? count.intValue() : 0;
        } catch (Exception e) {
            log.error("Failed to query posts count for group {}", groupId, e);
            return 0;
        }
    }

    private GroupDto mapToDto(GroupEntity entity) {
        int postsToday = getNewPostsCount(entity.getId(), 0);
        int postsMonth = getNewPostsCount(entity.getId(), 30);

        if (postsToday == 0 && entity.getNewPostsToday() != null && entity.getNewPostsToday() > 0) {
            postsToday = entity.getNewPostsToday();
        }
        if (postsMonth == 0 && entity.getNewPostsMonth() != null && entity.getNewPostsMonth() > 0) {
            postsMonth = entity.getNewPostsMonth();
        }

        return GroupDto.newBuilder()
                .setId(entity.getId())
                .setName(entity.getName())
                .setDescription(entity.getDescription() != null ? entity.getDescription() : "")
                .setPrivacy(entity.getPrivacy() != null ? entity.getPrivacy() : "public")
                .setImage(entity.getImage() != null ? entity.getImage() : "")
                .setMembersCount(entity.getMembersCount())
                .setLastActive(entity.getLastActive() != null ? entity.getLastActive() : "1 min temu")
                .setNewPostsToday(postsToday)
                .setNewPostsMonth(postsMonth)
                .setNewMembersWeek(entity.getNewMembersWeek() != null ? entity.getNewMembersWeek() : "")
                .setCreatedAge(entity.getCreatedAge() != null ? entity.getCreatedAge() : "")
                .build();
    }

    @Override
    public void getGroupMembership(GetGroupMembershipRequest request, StreamObserver<GetGroupMembershipResponse> responseObserver) {
        log.info("gRPC: Fetching membership for user {} in group {}", request.getUserId(), request.getGroupId());
        try {
            java.util.Optional<GroupMemberEntity> membershipOpt = groupMemberRepository.findByGroupIdAndUserId(request.getGroupId(), request.getUserId());
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

    @Override
    @Transactional
    public void approveGroupRequest(ApproveGroupRequestMsg request, StreamObserver<ApproveGroupResponseMsg> responseObserver) {
        log.info("gRPC: Approving join request for user {} in group {} by admin {}", request.getUserId(), request.getGroupId(), request.getAdminId());
        try {
            java.util.Optional<GroupMemberEntity> adminMembership = groupMemberRepository.findByGroupIdAndUserId(request.getGroupId(), request.getAdminId());
            if (adminMembership.isEmpty() || adminMembership.get().getRole() != GroupRole.ADMIN) {
                responseObserver.onError(io.grpc.Status.PERMISSION_DENIED
                        .withDescription("Only admins can approve membership requests")
                        .asRuntimeException());
                return;
            }

            java.util.Optional<GroupMemberEntity> pendingMembership = groupMemberRepository.findByGroupIdAndUserId(request.getGroupId(), request.getUserId());
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

    @Override
    @Transactional
    public void rejectGroupRequest(RejectGroupRequestMsg request, StreamObserver<RejectGroupResponseMsg> responseObserver) {
        log.info("gRPC: Rejecting join request for user {} in group {} by admin {}", request.getUserId(), request.getGroupId(), request.getAdminId());
        try {
            java.util.Optional<GroupMemberEntity> adminMembership = groupMemberRepository.findByGroupIdAndUserId(request.getGroupId(), request.getAdminId());
            if (adminMembership.isEmpty() || adminMembership.get().getRole() != GroupRole.ADMIN) {
                responseObserver.onError(io.grpc.Status.PERMISSION_DENIED
                        .withDescription("Only admins can reject membership requests")
                        .asRuntimeException());
                return;
            }

            java.util.Optional<GroupMemberEntity> pendingMembership = groupMemberRepository.findByGroupIdAndUserId(request.getGroupId(), request.getUserId());
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

    @Override
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

    @Override
    @Transactional
    public void removeGroupMember(RemoveGroupMemberRequest request, StreamObserver<RemoveGroupMemberResponse> responseObserver) {
        log.info("gRPC: Admin {} removing user {} from group {}", request.getAdminId(), request.getUserId(), request.getGroupId());
        try {
            java.util.Optional<GroupMemberEntity> adminMembership = groupMemberRepository.findByGroupIdAndUserId(request.getGroupId(), request.getAdminId());
            if (adminMembership.isEmpty() || adminMembership.get().getRole() != GroupRole.ADMIN) {
                responseObserver.onError(io.grpc.Status.PERMISSION_DENIED
                        .withDescription("Only group admins can remove members")
                        .asRuntimeException());
                return;
            }

            java.util.Optional<GroupMemberEntity> targetOpt = groupMemberRepository.findByGroupIdAndUserId(request.getGroupId(), request.getUserId());
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

    @Override
    @Transactional
    public void updateMemberRole(UpdateMemberRoleRequest request, StreamObserver<UpdateMemberRoleResponse> responseObserver) {
        log.info("gRPC: Admin {} updating role of user {} in group {} to {}", request.getAdminId(), request.getUserId(), request.getGroupId(), request.getNewRole());
        try {
            java.util.Optional<GroupMemberEntity> adminMembership = groupMemberRepository.findByGroupIdAndUserId(request.getGroupId(), request.getAdminId());
            if (adminMembership.isEmpty() || adminMembership.get().getRole() != GroupRole.ADMIN) {
                responseObserver.onError(io.grpc.Status.PERMISSION_DENIED
                        .withDescription("Only group admins can change member roles")
                        .asRuntimeException());
                return;
            }

            java.util.Optional<GroupMemberEntity> targetMembership = groupMemberRepository.findByGroupIdAndUserId(request.getGroupId(), request.getUserId());
            if (targetMembership.isPresent()) {
                GroupMemberEntity member = targetMembership.get();
                GroupRole newRoleEnum = GroupRole.fromString(request.getNewRole());
                
                // Last admin demotion guard
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

    @Override
    public void getGroupRules(GetGroupRulesRequest request, StreamObserver<GetGroupRulesResponse> responseObserver) {
        log.info("gRPC: Fetching group rules for group: {}", request.getGroupId());
        try {
            List<com.facebook.GroupsService.entity.GroupRuleEntity> rules = groupRuleRepository.findAllByGroupIdOrderByOrderIndexAsc(request.getGroupId());
            List<GroupRuleDto> dtos = rules.stream().map(rule -> GroupRuleDto.newBuilder()
                    .setId(rule.getId())
                    .setTitle(rule.getTitle())
                    .setDescription(rule.getDescription() != null ? rule.getDescription() : "")
                    .setOrderIndex(rule.getOrderIndex())
                    .build())
                    .collect(Collectors.toList());

            responseObserver.onNext(GetGroupRulesResponse.newBuilder()
                    .addAllRules(dtos)
                    .build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("Failed to fetch group rules", e);
            responseObserver.onError(io.grpc.Status.INTERNAL
                    .withDescription("Internal error: " + e.getMessage())
                    .asRuntimeException());
        }
    }

    @Override
    @Transactional
    public void createGroupRule(CreateGroupRuleRequest request, StreamObserver<CreateGroupRuleResponse> responseObserver) {
        log.info("gRPC: Creating group rule for group: {}", request.getGroupId());
        try {
            List<com.facebook.GroupsService.entity.GroupRuleEntity> rules = groupRuleRepository.findAllByGroupIdOrderByOrderIndexAsc(request.getGroupId());
            int nextOrderIndex = rules.size() + 1;

            com.facebook.GroupsService.entity.GroupRuleEntity ruleEntity = com.facebook.GroupsService.entity.GroupRuleEntity.builder()
                    .id(UUID.randomUUID().toString())
                    .groupId(request.getGroupId())
                    .title(request.getTitle())
                    .description(request.getDescription())
                    .orderIndex(nextOrderIndex)
                    .build();

            com.facebook.GroupsService.entity.GroupRuleEntity saved = groupRuleRepository.save(ruleEntity);

            responseObserver.onNext(CreateGroupRuleResponse.newBuilder()
                    .setRule(GroupRuleDto.newBuilder()
                            .setId(saved.getId())
                            .setTitle(saved.getTitle())
                            .setDescription(saved.getDescription() != null ? saved.getDescription() : "")
                            .setOrderIndex(saved.getOrderIndex())
                            .build())
                    .build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("Failed to create group rule", e);
            responseObserver.onError(io.grpc.Status.INTERNAL
                    .withDescription("Internal error: " + e.getMessage())
                    .asRuntimeException());
        }
    }

    @Override
    @Transactional
    public void updateGroupRulesOrder(UpdateGroupRulesOrderRequest request, StreamObserver<UpdateGroupRulesOrderResponse> responseObserver) {
        log.info("gRPC: Updating rules order for group: {}", request.getGroupId());
        try {
            List<String> orderedIds = request.getRuleIdsList();
            List<com.facebook.GroupsService.entity.GroupRuleEntity> rules = groupRuleRepository.findAllByGroupIdOrderByOrderIndexAsc(request.getGroupId());

            for (com.facebook.GroupsService.entity.GroupRuleEntity rule : rules) {
                int index = orderedIds.indexOf(rule.getId());
                if (index != -1) {
                    rule.setOrderIndex(index + 1);
                    groupRuleRepository.save(rule);
                }
            }

            responseObserver.onNext(UpdateGroupRulesOrderResponse.newBuilder()
                    .setSuccess(true)
                    .build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("Failed to update rules order", e);
            responseObserver.onError(io.grpc.Status.INTERNAL
                    .withDescription("Internal error: " + e.getMessage())
                    .asRuntimeException());
        }
    }

    @Override
    @Transactional
    public void deleteGroupRule(DeleteGroupRuleRequest request, StreamObserver<DeleteGroupRuleResponse> responseObserver) {
        log.info("gRPC: Deleting group rule: {}", request.getRuleId());
        try {
            java.util.Optional<com.facebook.GroupsService.entity.GroupRuleEntity> ruleOpt = groupRuleRepository.findById(request.getRuleId());
            if (ruleOpt.isPresent()) {
                com.facebook.GroupsService.entity.GroupRuleEntity rule = ruleOpt.get();
                String groupId = rule.getGroupId();
                groupRuleRepository.delete(rule);

                List<com.facebook.GroupsService.entity.GroupRuleEntity> remainingRules = groupRuleRepository.findAllByGroupIdOrderByOrderIndexAsc(groupId);
                for (int i = 0; i < remainingRules.size(); i++) {
                    com.facebook.GroupsService.entity.GroupRuleEntity remRule = remainingRules.get(i);
                    remRule.setOrderIndex(i + 1);
                    groupRuleRepository.save(remRule);
                }

                responseObserver.onNext(DeleteGroupRuleResponse.newBuilder()
                        .setSuccess(true)
                        .build());
            } else {
                responseObserver.onNext(DeleteGroupRuleResponse.newBuilder()
                        .setSuccess(false)
                        .build());
            }
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("Failed to delete group rule", e);
            responseObserver.onError(io.grpc.Status.INTERNAL
                    .withDescription("Internal error: " + e.getMessage())
                    .asRuntimeException());
        }
    }

    private String formatTime(java.time.LocalDateTime dt) {
        java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("d MMM yyyy, HH:mm", new java.util.Locale("pl"));
        return dt.format(formatter).replace(".", "");
    }

    private String formatDate(java.time.LocalDateTime dt) {
        java.time.LocalDate today = java.time.LocalDate.now();
        java.time.LocalDate date = dt.toLocalDate();
        if (date.equals(today)) {
            return "dziś";
        }
        java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("d MMM yyyy", new java.util.Locale("pl"));
        return dt.format(formatter).replace(".", "");
    }

    @Override
    public void getGroupActivityLogs(GetGroupActivityLogsRequest request, StreamObserver<GetGroupActivityLogsResponse> responseObserver) {
        log.info("gRPC: Fetching activity logs for group: {}", request.getGroupId());
        try {
            List<com.facebook.GroupsService.entity.GroupActivityLogEntity> logs = groupActivityLogRepository.findAllByGroupIdOrderByCreatedAtDesc(request.getGroupId());
            List<GroupActivityLogDto> dtos = logs.stream().map(l -> GroupActivityLogDto.newBuilder()
                    .setId(l.getId())
                    .setGroupId(l.getGroupId())
                    .setActorId(l.getActorId())
                    .setActorName(l.getActorName())
                    .setText(l.getDescription())
                    .setNote(l.getNote() != null ? l.getNote() : "")
                    .setTime(formatTime(l.getCreatedAt()))
                    .setDate(formatDate(l.getCreatedAt()))
                    .build())
                    .collect(Collectors.toList());

            responseObserver.onNext(GetGroupActivityLogsResponse.newBuilder()
                    .addAllLogs(dtos)
                    .build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("Failed to fetch activity logs", e);
            responseObserver.onError(io.grpc.Status.INTERNAL
                    .withDescription("Internal error: " + e.getMessage())
                    .asRuntimeException());
        }
    }

    @Override
    @Transactional
    public void logGroupActivity(LogGroupActivityRequest request, StreamObserver<LogGroupActivityResponse> responseObserver) {
        log.info("gRPC: Logging group activity for group: {}", request.getGroupId());
        try {
            com.facebook.GroupsService.entity.GroupActivityLogEntity logEntity = com.facebook.GroupsService.entity.GroupActivityLogEntity.builder()
                    .id(UUID.randomUUID().toString())
                    .groupId(request.getGroupId())
                    .actorId(request.getActorId())
                    .actorName(request.getActorName())
                    .description(request.getText())
                    .note(request.getNote())
                    .createdAt(java.time.LocalDateTime.now())
                    .build();

            com.facebook.GroupsService.entity.GroupActivityLogEntity saved = groupActivityLogRepository.save(logEntity);

            responseObserver.onNext(LogGroupActivityResponse.newBuilder()
                    .setLog(GroupActivityLogDto.newBuilder()
                            .setId(saved.getId())
                            .setGroupId(saved.getGroupId())
                            .setActorId(saved.getActorId())
                            .setActorName(saved.getActorName())
                            .setText(saved.getDescription())
                            .setNote(saved.getNote() != null ? saved.getNote() : "")
                            .setTime(formatTime(saved.getCreatedAt()))
                            .setDate(formatDate(saved.getCreatedAt()))
                            .build())
                    .build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("Failed to log activity", e);
            responseObserver.onError(io.grpc.Status.INTERNAL
                    .withDescription("Internal error: " + e.getMessage())
                    .asRuntimeException());
        }
    }

    private void publishGroupIndex(GroupEntity group) {
        try {
            boolean searchable = group.getPrivacy() != null && 
                (group.getPrivacy().equalsIgnoreCase("public") || group.getPrivacy().equalsIgnoreCase("private"));
            
            GroupIndexEvent indexEvent = GroupIndexEvent.builder()
                    .id(group.getId())
                    .name(group.getName())
                    .image(group.getImage())
                    .newPostsCount(group.getNewPostsToday() != null ? group.getNewPostsToday() : 0)
                    .delete(!searchable)
                    .build();
                    
            rabbitTemplate.convertAndSend(
                    RabbitConfig.EXCHANGE_NAME,
                    RabbitConfig.ROUTING_KEY,
                    indexEvent
            );
            log.info("Groups: Published group index event (delete={}) for ID: {}", !searchable, group.getId());
        } catch (Exception e) {
            log.error("Groups: Failed to publish group indexing event to RabbitMQ", e);
        }
    }
}
