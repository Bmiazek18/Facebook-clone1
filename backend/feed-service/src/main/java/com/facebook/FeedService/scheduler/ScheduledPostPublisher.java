package com.facebook.FeedService.scheduler;

import com.facebook.FeedService.entity.PostEntity;
import com.facebook.FeedService.repository.PostRepository;
import com.facebook.FeedService.service.UserMediaService;
import com.facebook.FeedService.util.MentionHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

@Component
@Slf4j
public class ScheduledPostPublisher {

    private static final String SCHEDULED_POSTS_ZSET_KEY = "posts:scheduled:zset";

    private final StringRedisTemplate redisTemplate;
    private final PostRepository postRepository;
    private final UserMediaService userMediaService;
    private final MentionHelper mentionHelper;
    private final ThreadPoolTaskScheduler taskScheduler;

    private final Map<String, ScheduledFuture<?>> scheduledFutures = new ConcurrentHashMap<>();

    public ScheduledPostPublisher(
            StringRedisTemplate redisTemplate,
            PostRepository postRepository,
            UserMediaService userMediaService,
            MentionHelper mentionHelper,
            @Qualifier("postTaskScheduler") ThreadPoolTaskScheduler taskScheduler) {
        this.redisTemplate = redisTemplate;
        this.postRepository = postRepository;
        this.userMediaService = userMediaService;
        this.mentionHelper = mentionHelper;
        this.taskScheduler = taskScheduler;
    }

    /**
     * Schedules a post to be published exactly at the target timestamp.
     * No polling loop - scheduled directly on ThreadPoolTaskScheduler.
     */
    public void enqueue(String postId, long scheduledPublishTimestamp) {
        Instant targetInstant = Instant.ofEpochMilli(scheduledPublishTimestamp);
        log.info("Precision Scheduler: Enqueuing post {} for exact execution at {}", postId, targetInstant);

        try {
            // Persist in Redis ZSet for cross-node replication and recovery
            redisTemplate.opsForZSet().add(SCHEDULED_POSTS_ZSET_KEY, postId, (double) scheduledPublishTimestamp);
        } catch (Exception e) {
            log.error("Failed to persist scheduled post {} to Redis", postId, e);
        }

        // Cancel previous timer if exists
        ScheduledFuture<?> existing = scheduledFutures.remove(postId);
        if (existing != null && !existing.isDone()) {
            existing.cancel(false);
        }

        if (targetInstant.isBefore(Instant.now())) {
            // Target time has already passed, publish immediately in background thread
            taskScheduler.execute(() -> publishNow(postId));
        } else {
            // Schedule high-precision execution on OS timer
            ScheduledFuture<?> future = taskScheduler.schedule(() -> publishNow(postId), targetInstant);
            scheduledFutures.put(postId, future);
        }
    }

    /**
     * Cancels scheduled execution both in memory and in Redis.
     */
    public void cancel(String postId) {
        log.info("Precision Scheduler: Canceling scheduled post {}", postId);
        ScheduledFuture<?> future = scheduledFutures.remove(postId);
        if (future != null && !future.isDone()) {
            future.cancel(false);
        }
        try {
            redisTemplate.opsForZSet().remove(SCHEDULED_POSTS_ZSET_KEY, postId);
        } catch (Exception e) {
            log.error("Failed to remove scheduled post {} from Redis", postId, e);
        }
    }

    /**
     * On application startup, load all pending scheduled posts from DB & Redis
     * and register their precise timer executions.
     */
    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        log.info("Precision Scheduler: Initializing pending scheduled posts from database and Redis...");
        try {
            List<PostEntity> scheduledPosts = postRepository.findAll().stream()
                    .filter(p -> "SCHEDULED".equalsIgnoreCase(p.getStatus()))
                    .toList();

            log.info("Precision Scheduler: Found {} pending scheduled post(s) to schedule", scheduledPosts.size());

            long now = System.currentTimeMillis();
            for (PostEntity post : scheduledPosts) {
                long publishTime = post.getScheduledPublishTime() != null ? post.getScheduledPublishTime() : post.getTimestamp();
                enqueue(post.getId(), publishTime);
            }
        } catch (Exception e) {
            log.error("Failed to recover scheduled posts during startup", e);
        }
    }

    /**
     * Exact-time publisher worker executed by ThreadPoolTaskScheduler.
     */
    public void publishNow(String postId) {
        scheduledFutures.remove(postId);
        log.info("Precision Scheduler: Triggered exact publishing for post {}", postId);

        try {
            // Atomically remove from Redis ZSet
            redisTemplate.opsForZSet().remove(SCHEDULED_POSTS_ZSET_KEY, postId);

            postRepository.findById(postId).ifPresent(post -> {
                if ("SCHEDULED".equalsIgnoreCase(post.getStatus())) {
                    post.setStatus("ACTIVE");
                    post.setDate(Instant.now().toString());
                    post.setTimestamp(System.currentTimeMillis());
                    postRepository.save(post);

                    userMediaService.indexPostMedia(post);

                    try {
                        mentionHelper.sendMentionNotifications(post.getMentionedUserIds(), post.getAuthorId(), "swoim poście");
                    } catch (Exception ex) {
                        log.error("Failed to send mention notifications on scheduled post activation for {}", post.getId(), ex);
                    }

                    log.info("Precision Scheduler: Post {} successfully published and is now ACTIVE", postId);
                }
            });
        } catch (Exception e) {
            log.error("Error during precision activation of post {}", postId, e);
        }
    }
}
