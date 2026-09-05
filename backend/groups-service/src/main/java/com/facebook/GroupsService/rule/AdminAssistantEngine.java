package com.facebook.GroupsService.rule;

import com.facebook.GroupsService.entity.AdminAssistRuleEntity;
import com.facebook.GroupsService.entity.GroupMemberEntity;
import com.facebook.GroupsService.entity.GroupRole;
import com.facebook.GroupsService.event.JoinRequestCreatedEvent;
import com.facebook.GroupsService.repository.AdminAssistRuleRepository;
import com.facebook.GroupsService.repository.GroupMemberRepository;
import com.facebook.GroupsService.repository.GroupRepository;
import com.facebook.user.grpc.GetUserByIdRequest;
import com.facebook.user.grpc.GetUserByIdResponse;
import com.facebook.user.grpc.UserDto;
import com.facebook.user.grpc.UserGrpcServiceGrpc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service
public class AdminAssistantEngine {

    private static final Logger log = LoggerFactory.getLogger(AdminAssistantEngine.class);

    private final AdminAssistRuleRepository ruleRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final GroupRepository groupRepository;
    private final List<RuleEvaluator> evaluators;

    public AdminAssistantEngine(AdminAssistRuleRepository ruleRepository,
                                GroupMemberRepository groupMemberRepository,
                                GroupRepository groupRepository,
                                List<RuleEvaluator> evaluators) {
        this.ruleRepository = ruleRepository;
        this.groupMemberRepository = groupMemberRepository;
        this.groupRepository = groupRepository;
        this.evaluators = evaluators;
    }

    @GrpcClient("user-service")
    private UserGrpcServiceGrpc.UserGrpcServiceBlockingStub userGrpcStub;

    @org.springframework.transaction.event.TransactionalEventListener(phase = org.springframework.transaction.event.TransactionPhase.AFTER_COMMIT)
    @org.springframework.scheduling.annotation.Async
    @Transactional(propagation = org.springframework.transaction.annotation.Propagation.REQUIRES_NEW)
    public void handleJoinRequest(JoinRequestCreatedEvent event) {
        log.info("AdminAssist: Evaluating join request for user {} in group {}", event.userId(), event.groupId());
        
        Optional<GroupMemberEntity> membershipOpt = groupMemberRepository.findByGroupIdAndUserId(event.groupId(), event.userId());
        if (membershipOpt.isEmpty() || membershipOpt.get().getRole() != GroupRole.PENDING) {
            log.warn("AdminAssist: Membership request not found or not PENDING for user {} in group {}", event.userId(), event.groupId());
            return;
        }

        GroupMemberEntity membership = membershipOpt.get();

        // 1. Fetch user details from user-service
        UserDto userDetails = null;
        try {
            if (userGrpcStub != null) {
                GetUserByIdResponse response = userGrpcStub.getUserById(
                        GetUserByIdRequest.newBuilder().setUserId(event.userId()).build()
                );
                userDetails = response.getUser();
            } else {
                log.warn("AdminAssist: userGrpcStub is null, cannot fetch user details for {}", event.userId());
            }
        } catch (Exception e) {
            log.error("AdminAssist: Failed to fetch user details for {}", event.userId(), e);
            return; // Exit if we cannot fetch user details to avoid false evaluation
        }

        RuleContext context = RuleContext.builder()
                .groupId(event.groupId())
                .userId(event.userId())
                .userDetails(userDetails)
                .build();

        // 2. Fetch active rules for the group
        List<AdminAssistRuleEntity> rules = ruleRepository.findAllByGroupIdAndEnabledTrue(event.groupId());
        
        // Filter rules specifically for JOIN_REQUEST
        List<AdminAssistRuleEntity> joinRules = rules.stream()
                .filter(r -> com.facebook.GroupsService.entity.RuleTarget.JOIN_REQUEST == r.getTarget())
                .toList();

        // Evaluate Decline/Reject rules first (Veto)
        boolean shouldDecline = false;
        for (AdminAssistRuleEntity rule : joinRules) {
            if (com.facebook.GroupsService.entity.RuleAction.DECLINE == rule.getAction()) {
                if (evaluateRule(rule, context)) {
                    shouldDecline = true;
                    log.info("AdminAssist: Decline rule matched (Rule ID: {}). Declining user {}", rule.getId(), event.userId());
                    break;
                }
            }
        }

        if (shouldDecline) {
            groupMemberRepository.delete(membership);
            log.info("AdminAssist: Join request DECLINED for user {} in group {}", event.userId(), event.groupId());
            return;
        }

        // Evaluate Approve rules
        boolean shouldApprove = false;
        for (AdminAssistRuleEntity rule : joinRules) {
            if (com.facebook.GroupsService.entity.RuleAction.APPROVE == rule.getAction()) {
                if (evaluateRule(rule, context)) {
                    shouldApprove = true;
                    log.info("AdminAssist: Approve rule matched (Rule ID: {}). Approving user {}", rule.getId(), event.userId());
                    break;
                }
            }
        }

        if (shouldApprove) {
            membership.setRole(GroupRole.MEMBER);
            groupMemberRepository.save(membership);
            groupRepository.incrementMembersCount(event.groupId());
            log.info("AdminAssist: Join request APPROVED for user {} in group {}", event.userId(), event.groupId());
        }
    }

    private boolean evaluateRule(AdminAssistRuleEntity rule, RuleContext context) {
        if (rule.getCriteria() == null) {
            return false;
        }
        for (RuleEvaluator evaluator : evaluators) {
            if (evaluator.supports(rule.getCriteria())) {
                if (evaluator.evaluate(rule.getCriteria(), context)) {
                    return true;
                }
            }
        }
        return false;
    }
}
