package com.facebook.UserService.service;

import com.facebook.UserService.client.SearchServiceClient;
import com.facebook.UserService.client.SocialGraphClient;
import com.facebook.UserService.dto.RegisterRequest;
import com.facebook.UserService.dto.RegisterResponse;
import com.facebook.UserService.mapper.UserProtoMapper;
import com.facebook.UserService.model.SearchUser;
import com.facebook.UserService.model.User;
import com.facebook.UserService.repository.SearchUserRepository;
import com.facebook.UserService.repository.UserRepository;
import com.facebook.socialgraph.grpc.UserRelation;
import com.facebook.search.grpc.SearchUserHit;
import com.facebook.user.grpc.UpdateProfileRequest;
import com.facebook.user.grpc.UserDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final SearchUserRepository searchUserRepository;
    private final SocialGraphClient socialGraphClient;
    private final SearchServiceClient searchServiceClient;
    private final MinioService minioService;
    private final MediaUrlService mediaUrlService;

    @net.devh.boot.grpc.client.inject.GrpcClient("social-graph-service")
    private com.facebook.socialgraph.grpc.SocialGraphGrpcServiceGrpc.SocialGraphGrpcServiceBlockingStub socialGraphGrpcStub;

    @Autowired
    public UserService(UserRepository userRepository,
                       SearchUserRepository searchUserRepository,
                       SocialGraphClient socialGraphClient,
                       SearchServiceClient searchServiceClient,
                       MinioService minioService,
                       MediaUrlService mediaUrlService) {
        this.userRepository = userRepository;
        this.searchUserRepository = searchUserRepository;
        this.socialGraphClient = socialGraphClient;
        this.searchServiceClient = searchServiceClient;
        this.minioService = minioService;
        this.mediaUrlService = mediaUrlService;
    }

    @Transactional
    public RegisterResponse registerUser(RegisterRequest request) {
        if (request.getUsername() == null || request.getUsername().trim().isEmpty()) {
            throw new IllegalArgumentException("Username is required");
        }
        if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("Email is required");
        }

        String username = request.getUsername().trim();
        String email = request.getEmail().trim();

        if (userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("Username is already taken");
        }
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email is already taken");
        }

        UUID userId = null;
        if (request.getUserId() != null && !request.getUserId().trim().isEmpty()) {
            userId = UUID.fromString(request.getUserId());
        }

        User user = User.builder()
                .id(userId)
                .username(username)
                .email(email)
                .password(request.getPassword())
                .firstName(request.getFirstName() != null ? request.getFirstName().trim() : null)
                .lastName(request.getLastName() != null ? request.getLastName().trim() : null)
                .build();

        User savedUser = userRepository.save(user);

        syncUserToSearchService(savedUser);
        createSocialGraphNode(savedUser, request.getBirthDate());

        return RegisterResponse.builder()
                .id(savedUser.getId())
                .username(savedUser.getUsername())
                .email(savedUser.getEmail())
                .firstName(savedUser.getFirstName())
                .lastName(savedUser.getLastName())
                .message("User registered successfully")
                .build();
    }

    private void syncUserToSearchService(User user) {
        try {
            searchServiceClient.indexUser(
                    user.getId().toString(),
                    user.getUsername(),
                    user.getFirstName(),
                    user.getLastName(),
                    user.getAvatarId()
            );
            log.info("Successfully synced user to search-service via gRPC: {}", user.getId());
        } catch (Exception e) {
            log.error("Failed to sync user to search-service via gRPC: {}", e.getMessage(), e);
        }
    }

    private void createSocialGraphNode(User user, String birthDate) {
        try {
            com.facebook.socialgraph.grpc.CreateNodeRequest grpcRequest = com.facebook.socialgraph.grpc.CreateNodeRequest.newBuilder()
                    .setUserId(String.valueOf(user.getId()))
                    .setUsername(user.getUsername())
                    .setBirthDate(birthDate != null ? birthDate : "")
                    .build();
            socialGraphGrpcStub.createUserNode(grpcRequest);
            log.info("Successfully created Social Graph node for user: {}", user.getId());
        } catch (Exception e) {
            log.error("Failed to create user node in SocialGraph via gRPC: {}", e.getMessage(), e);
        }
    }

    @Transactional
    public UserDto searchUserById(UUID searchedUserId, UUID searchingUserId) {
        if (searchedUserId == null) {
            throw new IllegalArgumentException("User ID query parameter is required");
        }

        User searchedUser = userRepository.findById(searchedUserId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + searchedUserId));

        SearchUser searchUser;
        if (searchingUserId != null) {
            User searchingUser = userRepository.findById(searchingUserId).orElse(null);
            searchUser = searchUserRepository.findBySearchingUserIdAndSearchedUserId(searchingUserId, searchedUserId)
                    .orElseGet(() -> SearchUser.builder()
                            .searchingUser(searchingUser)
                            .searchedUser(searchedUser)
                            .build());
        } else {
            searchUser = searchUserRepository.findBySearchedUser(searchedUser)
                    .orElseGet(() -> SearchUser.builder()
                            .searchedUser(searchedUser)
                            .build());
        }

        searchUser.setLastSearchedAt(LocalDateTime.now());
        searchUserRepository.save(searchUser);

        return mapToUserDto(searchedUser);
    }

    @Transactional(readOnly = true)
    public UserDto getUserProfileById(UUID userId) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID query parameter is required");
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));
        return mapToUserDto(user);
    }

    @Transactional(readOnly = true)
    public java.util.List<UserDto> searchUsers(String query, UUID currentUserId) {
        if (query == null || query.trim().isEmpty()) {
            return java.util.Collections.emptyList();
        }

        record UserSearchResult(
                User user,
                double similarity,
                boolean friend,
                int mutualFriendsCount
        ) {
            public boolean isFriend() { return friend; }
            public double getSimilarity() { return similarity; }
            public int getMutualFriendsCount() { return mutualFriendsCount; }
        }

        java.util.List<UserSearchResult> results;
        try {
            java.util.List<SearchUserHit> hits = searchServiceClient.searchUsers(query.trim());
            results = new java.util.ArrayList<>();
            for (int i = 0; i < hits.size(); i++) {
                SearchUserHit hit = hits.get(i);
                User user = User.builder()
                        .id(UUID.fromString(hit.getId()))
                        .username(hit.getUsername())
                        .firstName(hit.getFirstName())
                        .lastName(hit.getLastName())
                        .avatarId(hit.getAvatarId())
                        .build();
                results.add(new UserSearchResult(user, 1.0 - (i * 0.05), false, 0));
            }
        } catch (Exception e) {
            log.error("Meilisearch search failed via search-service gRPC: {}", e.getMessage(), e);
            return java.util.Collections.emptyList();
        }

        if (currentUserId != null && !results.isEmpty()) {
            java.util.List<UUID> targetUserIds = results.stream()
                    .map(r -> r.user().getId())
                    .collect(java.util.stream.Collectors.toList());

            java.util.List<UserRelation> relations = socialGraphClient.getRelations(currentUserId, targetUserIds);

            java.util.Map<UUID, UserRelation> relationMap = relations.stream()
                    .collect(java.util.stream.Collectors.toMap(
                            rel -> UUID.fromString(rel.getTargetUserId()),
                            rel -> rel,
                            (r1, r2) -> r1
                    ));

            results = results.stream()
                    .map(res -> {
                        UserRelation rel = relationMap.get(res.user().getId());
                        if (rel != null) {
                            return new UserSearchResult(res.user(), res.similarity(), rel.getFriend(), rel.getMutualFriendsCount());
                        }
                        return res;
                    })
                    .collect(java.util.stream.Collectors.toList());

            results.sort(
                    java.util.Comparator.comparing(UserSearchResult::isFriend, java.util.Comparator.reverseOrder())
                            .thenComparing(res -> res.getMutualFriendsCount() > 0, java.util.Comparator.reverseOrder())
                            .thenComparing(UserSearchResult::getSimilarity, java.util.Comparator.reverseOrder())
                            .thenComparing(UserSearchResult::getMutualFriendsCount, java.util.Comparator.reverseOrder())
            );
        }

        return results.stream()
                .limit(6)
                .map(res -> UserProtoMapper.toUserDto(res.user()))
                .collect(java.util.stream.Collectors.toList());
    }

    @Transactional(readOnly = true)
    public java.util.List<UserDto> getSearchHistory(UUID searchingUserId) {
        if (searchingUserId != null) {
            return searchUserRepository.findBySearchingUserIdOrderByLastSearchedAtDesc(searchingUserId).stream()
                    .map(su -> {
                        User user = su.getSearchedUser();
                        long lastSearchedEpoch = su.getLastSearchedAt().atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli();
                        int newPostsCount = (int) searchUserRepository.countNewPostsSince(user.getId().toString(), lastSearchedEpoch);
                        return UserProtoMapper.toUserDto(user, true, newPostsCount);
                    })
                    .collect(java.util.stream.Collectors.toList());
        }

        return searchUserRepository.findAllByOrderByLastSearchedAtDesc().stream()
                .map(su -> UserProtoMapper.toUserDto(su.getSearchedUser(), true, 0))
                .collect(java.util.stream.Collectors.toList());
    }

    @Transactional
    public UserDto setAvatar(UUID userId, org.springframework.web.multipart.MultipartFile file) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));

        String avatarId = minioService.uploadAvatar(file)
                .orElseThrow(() -> new IllegalStateException("Storage service is currently unavailable"));
        user.setAvatarId(avatarId);
        User savedUser = userRepository.save(user);
        syncUserToSearchService(savedUser);
        return mapToUserDto(savedUser);
    }

    @Transactional
    public UserDto setCoverPhoto(UUID userId, org.springframework.web.multipart.MultipartFile file) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));

        String coverId = minioService.uploadAvatar(file)
                .orElseThrow(() -> new IllegalStateException("Storage service is currently unavailable"));
        user.setCoverPhotoId(coverId);
        User savedUser = userRepository.save(user);
        syncUserToSearchService(savedUser);
        return mapToUserDto(savedUser);
    }

    public io.minio.GetObjectResponse downloadAvatar(String avatarId) {
        return minioService.downloadAvatar(avatarId)
                .orElseThrow(() -> new IllegalStateException("Unable to download avatar from storage"));
    }

    public String uploadChatMedia(org.springframework.web.multipart.MultipartFile file) {
        return minioService.uploadChatMedia(file)
                .orElseThrow(() -> new IllegalStateException("Storage service is currently unavailable"));
    }

    public com.facebook.UserService.dto.MediaAccessResponse resolveMediaAccess(String reference) {
        return mediaUrlService.buildMediaAccess(reference);
    }

    public String resolveMediaUrlForClient(String reference) {
        return mediaUrlService.resolveForClient(reference);
    }

    public String normalizeMediaReference(String reference) {
        return mediaUrlService.normalizeForStorage(reference);
    }

    @Transactional
    public UserDto updateProfile(UUID userId, UpdateProfileRequest input) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));

        UserProtoMapper.applyUpdateProfileRequest(user, input);

        User savedUser = userRepository.save(user);
        syncUserToSearchService(savedUser);
        return mapToUserDto(savedUser);
    }

    private UserDto mapToUserDto(User user) {
        if (user == null) {
            return UserDto.getDefaultInstance();
        }

        java.util.Optional<SearchUser> searchHistoryOpt = searchUserRepository.findBySearchedUser(user);

        boolean inHistory = searchHistoryOpt.isPresent();
        int newPostsCount = 0;
        if (inHistory) {
            java.time.LocalDateTime lastSearched = searchHistoryOpt.get().getLastSearchedAt();
            long lastSearchedEpoch = lastSearched.atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli();
            try {
                newPostsCount = (int) searchUserRepository.countNewPostsSince(user.getId().toString(), lastSearchedEpoch);
            } catch (Exception e) {
                log.error("Error querying new posts count: {}", e.getMessage());
            }
        }

        return UserProtoMapper.toUserDto(user, inHistory, newPostsCount);
    }

    public String resolveMediaPresignedUrl(String fileId) {
        return minioService.getPresignedObjectUrl("feed-uploads", fileId).orElse("");
    }

    // ==========================================
    // OPAQUE VAULT (E2EE PIN Backup)
    // ==========================================

    @Transactional(readOnly = true)
    public Map<String, Object> getVault(UUID userId) {
        return userRepository.findById(userId)
                .filter(user -> user.getOpaqueRecord() != null && !user.getOpaqueRecord().isEmpty())
                .map(user -> {
                    Map<String, Object> vault = new HashMap<>();
                    vault.put("opaqueRecord", user.getOpaqueRecord());
                    vault.put("encryptedHistory", user.getEncryptedHistory());
                    // Domyślnie 0, jeśli wartość w bazie to null
                    vault.put("failedAttempts", user.getFailedAttempts() != null ? user.getFailedAttempts() : 0);
                    return vault;
                })
                .orElse(null); // Jeśli użytkownik nie utworzył jeszcze Sejfu, zwracamy null
    }

    @Transactional
    public void updateVaultAttempts(UUID userId, Integer attempts) {
        userRepository.findById(userId).ifPresent(user -> {
            user.setFailedAttempts(attempts);
            userRepository.save(user);
            log.info("Updated failed vault attempts for user {}: {}", userId, attempts);
        });
    }

    @Transactional
    public void saveVault(UUID userId, String opaqueRecord, String encryptedHistory, Integer failedAttempts) {
        userRepository.findById(userId).ifPresent(user -> {
            user.setOpaqueRecord(opaqueRecord);
            user.setEncryptedHistory(encryptedHistory);
            user.setFailedAttempts(failedAttempts);
            userRepository.save(user);
            log.info("Saved OPAQUE Vault securely for user: {}", userId);
        });
    }
}