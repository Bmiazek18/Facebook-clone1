package com.facebook.ChatEdgeService.mapper;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ChatMapperTest {

    private final ChatMapper chatMapper = new ChatMapperImpl();

    @Test
    void testMapToJson() {
        Map<String, String> map = new HashMap<>();
        map.put("user-456", "LOVE");

        String json = chatMapper.mapToJson(map);

        assertEquals("{\"user-456\":\"LOVE\"}", json);
    }

    @Test
    void testMapToJsonNullAndEmpty() {
        assertEquals("{}", chatMapper.mapToJson(null));
        assertEquals("{}", chatMapper.mapToJson(Map.of()));
    }
}
