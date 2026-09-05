package com.facebook.GroupsService.config;

import com.facebook.GroupsService.entity.GroupEntity;
import com.facebook.GroupsService.entity.GroupMemberEntity;
import com.facebook.GroupsService.repository.GroupMemberRepository;
import com.facebook.GroupsService.repository.GroupRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DatabaseSeeder implements CommandLineRunner {

    private final GroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final com.facebook.GroupsService.repository.AdminAssistRuleRepository ruleRepository;
    private final com.facebook.GroupsService.service.AdminAssistRuleService ruleService;

    public static final String GROUP_FRONTEND_ID = "1b9d6bcd-bbfd-4b2d-9b5d-ab8dfbbd4bed";
    public static final String GROUP_VUE_ID = "2b9d6bcd-bbfd-4b2d-9b5d-ab8dfbbd4bed";
    public static final String GROUP_TAILWIND_ID = "3b9d6bcd-bbfd-4b2d-9b5d-ab8dfbbd4bed";
    public static final String GROUP_KOLEGIUM_SEDZIOW_ID = "4b9d6bcd-bbfd-4b2d-9b5d-ab8dfbbd4bed";
    public static final String GROUP_TANIE_LOTY_ID = "5b9d6bcd-bbfd-4b2d-9b5d-ab8dfbbd4bed";

    @Override
    public void run(String... args) throws Exception {
        log.info("Seeding / updating groups database with standard UUIDs...");

        List<GroupEntity> groups = List.of(
            GroupEntity.builder()
                .id(GROUP_FRONTEND_ID)
                .name("Frontend Developers")
                .description("A group for frontend developers to share knowledge and best practices.")
                .membersCount(1200)
                .privacy("public")
                .image("https://images.unsplash.com/photo-1498050108023-c5249f4df085?auto=format&fit=crop&w=800&q=80")
                .lastActive("12 min temu")
                .createdAge("2020-03-15")
                .build(),
            GroupEntity.builder()
                .id(GROUP_VUE_ID)
                .name("Vue.js Enthusiasts")
                .description("A group for Vue.js enthusiasts to discuss the latest features and projects.")
                .membersCount(2500)
                .privacy("public")
                .image("https://images.unsplash.com/photo-1555066931-4365d14bab8c?auto=format&fit=crop&w=800&q=80")
                .lastActive("30 min temu")
                .createdAge("2021-06-01")
                .build(),
            GroupEntity.builder()
                .id(GROUP_TAILWIND_ID)
                .name("Tailwind CSS Fans")
                .description("A group for Tailwind CSS fans to share tips and tricks.")
                .membersCount(800)
                .privacy("private")
                .image("https://images.unsplash.com/photo-1507721999472-8ed4421c4af2?auto=format&fit=crop&w=800&q=80")
                .lastActive("2 godz. temu")
                .createdAge("2022-09-10")
                .build(),
            GroupEntity.builder()
                .id(GROUP_KOLEGIUM_SEDZIOW_ID)
                .name("Kolegium Sędziów BOZPN")
                .description("Oficjalna grupa Kolegium Sędziów BOZPN.")
                .membersCount(65)
                .privacy("private")
                .image("https://ui-avatars.com/api/?name=KS&background=3F6212&color=fff&size=128&font-size=0.4")
                .lastActive("2 dni temu")
                .newPostsToday(2)
                .newPostsMonth(24)
                .newMembersWeek("Brak nowych członków w ostatnim tygodniu")
                .createdAge("2015-08-06")
                .build(),
            GroupEntity.builder()
                .id(GROUP_TANIE_LOTY_ID)
                .name("Absurdalnie Tanie Loty")
                .description("Grupa dzieląca się informacjami o najtańszych lotach i okazjach podróżniczych.")
                .membersCount(95000)
                .privacy("public")
                .image("https://ui-avatars.com/api/?name=TL&background=3B82F6&color=fff&size=128&font-size=0.4")
                .lastActive("37 min temu")
                .createdAge("2019-11-20")
                .build()
        );

        for (GroupEntity g : groups) {
            if (!groupRepository.existsById(g.getId())) {
                groupRepository.save(g);
            }
        }

        // Seed Admin Assist rules for Kolegium Sędziów BOZPN
        for (String gId : List.of(GROUP_KOLEGIUM_SEDZIOW_ID, "4")) {
            String ageRuleId = "rule-age-decline-" + gId;
            if (!ruleRepository.existsById(ageRuleId)) {
                ruleService.saveRule(com.facebook.GroupsService.entity.AdminAssistRuleEntity.builder()
                        .id(ageRuleId)
                        .groupId(gId)
                        .target(com.facebook.GroupsService.entity.RuleTarget.JOIN_REQUEST)
                        .action(com.facebook.GroupsService.entity.RuleAction.DECLINE)
                        .criteria(com.facebook.GroupsService.entity.RuleCriteria.builder()
                                .minimumAccountAgeDays(30)
                                .build())
                        .enabled(true)
                        .build(), "system");
            }

            String avatarRuleId = "rule-avatar-decline-" + gId;
            if (!ruleRepository.existsById(avatarRuleId)) {
                ruleService.saveRule(com.facebook.GroupsService.entity.AdminAssistRuleEntity.builder()
                        .id(avatarRuleId)
                        .groupId(gId)
                        .target(com.facebook.GroupsService.entity.RuleTarget.JOIN_REQUEST)
                        .action(com.facebook.GroupsService.entity.RuleAction.DECLINE)
                        .criteria(com.facebook.GroupsService.entity.RuleCriteria.builder()
                                .requireProfilePicture(true)
                                .build())
                        .enabled(true)
                        .build(), "system");
            }

            String welcomeRuleId = "rule-welcome-" + gId;
            if (!ruleRepository.existsById(welcomeRuleId)) {
                ruleService.saveRule(com.facebook.GroupsService.entity.AdminAssistRuleEntity.builder()
                        .id(welcomeRuleId)
                        .groupId(gId)
                        .target(com.facebook.GroupsService.entity.RuleTarget.WELCOME_POST)
                        .action(com.facebook.GroupsService.entity.RuleAction.PUBLISH)
                        .criteria(com.facebook.GroupsService.entity.RuleCriteria.builder()
                                .welcomeMessage("Witajcie w naszej grupie Kolegium Sędziów BOZPN! Życzymy udanych dyskusji.")
                                .cronExpression("0 0 12 * * ?")
                                .build())
                        .enabled(true)
                        .build(), "system");
            }
        }

        // Seed ADMIN memberships for current user and test accounts across all groups
        List<String> adminUserIds = List.of(
            "e1088d18-971c-4bbf-a6ea-b7692fc3f412", // testuser (current logged-in user in production)
            "7f23f5b8-87fb-4250-9ba9-6b5ed04afff0", // dsd
            "0d4b14bc-1337-490f-ba79-27b62f4fdaf6", // bmiazek
            "1e4332f6-5a7a-3210-b5fb-fb92c7c60cce", // Jan Wiśniewski
            java.util.UUID.nameUUIDFromBytes("testuser".getBytes()).toString(),
            java.util.UUID.nameUUIDFromBytes("testuser1".getBytes()).toString()
        );

        java.util.Set<String> allGroupIds = new java.util.HashSet<>(List.of(
            GROUP_FRONTEND_ID, GROUP_VUE_ID, GROUP_TAILWIND_ID, GROUP_KOLEGIUM_SEDZIOW_ID, GROUP_TANIE_LOTY_ID,
            "1", "2", "3", "4", "5"
        ));
        groupRepository.findAll().forEach(g -> allGroupIds.add(g.getId()));

        for (String adminId : adminUserIds) {
            for (String gId : allGroupIds) {
                java.util.Optional<GroupMemberEntity> existingMembership = groupMemberRepository.findByGroupIdAndUserId(gId, adminId);
                if (existingMembership.isPresent()) {
                    GroupMemberEntity m = existingMembership.get();
                    m.setRole(com.facebook.GroupsService.entity.GroupRole.ADMIN);
                    groupMemberRepository.save(m);
                } else {
                    groupMemberRepository.save(GroupMemberEntity.builder()
                        .groupId(gId)
                        .userId(adminId)
                        .role(com.facebook.GroupsService.entity.GroupRole.ADMIN)
                        .joinedAt(java.time.Instant.now().minus(24, java.time.temporal.ChronoUnit.HOURS))
                        .build());
                }
            }
        }

        // Add testuser1 to testuser6 to Kolegium Sędziów BOZPN if they are not there
        String[] testUsers = {"testuser1", "testuser2", "testuser3", "testuser4", "testuser5", "testuser6"};
        int[] hoursBack = {8, 6, 4, 2, 1, 10};
        for (int i = 0; i < testUsers.length; i++) {
            String uId = java.util.UUID.nameUUIDFromBytes(testUsers[i].getBytes()).toString();
            for (String gId : List.of(GROUP_KOLEGIUM_SEDZIOW_ID, "4")) {
                if (!groupMemberRepository.existsByGroupIdAndUserId(gId, uId)) {
                    groupMemberRepository.save(GroupMemberEntity.builder()
                        .groupId(gId)
                        .userId(uId)
                        .role(com.facebook.GroupsService.entity.GroupRole.MEMBER)
                        .joinedAt(java.time.Instant.now().minus(hoursBack[i], i == 5 ? java.time.temporal.ChronoUnit.MINUTES : java.time.temporal.ChronoUnit.HOURS))
                        .build());
                }
            }
        }

        log.info("Groups database seeded successfully with standard UUIDs and ADMIN permissions.");
    }
}
