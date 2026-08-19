package com.facebook.UserEdgeService.mapper;

import com.facebook.user.generated.types.*;
import com.facebook.user.grpc.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;
import java.util.Map;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface EdgeMapper {

    ObjectMapper objectMapper = new ObjectMapper();

    @Mapping(target = "city", source = "location")
    @Mapping(target = "work", expression = "java(computeWork(u.getJob(), u.getCompany()))")
    @Mapping(target = "languages", expression = "java(computeLanguages(u.getLanguages()))")
    UserSearchResponse grpcUserToDgsUser(UserDto u);

    Listing grpcListingToDgsListing(ListingDto l);

    @Mapping(target = "sender", ignore = true)
    Notification grpcNotificationToDgsNotification(NotificationDto n);

    default CreateListingRequest buildCreateListingRequest(CreateListingInput input) {
        return CreateListingRequest.newBuilder()
                .setTitle(input.getTitle())
                .setPrice(input.getPrice())
                .setCategory(input.getCategory())
                .setCondition(input.getCondition())
                .setDescription(input.getDescription() != null ? input.getDescription() : "")
                .setLatitude(input.getLatitude())
                .setLongitude(input.getLongitude())
                .build();
    }

    default UpdateProfileRequest buildUpdateProfileRequest(String userId, UpdateProfileInput input) {
        UpdateProfileRequest.Builder builder = UpdateProfileRequest.newBuilder().setUserId(userId);

        if (input.getBio() != null) {
            builder.setBio(input.getBio());
        }
        if (input.getLocation() != null) {
            builder.setLocation(input.getLocation());
        } else if (input.getCity() != null) {
            builder.setLocation(input.getCity());
        }
        if (input.getHometown() != null) {
            builder.setHometown(input.getHometown());
        }
        if (input.getSchool() != null) {
            builder.setSchool(input.getSchool());
        }
        if (input.getHighSchool() != null) {
            builder.setHighSchool(input.getHighSchool());
        }
        if (input.getEducation() != null) {
            builder.setEducation(input.getEducation());
        }
        if (input.getJob() != null) {
            builder.setJob(input.getJob());
        } else if (input.getWork() != null) {
            builder.setJob(input.getWork());
        }
        if (input.getCompany() != null) {
            builder.setCompany(input.getCompany());
        }
        if (input.getPhone() != null) {
            builder.setPhone(input.getPhone());
        }
        if (input.getWebsite() != null) {
            builder.setWebsite(input.getWebsite());
        }
        if (input.getGender() != null) {
            builder.setGender(input.getGender());
        }
        if (input.getBirthDate() != null) {
            builder.setBirthDate(input.getBirthDate());
        }
        if (input.getLanguages() != null) {
            builder.setLanguages(String.join(",", input.getLanguages()));
        }
        if (input.getPronouns() != null) {
            builder.setPronouns(input.getPronouns());
        }
        if (input.getNote() != null) {
            builder.setNote(input.getNote());
        }

        return builder.build();
    }

    default String mapToJson(Map<String, String> map) {
        if (map == null || map.isEmpty()) return "{}";
        try {
            return objectMapper.writeValueAsString(map);
        } catch (Exception e) {
            return "{}";
        }
    }

    default String computeWork(String job, String company) {
        return (job != null && !job.isEmpty())
                ? job + (company != null && !company.isEmpty() ? " w " + company : "")
                : company;
    }

    default List<String> computeLanguages(String langs) {
        return (langs != null && !langs.isEmpty())
                ? List.of(langs.split(","))
                : List.of();
    }
}
