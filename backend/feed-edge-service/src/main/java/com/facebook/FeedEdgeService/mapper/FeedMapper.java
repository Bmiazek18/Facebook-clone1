package com.facebook.FeedEdgeService.mapper;

import com.facebook.FeedEdgeService.codegen.types.CreateEventInput;
import com.facebook.FeedEdgeService.codegen.types.Comment;
import com.facebook.FeedEdgeService.codegen.types.Event;
import com.facebook.FeedEdgeService.codegen.types.Post;
import com.facebook.FeedEdgeService.codegen.types.PostMedia;
import com.facebook.FeedEdgeService.codegen.types.ReactionDetail;
import com.facebook.FeedEdgeService.codegen.types.Story;
import com.facebook.feed.grpc.CreateEventRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.function.Consumer;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface FeedMapper {

    @Mapping(target = "media", source = "mediaList")
    @Mapping(target = "allowedUserIds", source = "allowedUserIdsList")
    @Mapping(target = "reactions", ignore = true)
    @Mapping(target = "sharedPost", ignore = true)
    Post mapToPost(com.facebook.feed.grpc.Post grpcPost);

    PostMedia mapToPostMedia(com.facebook.feed.grpc.PostMedia grpcMedia);

    @Mapping(target = "viewedBy", source = "viewedByUserIdsList")
    Story mapToStory(com.facebook.feed.grpc.Story grpcStory);

    @Mapping(target = "images", source = "imagesList")
    @Mapping(target = "hosts", source = "hostsList")
    @Mapping(target = "coordinates", source = "coordinatesList")
    Event mapToEvent(com.facebook.feed.grpc.Event grpcEvent);

    @Mapping(target = "reactions", source = "reactionsList")
    @Mapping(target = "mentionedUsers", ignore = true)
    Comment mapToComment(com.facebook.feed.grpc.Comment grpcComment);

    @Mapping(target = "reactionType", source = "reactionType")
    @Mapping(target = "userIds", source = "userIdsList")
    ReactionDetail mapToReactionDetail(com.facebook.feed.grpc.ReactionDetail grpcReaction);

    // Jawna metoda budująca zapytanie gRPC dla Eventu bez walki z MapStructem i builderami Protobufa
    default CreateEventRequest buildCreateEventRequest(CreateEventInput input) {
        CreateEventRequest.Builder reqBuilder = CreateEventRequest.newBuilder()
                .setUserId(input.getUserId())
                .setName(input.getName())
                .setStartDate(input.getStartDate())
                .setType(input.getType())
                .setPrivacy(input.getPrivacy());

        applyIfPresent(input.getId(), reqBuilder::setId);
        applyIfPresent(input.getTitle(), reqBuilder::setTitle);
        applyIfPresent(input.getStartTime(), reqBuilder::setStartTime);
        applyIfPresent(input.getEndDate(), reqBuilder::setEndDate);
        applyIfPresent(input.getEndTime(), reqBuilder::setEndTime);
        applyIfPresent(input.getDescription(), reqBuilder::setDescription);
        applyIfPresent(input.getImages(), reqBuilder::addAllImages);
        applyIfPresent(input.getLocation(), reqBuilder::setLocation);
        applyIfPresent(input.getLocationName(), reqBuilder::setLocationName);
        applyIfPresent(input.getAddress(), reqBuilder::setAddress);
        applyIfPresent(input.getShowGuestList(), reqBuilder::setShowGuestList);
        applyIfPresent(input.getHosts(), reqBuilder::addAllHosts);
        applyIfPresent(input.getDate(), reqBuilder::setDate);
        applyIfPresent(input.getCoordinates(), reqBuilder::addAllCoordinates);
        applyIfPresent(input.getFrequency(), reqBuilder::setFrequency);

        return reqBuilder.build();
    }

    default <T> void applyIfPresent(T value, Consumer<T> setter) {
        if (value != null) {
            setter.accept(value);
        }
    }
}