package com.facebook.GroupsService.grpc.handler;

import com.facebook.GroupsService.config.RabbitConfig;
import com.facebook.GroupsService.entity.GroupActivityLogEntity;
import com.facebook.GroupsService.entity.GroupEntity;
import com.facebook.GroupsService.entity.GroupMemberEntity;
import com.facebook.GroupsService.entity.GroupRole;
import com.facebook.GroupsService.entity.GroupRuleEntity;
import com.facebook.GroupsService.event.GroupIndexEvent;
import com.facebook.GroupsService.repository.GroupActivityLogRepository;
import com.facebook.GroupsService.repository.GroupMemberRepository;
import com.facebook.GroupsService.repository.GroupRepository;
import com.facebook.GroupsService.repository.GroupRuleRepository;
import com.facebook.groups.grpc.*;
import io.grpc.stub.StreamObserver;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class GroupManagementGrpcHandler {

    private final GroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final GroupRuleRepository groupRuleRepository;
    private final GroupActivityLogRepository groupActivityLogRepository;
    private final RabbitTemplate rabbitTemplate;

    @GrpcClient("feed-service")
    private com.facebook.feed.grpc.FeedGrpcServiceGrpc.FeedGrpcServiceBlockingStub feedGrpcStub;

    @PersistenceContext
    private EntityManager entityManager;

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

            publishGroupIndex(saved);

            try {
                if (feedGrpcStub != null) {
                    log.info("Sending gRPC request to feed-service to create post for group creation: {}", saved.getName());
                    var postRequest = com.facebook.feed.grpc.CreatePostRequest.newBuilder()
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

    public void getGroupById(GetGroupByIdRequest request, StreamObserver<GetGroupByIdResponse> responseObserver) {
        log.info("gRPC: Fetching group: {}", request.getId());
        try {
            Optional<GroupEntity> entityOpt = groupRepository.findById(request.getId());
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

    public void getGroupRules(GetGroupRulesRequest request, StreamObserver<GetGroupRulesResponse> responseObserver) {
        log.info("gRPC: Fetching group rules for group: {}", request.getGroupId());
        try {
            List<GroupRuleEntity> rules = groupRuleRepository.findAllByGroupIdOrderByOrderIndexAsc(request.getGroupId());
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

    @Transactional
    public void createGroupRule(CreateGroupRuleRequest request, StreamObserver<CreateGroupRuleResponse> responseObserver) {
        log.info("gRPC: Creating group rule for group: {}", request.getGroupId());
        try {
            List<GroupRuleEntity> rules = groupRuleRepository.findAllByGroupIdOrderByOrderIndexAsc(request.getGroupId());
            int nextOrderIndex = rules.size() + 1;

            GroupRuleEntity ruleEntity = GroupRuleEntity.builder()
                    .id(UUID.randomUUID().toString())
                    .groupId(request.getGroupId())
                    .title(request.getTitle())
                    .description(request.getDescription())
                    .orderIndex(nextOrderIndex)
                    .build();

            GroupRuleEntity saved = groupRuleRepository.save(ruleEntity);

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

    @Transactional
    public void updateGroupRulesOrder(UpdateGroupRulesOrderRequest request, StreamObserver<UpdateGroupRulesOrderResponse> responseObserver) {
        log.info("gRPC: Updating rules order for group: {}", request.getGroupId());
        try {
            List<String> orderedIds = request.getRuleIdsList();
            List<GroupRuleEntity> rules = groupRuleRepository.findAllByGroupIdOrderByOrderIndexAsc(request.getGroupId());

            for (GroupRuleEntity rule : rules) {
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

    @Transactional
    public void deleteGroupRule(DeleteGroupRuleRequest request, StreamObserver<DeleteGroupRuleResponse> responseObserver) {
        log.info("gRPC: Deleting group rule: {}", request.getRuleId());
        try {
            Optional<GroupRuleEntity> ruleOpt = groupRuleRepository.findById(request.getRuleId());
            if (ruleOpt.isPresent()) {
                GroupRuleEntity rule = ruleOpt.get();
                String groupId = rule.getGroupId();
                groupRuleRepository.delete(rule);

                List<GroupRuleEntity> remainingRules = groupRuleRepository.findAllByGroupIdOrderByOrderIndexAsc(groupId);
                for (int i = 0; i < remainingRules.size(); i++) {
                    GroupRuleEntity remRule = remainingRules.get(i);
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

    public void getGroupActivityLogs(GetGroupActivityLogsRequest request, StreamObserver<GetGroupActivityLogsResponse> responseObserver) {
        log.info("gRPC: Fetching activity logs for group: {}", request.getGroupId());
        try {
            List<GroupActivityLogEntity> logs = groupActivityLogRepository.findAllByGroupIdOrderByCreatedAtDesc(request.getGroupId());
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

    @Transactional
    public void logGroupActivity(LogGroupActivityRequest request, StreamObserver<LogGroupActivityResponse> responseObserver) {
        log.info("gRPC: Logging group activity for group: {}", request.getGroupId());
        try {
            GroupActivityLogEntity logEntity = GroupActivityLogEntity.builder()
                    .id(UUID.randomUUID().toString())
                    .groupId(request.getGroupId())
                    .actorId(request.getActorId())
                    .actorName(request.getActorName())
                    .description(request.getText())
                    .note(request.getNote())
                    .createdAt(LocalDateTime.now())
                    .build();

            GroupActivityLogEntity saved = groupActivityLogRepository.save(logEntity);

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

    public GroupDto mapToDto(GroupEntity entity) {
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

    private int getNewPostsCount(String groupId, int daysAgo) {
        if (entityManager == null) {
            return 0;
        }
        try {
            long sinceMillis = LocalDate.now()
                    .minusDays(daysAgo)
                    .atStartOfDay(ZoneId.systemDefault())
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

    private String formatTime(LocalDateTime dt) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d MMM yyyy, HH:mm", new Locale("pl"));
        return dt.format(formatter).replace(".", "");
    }

    private String formatDate(LocalDateTime dt) {
        LocalDate today = LocalDate.now();
        LocalDate date = dt.toLocalDate();
        if (date.equals(today)) {
            return "dziś";
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d MMM yyyy", new Locale("pl"));
        return dt.format(formatter).replace(".", "");
    }
}
