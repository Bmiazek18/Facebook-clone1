package com.facebook.ChatEdgeService.mapper;

import com.facebook.ChatEdgeService.codegen.types.ChatMessage;
import com.facebook.chat.grpc.ChatMessageDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.Map;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ChatMapper {

    ObjectMapper objectMapper = new ObjectMapper();

    @Mapping(target = "reactionsJson", expression = "java(mapToJson(msg.getReactionsMap()))")
    ChatMessage grpcChatMessageToDgsChat(ChatMessageDto msg);

    default String mapToJson(Map<String, String> map) {
        if (map == null || map.isEmpty()) return "{}";
        try {
            return objectMapper.writeValueAsString(map);
        } catch (Exception e) {
            return "{}";
        }
    }
}
