package com.facebook.FeedService.service;

import com.facebook.FeedService.entity.PostEntity;
import com.facebook.FeedService.entity.UserMediaEntity;
import com.facebook.FeedService.repository.PostRepository;
import com.facebook.FeedService.repository.UserMediaRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserMediaService {

    private final UserMediaRepository userMediaRepository;
    private final PostRepository postRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public record UserAlbumDto(String name, long count, String coverUrl) {}

    public record UserMediaPageDto(List<UserMediaEntity> items, long totalCount, boolean hasMore) {}

    /**
     * Index media for a single post
     */
    @Transactional
    public void indexPostMedia(PostEntity post) {
        if (post == null || post.getMediaJson() == null || post.getMediaJson().trim().isEmpty()) {
            return;
        }

        try {
            // First remove existing indexed media for this post to avoid duplicates on edits
            userMediaRepository.deleteByPostId(post.getId());

            List<Map<String, Object>> mediaList = objectMapper.readValue(
                    post.getMediaJson(), new TypeReference<List<Map<String, Object>>>() {}
            );

            if (mediaList == null || mediaList.isEmpty()) {
                return;
            }

            String albumName = resolveAlbumName(post);

            List<UserMediaEntity> toSave = new ArrayList<>();
            for (Map<String, Object> m : mediaList) {
                String src = (String) m.get("src");
                if (src == null || src.isEmpty()) continue;

                String altText = (String) m.get("altText");
                String mediaType = isVideoUrl(src) ? "VIDEO" : "IMAGE";

                UserMediaEntity mediaEntity = UserMediaEntity.builder()
                        .userId(post.getAuthorId())
                        .postId(post.getId())
                        .mediaUrl(src)
                        .mediaType(mediaType)
                        .albumName(albumName)
                        .altText(altText)
                        .timestamp(post.getTimestamp() != null ? post.getTimestamp() : System.currentTimeMillis())
                        .createdAt(post.getDate() != null ? Instant.parse(post.getDate()) : Instant.now())
                        .build();

                toSave.add(mediaEntity);
            }

            if (!toSave.isEmpty()) {
                userMediaRepository.saveAll(toSave);
                log.info("Indexed {} media items for post {} of user {}", toSave.size(), post.getId(), post.getAuthorId());
            }
        } catch (Exception e) {
            log.error("Failed to index media for post {}", post.getId(), e);
        }
    }

    /**
     * Get paginated media items by filter: ALL, PHOTOS, VIDEOS, TAGGED, ALBUM
     */
    public UserMediaPageDto getUserMedia(String userId, String filter, String albumName, int limit, int offset) {
        int page = limit > 0 ? offset / limit : 0;
        int pageSize = limit > 0 ? limit : 20;
        Pageable pageable = PageRequest.of(page, pageSize);

        String normalizedFilter = filter != null ? filter.toUpperCase() : "ALL";

        if ("TAGGED".equals(normalizedFilter)) {
            return getTaggedMedia(userId, pageable);
        }

        Page<UserMediaEntity> result;
        if ("PHOTOS".equals(normalizedFilter)) {
            result = userMediaRepository.findByUserIdAndMediaTypeOrderByCreatedAtDesc(userId, "IMAGE", pageable);
        } else if ("VIDEOS".equals(normalizedFilter)) {
            result = userMediaRepository.findByUserIdAndMediaTypeOrderByCreatedAtDesc(userId, "VIDEO", pageable);
        } else if ("ALBUM".equals(normalizedFilter) && albumName != null && !albumName.isEmpty()) {
            result = userMediaRepository.findByUserIdAndAlbumNameOrderByCreatedAtDesc(userId, albumName, pageable);
        } else {
            result = userMediaRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
        }

        return new UserMediaPageDto(
                result.getContent(),
                result.getTotalElements(),
                result.hasNext()
        );
    }

    /**
     * Get media from posts where the user is tagged
     */
    public UserMediaPageDto getTaggedMedia(String userId, Pageable pageable) {
        List<PostEntity> taggedPosts = postRepository.findByTaggedUserId(userId);
        List<UserMediaEntity> taggedMedia = new ArrayList<>();

        for (PostEntity post : taggedPosts) {
            if (post.getMediaJson() != null && !post.getMediaJson().trim().isEmpty()) {
                try {
                    List<Map<String, Object>> mediaList = objectMapper.readValue(
                            post.getMediaJson(), new TypeReference<List<Map<String, Object>>>() {}
                    );
                    if (mediaList != null) {
                        for (Map<String, Object> m : mediaList) {
                            String src = (String) m.get("src");
                            if (src != null && !src.isEmpty()) {
                                String mediaType = isVideoUrl(src) ? "VIDEO" : "IMAGE";
                                taggedMedia.add(UserMediaEntity.builder()
                                        .userId(post.getAuthorId())
                                        .postId(post.getId())
                                        .mediaUrl(src)
                                        .mediaType(mediaType)
                                        .albumName("Zdjęcia z Twoim oznaczeniem")
                                        .altText((String) m.get("altText"))
                                        .timestamp(post.getTimestamp())
                                        .createdAt(post.getDate() != null ? Instant.parse(post.getDate()) : Instant.now())
                                        .build());
                            }
                        }
                    }
                } catch (Exception ignored) {}
            }
        }

        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), taggedMedia.size());
        List<UserMediaEntity> paged = start <= taggedMedia.size() ? taggedMedia.subList(start, end) : Collections.emptyList();
        boolean hasMore = end < taggedMedia.size();

        return new UserMediaPageDto(paged, taggedMedia.size(), hasMore);
    }

    /**
     * Get list of albums for user with photo count and cover
     */
    public List<UserAlbumDto> getUserAlbums(String userId) {
        List<String> albumNames = userMediaRepository.findDistinctAlbumNamesByUserId(userId);
        Set<String> allAlbums = new LinkedHashSet<>(List.of("Zdjęcia profilowe", "Zdjęcia w tle", "Oś czasu"));
        allAlbums.addAll(albumNames);

        List<UserAlbumDto> albums = new ArrayList<>();
        for (String name : allAlbums) {
            long count = userMediaRepository.countByUserIdAndAlbumName(userId, name);
            if (count > 0 || "Oś czasu".equals(name) || "Zdjęcia profilowe".equals(name)) {
                UserMediaEntity cover = userMediaRepository.findFirstByUserIdAndAlbumNameOrderByCreatedAtDesc(userId, name);
                String coverUrl = cover != null ? cover.getMediaUrl() : null;
                albums.add(new UserAlbumDto(name, count, coverUrl));
            }
        }
        return albums;
    }

    private String resolveAlbumName(PostEntity post) {
        if ("profile_photo".equalsIgnoreCase(post.getTargetType())) {
            return "Zdjęcia profilowe";
        } else if ("cover_photo".equalsIgnoreCase(post.getTargetType())) {
            return "Zdjęcia w tle";
        }
        return "Oś czasu";
    }

    private boolean isVideoUrl(String url) {
        if (url == null) return false;
        String lower = url.toLowerCase();
        return lower.endsWith(".mp4") || lower.endsWith(".webm") || lower.endsWith(".mov") || lower.contains("/video/") || lower.contains("video_abr");
    }

    /**
     * Backfill user_media on startup if table is empty
     */
    @PostConstruct
    public void backfillIfEmpty() {
        try {
            if (userMediaRepository.count() == 0) {
                log.info("user_media table is empty. Starting backfill from existing posts...");
                List<PostEntity> allPosts = postRepository.findAll();
                for (PostEntity post : allPosts) {
                    indexPostMedia(post);
                }
                log.info("Finished user_media backfill for {} posts.", allPosts.size());
            }
        } catch (Exception e) {
            log.warn("Backfill user_media failed or deferred: {}", e.getMessage());
        }
    }
}
