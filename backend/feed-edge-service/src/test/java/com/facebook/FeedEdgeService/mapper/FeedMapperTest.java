package com.facebook.FeedEdgeService.mapper;

import com.facebook.FeedEdgeService.codegen.types.CreateEventInput;
import com.facebook.feed.grpc.CreateEventRequest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FeedMapperTest {

    private final FeedMapper feedMapper = new FeedMapperImpl();

    @Test
    void testBuildCreateEventRequest() {
        CreateEventInput input = CreateEventInput.newBuilder()
                .userId("user-123")
                .name("Birthday Party")
                .startDate("2026-08-19")
                .type("PHYSICAL")
                .privacy("PRIVATE")
                .title("My Birthday")
                .description("Party description")
                .build();

        CreateEventRequest request = feedMapper.buildCreateEventRequest(input);

        assertEquals("user-123", request.getUserId());
        assertEquals("Birthday Party", request.getName());
        assertEquals("2026-08-19", request.getStartDate());
        assertEquals("PHYSICAL", request.getType());
        assertEquals("PRIVATE", request.getPrivacy());
        assertEquals("My Birthday", request.getTitle());
        assertEquals("Party description", request.getDescription());
    }
}
