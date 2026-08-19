package com.facebook.SocialGraphService;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(properties = {
    "grpc.server.port=-1"
})
@ActiveProfiles("test")
class SocialGraphServiceApplicationTests {

    @org.springframework.test.context.bean.override.mockito.MockitoBean
    private org.neo4j.driver.Driver neo4jDriver;

    @Test
    void contextLoads() {
    }
}
