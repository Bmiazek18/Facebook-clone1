package com.facebook.SocialGraphService.config;

import org.neo4j.driver.Driver;
import org.neo4j.driver.Session;
import org.neo4j.driver.Values;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class SocialGraphSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(SocialGraphSeeder.class);
    private final Driver neo4jDriver;

    public SocialGraphSeeder(Driver neo4jDriver) {
        this.neo4jDriver = neo4jDriver;
    }

    @Override
    public void run(String... args) throws Exception {
        log.info("SocialGraphSeeder: Initializing Neo4j nodes and testuser graph...");
        try (Session session = neo4jDriver.session()) {
            // 1. Create constraints / indexes if not exists
            try {
                session.executeWrite(tx -> {
                    tx.run("CREATE CONSTRAINT user_id_unique IF NOT EXISTS FOR (u:User) REQUIRE u.userId IS UNIQUE").consume();
                    tx.run("CREATE CONSTRAINT city_name_unique IF NOT EXISTS FOR (c:City) REQUIRE c.name IS UNIQUE").consume();
                    tx.run("CREATE CONSTRAINT school_name_unique IF NOT EXISTS FOR (s:School) REQUIRE s.name IS UNIQUE").consume();
                    return null;
                });
            } catch (Exception e) {
                log.debug("Constraint creation notice: {}", e.getMessage());
            }

            // 2. Ensure testuser node exists with city and school
            String testUserId = "e1088d18-971c-4bbf-a6ea-b7692fc3f412";
            session.executeWrite(tx -> {
                tx.run(
                    "MERGE (u:User {userId: $userId}) " +
                    "SET u.username = $username, u.birthDate = $birthDate, u.month = $month " +
                    "MERGE (c:City {name: $cityName}) " +
                    "MERGE (u)-[:LIVES_IN]->(c) " +
                    "MERGE (s:School {name: $schoolName}) " +
                    "MERGE (u)-[:ATTENDED_HIGH_SCHOOL]->(s) " +
                    "RETURN u",
                    Values.parameters(
                        "userId", testUserId,
                        "username", "testuser",
                        "birthDate", "1995-05-15",
                        "month", "MAY",
                        "cityName", "Radom",
                        "schoolName", "Zespół Szkół Elektronicznych w Radomiu"
                    )
                ).consume();
                return null;
            });

            log.info("SocialGraphSeeder: Seeded primary testuser node ({}) with City and High School relationships", testUserId);
        } catch (Exception e) {
            log.warn("SocialGraphSeeder: Failed to seed initial Neo4j graph: {}", e.getMessage());
        }
    }
}
