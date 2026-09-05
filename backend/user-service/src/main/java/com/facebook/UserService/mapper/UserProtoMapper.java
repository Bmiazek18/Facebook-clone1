package com.facebook.UserService.mapper;

import com.facebook.UserService.model.User;
import com.facebook.user.grpc.UpdateProfileRequest;
import com.facebook.user.grpc.UserDto;

import java.util.List;
import java.util.stream.Collectors;

public final class UserProtoMapper {

    private UserProtoMapper() {
    }

    public static UserDto toUserDto(User user) {
        return toUserDto(user, false, 0);
    }

    public static UserDto toUserDto(User user, boolean inHistory, int newPostsCount) {
        if (user == null) {
            return UserDto.getDefaultInstance();
        }

        List<String> lifeEvents = List.of();
        try {
            if (user.getLifeEvents() != null) {
                lifeEvents = user.getLifeEvents().stream()
                        .map(le -> le.getDate() + "," + le.getEvent())
                        .collect(Collectors.toList());
            }
        } catch (Exception ignored) {}

        List<String> socialLinks = List.of();
        try {
            if (user.getSocialLinks() != null) {
                socialLinks = user.getSocialLinks().stream()
                        .map(sl -> sl.getName() + "," + sl.getUrl())
                        .collect(Collectors.toList());
            }
        } catch (Exception ignored) {}

        UserDto.Builder builder = UserDto.newBuilder()
                .setId(String.valueOf(user.getId()))
                .setFirstName(nullToEmpty(user.getFirstName()))
                .setLastName(nullToEmpty(user.getLastName()))
                .setAvatarId(nullToEmpty(user.getAvatarId()))
                .setUsername(nullToEmpty(user.getUsername()))
                .setBio(nullToEmpty(user.getBio()))
                .setLocation(nullToEmpty(user.getLocation()))
                .setHometown(nullToEmpty(user.getHometown()))
                .setSchool(nullToEmpty(user.getSchool()))
                .setHighSchool(nullToEmpty(user.getHighSchool()))
                .setEducation(nullToEmpty(user.getEducation()))
                .setJob(nullToEmpty(user.getJob()))
                .setCompany(nullToEmpty(user.getCompany()))
                .setPhone(nullToEmpty(user.getPhone()))
                .setWebsite(nullToEmpty(user.getWebsite()))
                .setGender(nullToEmpty(user.getGender()))
                .setBirthDate(nullToEmpty(user.getBirthDate()))
                .setLanguages(nullToEmpty(user.getLanguages()))
                .setPronouns(nullToEmpty(user.getPronouns()))
                .setRelationshipStatus(nullToEmpty(user.getRelationshipStatus()))
                .setRelationshipSince(nullToEmpty(user.getRelationshipSince()))
                .setPartnerName(nullToEmpty(user.getPartnerName()))
                .setPartnerAvatar(nullToEmpty(user.getPartnerAvatar()))
                .setBioDetails(nullToEmpty(user.getBioDetails()))
                .setNamePronounciation(nullToEmpty(user.getNamePronounciation()))
                .addAllLifeEvents(lifeEvents)
                .addAllSocialLinks(socialLinks)
                .setInHistory(inHistory)
                .setNewPostsCount(newPostsCount)
                .setCoverId(nullToEmpty(user.getCoverPhotoId()));

        try {
            if (user.getOtherNames() != null) {
                builder.addAllOtherNames(user.getOtherNames());
            }
        } catch (Exception ignored) {}
        try {
            if (user.getFavoriteQuotes() != null) {
                builder.addAllFavoriteQuotes(user.getFavoriteQuotes());
            }
        } catch (Exception ignored) {}
        if (user.getCreatedAt() != null) {
            builder.setCreatedAt(user.getCreatedAt().toString());
        }
        if (user.getUpdatedAt() != null) {
            builder.setUpdatedAt(user.getUpdatedAt().toString());
        }

        if (user.getNote() != null && user.getNoteCreatedAt() != null) {
            if (user.getNoteCreatedAt().isAfter(java.time.LocalDateTime.now().minusHours(24))) {
                builder.setNote(user.getNote());
            }
        }

        return builder.build();
    }

    public static void applyUpdateProfileRequest(User user, UpdateProfileRequest request) {
        if (request.hasBio()) {
            user.setBio(request.getBio());
        }
        if (request.hasLocation()) {
            user.setLocation(request.getLocation());
        }
        if (request.hasHometown()) {
            user.setHometown(request.getHometown());
        }
        if (request.hasSchool()) {
            user.setSchool(request.getSchool());
        }
        if (request.hasHighSchool()) {
            user.setHighSchool(request.getHighSchool());
        }
        if (request.hasEducation()) {
            user.setEducation(request.getEducation());
        }
        if (request.hasJob()) {
            user.setJob(request.getJob());
        }
        if (request.hasCompany()) {
            user.setCompany(request.getCompany());
        }
        if (request.hasPhone()) {
            user.setPhone(request.getPhone());
        }
        if (request.hasWebsite()) {
            user.setWebsite(request.getWebsite());
        }
        if (request.hasGender()) {
            user.setGender(request.getGender());
        }
        if (request.hasBirthDate()) {
            user.setBirthDate(request.getBirthDate());
        }
        if (request.hasLanguages()) {
            user.setLanguages(request.getLanguages());
        }
        if (request.hasPronouns()) {
            user.setPronouns(request.getPronouns());
        }
        if (request.hasNote()) {
            user.setNote(request.getNote());
            user.setNoteCreatedAt(java.time.LocalDateTime.now());
        }
    }

    private static String nullToEmpty(String value) {
        return value != null ? value : "";
    }
}
