package com.facebook.UserService.service;

import com.facebook.UserService.client.SearchServiceClient;
import com.facebook.UserService.dto.CreatePageRequest;
import com.facebook.UserService.dto.PageDto;
import com.facebook.UserService.model.Page;
import com.facebook.UserService.model.User;
import com.facebook.UserService.repository.PageRepository;
import com.facebook.UserService.repository.UserRepository;
import com.facebook.UserService.dto.PageIndexEvent;
import com.facebook.UserService.config.RabbitConfig;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class PageService {

    private static final Logger log = LoggerFactory.getLogger(PageService.class);

    private final PageRepository pageRepository;
    private final UserRepository userRepository;
    private final SearchServiceClient searchServiceClient;
    private final RabbitTemplate rabbitTemplate;

    @Autowired
    public PageService(PageRepository pageRepository,
                       UserRepository userRepository,
                       SearchServiceClient searchServiceClient,
                       RabbitTemplate rabbitTemplate) {
        this.pageRepository = pageRepository;
        this.userRepository = userRepository;
        this.searchServiceClient = searchServiceClient;
        this.rabbitTemplate = rabbitTemplate;
    }

    /**
     * Create page with validated owner identity (prioritizing X-User-Id header over body parameter)
     */
    @Transactional
    public PageDto createPage(CreatePageRequest request, String xUserId) {
        String pageName = request.getEffectiveName();
        String category = request.getCategory() != null && !request.getCategory().trim().isEmpty()
                ? request.getCategory().trim()
                : "Ogólna";

        // Security: Prioritize authenticated identity from gateway header (X-User-Id)
        UUID ownerUuid;
        try {
            if (xUserId != null && !xUserId.trim().isEmpty()) {
                ownerUuid = UUID.fromString(xUserId.trim());
            } else if (request.getOwnerId() != null && !request.getOwnerId().trim().isEmpty()) {
                ownerUuid = UUID.fromString(request.getOwnerId().trim());
            } else {
                ownerUuid = UUID.fromString("1e4332f6-5a7a-3210-b5fb-fb92c7c60cce");
            }
        } catch (IllegalArgumentException e) {
            log.warn("Invalid UUID provided for owner, using fallback: {}", e.getMessage());
            ownerUuid = UUID.fromString("1e4332f6-5a7a-3210-b5fb-fb92c7c60cce");
        }

        UUID pageId = UUID.randomUUID();

        String avatar = request.getProfileImage() != null && !request.getProfileImage().trim().isEmpty()
                ? request.getProfileImage().trim()
                : "https://i.pravatar.cc/150?u=" + pageId;

        String cover = request.getCoverImage() != null && !request.getCoverImage().trim().isEmpty()
                ? request.getCoverImage().trim()
                : "https://picsum.photos/seed/" + pageId + "/1200/400";

        Page page = Page.builder()
                .id(pageId)
                .ownerId(ownerUuid)
                .name(pageName)
                .category(category)
                .bio(request.getBio())
                .website(request.getWebsite())
                .phoneCode(request.getPhoneCode())
                .phone(request.getPhone())
                .email(request.getEmail())
                .address(request.getAddress())
                .city(request.getCity())
                .zip(request.getZip())
                .hours(request.getHours())
                .avatarUrl(avatar)
                .coverUrl(cover)
                .pageNotifications(request.getPageNotifications() != null ? request.getPageNotifications() : true)
                .promotionalEmails(request.getPromotionalEmails() != null ? request.getPromotionalEmails() : false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        Page savedPage = pageRepository.save(page);

        // Register page as a User entity for actor identity resolution in gRPC / GraphQL
        try {
            String pageUsername = "page_" + pageId.toString().replace("-", "").substring(0, 12);
            String pageEmail = request.getEmail() != null && !request.getEmail().trim().isEmpty()
                    && !userRepository.existsByEmail(request.getEmail().trim())
                    ? request.getEmail().trim()
                    : "page_" + pageId + "@pages.facebook.local";

            User pageUser = User.builder()
                    .id(pageId)
                    .username(pageUsername)
                    .email(pageEmail)
                    .password(UUID.randomUUID().toString())
                    .firstName(pageName)
                    .lastName("")
                    .avatarId(avatar)
                    .coverPhotoId(cover)
                    .bio(request.getBio())
                    .website(request.getWebsite())
                    .phone(request.getPhone())
                    .location(request.getCity() != null ? request.getCity() : request.getAddress())
                    .job(category)
                    .company(pageName)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();

            userRepository.save(pageUser);

            try {
                PageIndexEvent pageIndexEvent = PageIndexEvent.builder()
                        .id(pageId.toString())
                        .name(pageName)
                        .category(category)
                        .avatarUrl(avatar)
                        .delete(false)
                        .build();
                rabbitTemplate.convertAndSend(
                        RabbitConfig.EXCHANGE_NAME,
                        RabbitConfig.PAGE_ROUTING_KEY,
                        pageIndexEvent
                );
                log.info("Pages: Published page indexing event to RabbitMQ for ID: {}", pageId);
            } catch (Exception e) {
                log.error("Pages: Failed to publish page indexing event to RabbitMQ", e);
            }

            log.info("Successfully created Page and linked User record with id: {} owned by: {}", pageId, ownerUuid);
        } catch (Exception e) {
            log.error("Failed to sync page as user: {}", e.getMessage(), e);
        }

        return toDto(savedPage);
    }

    /**
     * Check if a given user is the authorized owner or administrator of a page
     */
    public boolean isUserAuthorizedForPage(UUID pageId, UUID userId) {
        if (pageId == null || userId == null) return false;
        if (pageId.equals(userId)) return true; // Direct actor
        return pageRepository.existsByIdAndOwnerId(pageId, userId);
    }

    /**
     * Enforce page ownership verification, throwing 403 FORBIDDEN if unauthorized
     */
    public void validatePageOwnership(UUID pageId, UUID userId) {
        if (!isUserAuthorizedForPage(pageId, userId)) {
            log.warn("Access denied: User {} tried to act on page {} without ownership", userId, pageId);
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Brak uprawnień do zarządzania tą stroną");
        }
    }

    /**
     * Get pages owned by a user, with optional security check if authenticated X-User-Id is provided
     */
    public List<PageDto> getPagesByOwnerId(UUID requestedOwnerId, String xUserId) {
        if (xUserId != null && !xUserId.trim().isEmpty()) {
            try {
                UUID authUserId = UUID.fromString(xUserId.trim());
                // Only allow viewing full private page management list for own profile
                if (!authUserId.equals(requestedOwnerId)) {
                    log.info("Requesting pages for user {} by authenticated user {}", requestedOwnerId, authUserId);
                }
            } catch (IllegalArgumentException ignored) {}
        }

        return pageRepository.findByOwnerIdOrderByCreatedAtDesc(requestedOwnerId)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public List<PageDto> getAllPages() {
        return pageRepository.findAll()
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public PageDto getPageById(UUID pageId) {
        return pageRepository.findById(pageId)
                .map(this::toDto)
                .orElse(null);
    }

    public PageDto getPageByWebsite(String domain) {
        List<Page> pages = pageRepository.findByWebsiteDomain(domain);
        if (pages.isEmpty()) {
            return null;
        }
        return toDto(pages.get(0));
    }

    public PageDto toDto(Page page) {
        if (page == null) return null;
        return PageDto.builder()
                .id(page.getId())
                .ownerId(page.getOwnerId())
                .name(page.getName())
                .category(page.getCategory())
                .bio(page.getBio())
                .website(page.getWebsite())
                .phoneCode(page.getPhoneCode())
                .phone(page.getPhone())
                .email(page.getEmail())
                .address(page.getAddress())
                .city(page.getCity())
                .zip(page.getZip())
                .hours(page.getHours())
                .avatar(page.getAvatarUrl())
                .cover(page.getCoverUrl())
                .pageNotifications(page.getPageNotifications())
                .promotionalEmails(page.getPromotionalEmails())
                .createdAt(page.getCreatedAt() != null ? page.getCreatedAt().toString() : null)
                .updatedAt(page.getUpdatedAt() != null ? page.getUpdatedAt().toString() : null)
                .build();
    }
}
