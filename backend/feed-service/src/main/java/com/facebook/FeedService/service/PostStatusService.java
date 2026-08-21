package com.facebook.FeedService.service;

import com.facebook.FeedService.entity.PostEntity;
import com.facebook.FeedService.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PostStatusService {

    private final PostRepository postRepository;

    public boolean updatePostStatus(String postId, String status) {
        if (status == null || status.trim().isEmpty()) {
            return false;
        }

        String newStatus = status.trim().toUpperCase();
        log.info("Updating post {} status to {}", postId, newStatus);

        Optional<PostEntity> postOpt = postRepository.findById(postId);
        if (postOpt.isEmpty()) {
            log.warn("Post {} not found for status update", postId);
            return false;
        }

        PostEntity post = postOpt.get();
        String currentStatus = post.getStatus() != null ? post.getStatus() : "ACTIVE";

        if ("REJECTED".equalsIgnoreCase(newStatus)
                || "HIDDEN".equalsIgnoreCase(newStatus)
                || "NOT_VISIBLE".equalsIgnoreCase(newStatus)) {
            post.setStatus(newStatus);
        } else if ("ACTIVE".equalsIgnoreCase(newStatus)) {
            if (!"RENDERING".equalsIgnoreCase(currentStatus)) {
                post.setStatus("ACTIVE");
            } else {
                log.info("Ignoring ACTIVE status for post {} because it is still RENDERING", postId);
            }
        } else {
            post.setStatus(newStatus);
        }

        postRepository.save(post);
        log.info("Post {} status is now {}", postId, post.getStatus());
        return true;
    }

    public int updatePostStatusByMedia(String fileId, String status) {
        if (status == null || status.trim().isEmpty()) {
            return 0;
        }

        String newStatus = status.trim().toUpperCase();
        log.info("Updating posts for media {} to status {}", fileId, newStatus);

        List<PostEntity> posts = postRepository.findByMediaFileId(fileId);
        int updated = 0;

        for (PostEntity post : posts) {
            String currentStatus = post.getStatus() != null ? post.getStatus() : "ACTIVE";

            if ("ACTIVE".equalsIgnoreCase(newStatus)) {
                if ("RENDERING".equalsIgnoreCase(currentStatus)) {
                    post.setStatus("ACTIVE");
                    log.info("Post {} status updated from RENDERING to ACTIVE", post.getId());
                    updated++;
                } else {
                    log.info("Ignoring ACTIVE status for post {} because current status is {}", post.getId(), currentStatus);
                }
            } else {
                post.setStatus(newStatus);
                updated++;
            }
            postRepository.save(post);
        }

        return updated;
    }
}
