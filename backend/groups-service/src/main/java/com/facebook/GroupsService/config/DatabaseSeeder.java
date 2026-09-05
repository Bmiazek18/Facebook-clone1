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

    @Override
    public void run(String... args) throws Exception {
        if (ruleRepository.count() == 0) {
            log.info("Seeding Admin Assist rules...");
            
            com.facebook.GroupsService.entity.AdminAssistRuleEntity ageRule = com.facebook.GroupsService.entity.AdminAssistRuleEntity.builder()
                    .id("rule-age-decline-4")
                    .groupId("4")
                    .target(com.facebook.GroupsService.entity.RuleTarget.JOIN_REQUEST)
                    .action(com.facebook.GroupsService.entity.RuleAction.DECLINE)
                    .criteria(com.facebook.GroupsService.entity.RuleCriteria.builder()
                            .minimumAccountAgeDays(30)
                            .build())
                    .enabled(true)
                    .build();
            ruleService.saveRule(ageRule, "system");

            com.facebook.GroupsService.entity.AdminAssistRuleEntity avatarRule = com.facebook.GroupsService.entity.AdminAssistRuleEntity.builder()
                    .id("rule-avatar-decline-4")
                    .groupId("4")
                    .target(com.facebook.GroupsService.entity.RuleTarget.JOIN_REQUEST)
                    .action(com.facebook.GroupsService.entity.RuleAction.DECLINE)
                    .criteria(com.facebook.GroupsService.entity.RuleCriteria.builder()
                            .requireProfilePicture(true)
                            .build())
                    .enabled(true)
                    .build();
            ruleService.saveRule(avatarRule, "system");

            // Seed a welcome post rule running every day at 12:00
            com.facebook.GroupsService.entity.AdminAssistRuleEntity welcomeRule = com.facebook.GroupsService.entity.AdminAssistRuleEntity.builder()
                    .id("rule-welcome-4")
                    .groupId("4")
                    .target(com.facebook.GroupsService.entity.RuleTarget.WELCOME_POST)
                    .action(com.facebook.GroupsService.entity.RuleAction.PUBLISH)
                    .criteria(com.facebook.GroupsService.entity.RuleCriteria.builder()
                            .welcomeMessage("Witajcie w naszej grupie Kolegium Sędziów BOZPN! Życzymy udanych dyskusji.")
                            .cronExpression("0 0 12 * * ?") // Everyday at 12:00 PM
                            .build())
                    .enabled(true)
                    .build();
            ruleService.saveRule(welcomeRule, "system");
        }

        if (groupRepository.count() == 0) {
            log.info("Seeding groups database...");

        List<GroupEntity> groups = List.of(
            GroupEntity.builder()
                .id("1")
                .name("Frontend Developers")
                .description("A group for frontend developers to share knowledge and best practices.")
                .membersCount(1200)
                .privacy("public")
                .image("https://images.unsplash.com/photo-1498050108023-c5249f4df085?auto=format&fit=crop&w=800&q=80")
                .lastActive("12 min temu")
                .createdAge("2020-03-15")
                .build(),
            GroupEntity.builder()
                .id("2")
                .name("Vue.js Enthusiasts")
                .description("A group for Vue.js enthusiasts to discuss the latest features and projects.")
                .membersCount(2500)
                .privacy("public")
                .image("https://images.unsplash.com/photo-1555066931-4365d14bab8c?auto=format&fit=crop&w=800&q=80")
                .lastActive("30 min temu")
                .createdAge("2021-06-01")
                .build(),
            GroupEntity.builder()
                .id("3")
                .name("Tailwind CSS Fans")
                .description("A group for Tailwind CSS fans to share tips and tricks.")
                .membersCount(800)
                .privacy("private")
                .image("https://images.unsplash.com/photo-1507721999472-8ed4421c4af2?auto=format&fit=crop&w=800&q=80")
                .lastActive("2 godz. temu")
                .createdAge("2022-09-10")
                .build(),
            GroupEntity.builder()
                .id("4")
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
                .id("5")
                .name("Absurdalnie Tanie Loty")
                .description("Grupa dzieląca się informacjami o najtańszych lotach i okazjach podróżniczych.")
                .membersCount(95000)
                .privacy("public")
                .image("https://ui-avatars.com/api/?name=TL&background=3B82F6&color=fff&size=128&font-size=0.4")
                .lastActive("37 min temu")
                .createdAge("2019-11-20")
                .build()
        );

        groupRepository.saveAll(groups);

        // Seed some memberships for user 18 (Bartosz Miazek's UUID 'f92f2541-11bf-383f-8468-d0dfd8787f0b')
        String userUuid = java.util.UUID.nameUUIDFromBytes("testuser1".getBytes()).toString();
        
        List<GroupMemberEntity> members = List.of(
            GroupMemberEntity.builder().groupId("1").userId(userUuid).role(com.facebook.GroupsService.entity.GroupRole.MEMBER).build(),
            GroupMemberEntity.builder().groupId("2").userId(userUuid).role(com.facebook.GroupsService.entity.GroupRole.ADMIN).build(),
            GroupMemberEntity.builder().groupId("4").userId(userUuid).role(com.facebook.GroupsService.entity.GroupRole.MEMBER).build()
        );
        
        groupMemberRepository.saveAll(members);
        }

        // Seed memberships for common user IDs
        List<String> adminUserIds = List.of(
            "e1088d18-971c-4bbf-a6ea-b7692fc3f412", // testuser
            "7f23f5b8-87fb-4250-9ba9-6b5ed04afff0", // dsd
            "0d4b14bc-1337-490f-ba79-27b62f4fdaf6", // bmiazek
            "1e4332f6-5a7a-3210-b5fb-fb92c7c60cce", // Jan Wiśniewski
            java.util.UUID.nameUUIDFromBytes("testuser".getBytes()).toString(),
            java.util.UUID.nameUUIDFromBytes("testuser1".getBytes()).toString()
        );

        for (String adminId : adminUserIds) {
            for (String gId : List.of("1", "2", "4", "5")) {
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

        // Add testuser1 to testuser6 to group 4 if they are not there
        String[] testUsers = {"testuser1", "testuser2", "testuser3", "testuser4", "testuser5", "testuser6"};
        int[] hoursBack = {8, 6, 4, 2, 1, 10};
        for (int i = 0; i < testUsers.length; i++) {
            String uId = java.util.UUID.nameUUIDFromBytes(testUsers[i].getBytes()).toString();
            if (!groupMemberRepository.existsByGroupIdAndUserId("4", uId)) {
                groupMemberRepository.save(GroupMemberEntity.builder()
                    .groupId("4")
                    .userId(uId)
                    .role(com.facebook.GroupsService.entity.GroupRole.MEMBER)
                    .joinedAt(java.time.Instant.now().minus(hoursBack[i], i == 5 ? java.time.temporal.ChronoUnit.MINUTES : java.time.temporal.ChronoUnit.HOURS))
                    .build());
            }
        }

        // Ensure group 4 has the updated stats
        java.util.Optional<GroupEntity> group4Opt = groupRepository.findById("4");
        if (group4Opt.isPresent()) {
            GroupEntity g4 = group4Opt.get();
            g4.setMembersCount(65);
            g4.setNewPostsToday(2);
            g4.setNewPostsMonth(24);
            g4.setNewMembersWeek("Brak nowych członków w ostatnim tygodniu");
            g4.setCreatedAge("2015-08-06");
            groupRepository.save(g4);
        }

        log.info("Groups database seeded successfully.");
    }
}
