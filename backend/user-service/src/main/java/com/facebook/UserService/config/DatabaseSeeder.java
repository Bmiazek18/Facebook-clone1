package com.facebook.UserService.config;

import com.facebook.UserService.model.User;
import com.facebook.UserService.repository.UserRepository;
import com.facebook.UserService.repository.SearchUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Component
@RequiredArgsConstructor
@Slf4j
public class DatabaseSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final SearchUserRepository searchUserRepository;

    @net.devh.boot.grpc.client.inject.GrpcClient("social-graph-service")
    private com.facebook.socialgraph.grpc.SocialGraphGrpcServiceGrpc.SocialGraphGrpcServiceBlockingStub socialGraphGrpcStub;

    @Override
    public void run(String... args) throws Exception {
        log.info("Verifying and seeding initial users including testuser (e1088d18-971c-4bbf-a6ea-b7692fc3f412)...");

        List<UserSeedData> seedList = List.of(
                new UserSeedData(UUID.fromString("e1088d18-971c-4bbf-a6ea-b7692fc3f412"), "testuser", "testuser@lab-bm.com", "Test", "User", "1995-05-15"),
                new UserSeedData("testuser1", "testuser1@example.com", "Piotr", "Kowalski", "1995-03-12"),
                new UserSeedData("testuser2", "testuser2@example.com", "Anna", "Nowak", "1998-07-24"),
                new UserSeedData("testuser3", "testuser3@example.com", "Jan", "Wiśniewski", "1992-11-05"),
                new UserSeedData("testuser4", "testuser4@example.com", "Katarzyna", "Zielińska", "1996-01-19"),
                new UserSeedData("testuser5", "testuser5@example.com", "Michał", "Woźniak", "1990-05-30"),
                new UserSeedData("testuser6", "testuser6@example.com", "Agnieszka", "Dąbrowska", "1994-09-14"),
                new UserSeedData("testuser7", "testuser7@example.com", "Tomasz", "Lewandowski", "1997-12-01"),
                new UserSeedData("testuser8", "testuser8@example.com", "Małgorzata", "Kamińska", "1993-02-18"),
                new UserSeedData("testuser9", "testuser9@example.com", "Marcin", "Wójcik", "1991-06-25"),
                new UserSeedData("testuser10", "testuser10@example.com", "Karolina", "Kaczmarek", "1999-10-09"),
                new UserSeedData("testuser11", "testuser11@example.com", "Mateusz", "Kowalczyk", "1994-04-15"),
                new UserSeedData("testuser12", "testuser12@example.com", "Monika", "Mazur", "1997-08-22"),
                new UserSeedData("testuser13", "testuser13@example.com", "Łukasz", "Krawczyk", "1991-12-02"),
                new UserSeedData("testuser14", "testuser14@example.com", "Sylwia", "Piotrowska", "1995-02-28"),
                new UserSeedData("testuser15", "testuser15@example.com", "Paweł", "Grabowski", "1990-06-12"),
                new UserSeedData("testuser16", "testuser16@example.com", "Natalia", "Pawlak", "1993-09-05"),
                new UserSeedData("testuser17", "testuser17@example.com", "Grzegorz", "Michalski", "1996-10-18"),
                new UserSeedData("testuser18", "testuser18@example.com", "Justyna", "Rutkowska", "1992-05-24"),
                new UserSeedData("testuser19", "testuser19@example.com", "Marcin", "Baran", "1998-03-30"),
                new UserSeedData("testuser20", "testuser20@example.com", "Milena", "Szewczyk", "1999-07-14"),
                new UserSeedData(UUID.fromString("7f23f5b8-87fb-4250-9ba9-6b5ed04afff0"), "dsd@wpl.pl", "dsd@wpl.pl", "Dsd", "User", "1990-01-01"),
                new UserSeedData(UUID.fromString("0d4b14bc-1337-490f-ba79-27b62f4fdaf6"), "bmiazek@12.pl", "bmiazek@12.pl", "Bmiazek", "User", "1990-01-01")
        );

        List<User> users = new ArrayList<>();

        for (UserSeedData seed : seedList) {
            UUID deterministicId = seed.customId != null ? seed.customId : UUID.nameUUIDFromBytes(seed.username.getBytes());
            Optional<User> existing = userRepository.findById(deterministicId);
            if (existing.isEmpty()) {
                existing = userRepository.findByUsername(seed.username);
            }
            if (existing.isEmpty()) {
                existing = userRepository.findByEmail(seed.email);
            }

            User user;
            if (existing.isPresent()) {
                user = existing.get();
            } else {
                user = User.builder()
                        .id(deterministicId)
                        .username(seed.username)
                        .email(seed.email)
                        .password("Password123!") // default password
                        .firstName(seed.firstName)
                        .lastName(seed.lastName)
                        .birthDate(seed.birthDate)
                        .bio("Welcome to Facebook Clone!")
                        .location("Warsaw, Poland")
                        .gender("CUSTOM")
                        .pronouns("they/them")
                        .relationshipStatus("SINGLE")
                        .build();
                user = userRepository.save(user);
                log.info("Seeded user {} with ID {}", seed.username, user.getId());
            }
            users.add(user);
        }

        // SocialGraph starts after UserService in compose, so this must not block
        // the HTTP health check. Retry until its gRPC endpoint becomes available.
        CompletableFuture.runAsync(() -> seedSocialGraphWithRetry(users));
    }

    private void seedSocialGraphWithRetry(List<User> users) {
        for (int attempt = 1; attempt <= 12; attempt++) {
            try {
                for (User user : users) {
                    socialGraphGrpcStub.createUserNode(com.facebook.socialgraph.grpc.CreateNodeRequest.newBuilder()
                            .setUserId(String.valueOf(user.getId()))
                            .setUsername(user.getUsername())
                            .setBirthDate(user.getBirthDate() != null ? user.getBirthDate() : "")
                            .build());
                }
                seedFriendships(users);
                log.info("Finished social graph relationship seeding.");
                return;
            } catch (Exception e) {
                log.warn("Social graph not ready (attempt {}/12): {}", attempt, e.getMessage());
                try { Thread.sleep(5000); } catch (InterruptedException interrupted) { Thread.currentThread().interrupt(); return; }
            }
        }
        log.error("Social graph seed failed after retries");
    }

    private void seedFriendships(List<User> users) {
        log.info("Seeding Neo4j friendships between test users...");
        
        // testuser (index 0, e1088d18-971c-4bbf-a6ea-b7692fc3f412) is friends with users 1-6
        User testUser = users.get(0);
        for (int i = 1; i <= Math.min(6, users.size() - 1); i++) {
            makeFriends(testUser.getId(), users.get(i).getId());
        }

        // Pending friend requests sent to testUser
        if (users.size() > 8) {
            sendPendingFriendRequest(users.get(7).getId(), testUser.getId());
            sendPendingFriendRequest(users.get(8).getId(), testUser.getId());
        }

        // testuser4 (index 4) is friends with everyone else
        if (users.size() > 4) {
            User u4 = users.get(4);
            for (int i = 0; i < users.size(); i++) {
                if (i != 4 && i != 0) {
                    makeFriends(u4.getId(), users.get(i).getId());
                }
            }
        }

        // Additional mutual friendships
        if (users.size() > 3) {
            makeFriends(users.get(1).getId(), users.get(2).getId());
            makeFriends(users.get(1).getId(), users.get(3).getId());
            makeFriends(users.get(2).getId(), users.get(3).getId());
        }
    }

    private void sendPendingFriendRequest(java.util.UUID senderId, java.util.UUID receiverId) {
        try {
            socialGraphGrpcStub.sendFriendRequest(com.facebook.socialgraph.grpc.FriendRequestMsg.newBuilder()
                    .setSenderId(senderId.toString())
                    .setReceiverId(receiverId.toString())
                    .build());
            log.info("Sent pending friend request from {} to {}", senderId, receiverId);
        } catch (Exception e) {
            log.warn("Failed to send pending friend request from {} to {}: {}", senderId, receiverId, e.getMessage());
        }
    }

    private void makeFriends(java.util.UUID user1Id, java.util.UUID user2Id) {
        try {
            socialGraphGrpcStub.sendFriendRequest(com.facebook.socialgraph.grpc.FriendRequestMsg.newBuilder()
                    .setSenderId(user1Id.toString())
                    .setReceiverId(user2Id.toString())
                    .build());
            socialGraphGrpcStub.acceptFriendRequest(com.facebook.socialgraph.grpc.FriendRequestMsg.newBuilder()
                    .setSenderId(user1Id.toString())
                    .setReceiverId(user2Id.toString())
                    .build());
        } catch (Exception e) {
            log.error("Failed to make users {} and {} friends: {}", user1Id, user2Id, e.getMessage());
        }
    }

    private static class UserSeedData {
        UUID customId;
        String username;
        String email;
        String firstName;
        String lastName;
        String birthDate;

        UserSeedData(String username, String email, String firstName, String lastName, String birthDate) {
            this.username = username;
            this.email = email;
            this.firstName = firstName;
            this.lastName = lastName;
            this.birthDate = birthDate;
        }

        UserSeedData(UUID customId, String username, String email, String firstName, String lastName, String birthDate) {
            this(username, email, firstName, lastName, birthDate);
            this.customId = customId;
        }
    }
}
