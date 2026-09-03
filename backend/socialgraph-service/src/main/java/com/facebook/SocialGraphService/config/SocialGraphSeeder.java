package com.facebook.SocialGraphService.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.neo4j.driver.Driver;
import org.neo4j.driver.Session;
import org.neo4j.driver.Values;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class SocialGraphSeeder implements CommandLineRunner {

    private final Driver neo4jDriver;

    @Override
    public void run(String... args) throws Exception {
        log.info("SocialGraphSeeder: Initializing Neo4j nodes and testuser graph...");
        try (Session session = neo4jDriver.session()) {
            // 1. Create constraint / index if not exists
            try {
                session.executeWrite(tx -> tx.run(
                        "CREATE CONSTRAINT user_id_unique IF NOT EXISTS FOR (u:User) REQUIRE u.userId IS UNIQUE"
                ).consume());
            } catch (Exception e) {
                log.debug("Constraint creation notice: {}", e.getMessage());
            }

            // 2. Ensure testuser node exists
            String testUserId = "e1088d18-971c-4bbf-a6ea-b7692fc3f412";
            session.executeWrite(tx -> tx.run(
                    "MERGE (u:User {userId: $userId}) " +
                    "SET u.username = $username, u.birthDate = $birthDate, u.month = $month " +
                    "RETURN u",
                    Values.parameters("userId", testUserId, "username", "testuser", "birthDate", "1995-05-15", "month", "MAY")
            ).consume());

            log.info("SocialGraphSeeder: Seeded primary testuser node ({})", testUserId);
        } catch (Exception e) {
            log.warn("SocialGraphSeeder: Failed to seed initial Neo4j graph: {}", e.getMessage());
        }
    }
}
