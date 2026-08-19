package com.facebook.UserService.service;

import io.minio.BucketExistsArgs;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.StatObjectArgs;
import io.minio.http.Method;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct;
import java.io.InputStream;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class MinioService {
    public static final String DEFAULT_AVATAR_OBJECT = "default_avatar.png";

    @Value("${minio.url}")
    private String minioUrl;

    @Value("${minio.access-key}")
    private String accessKey;

    @Value("${minio.secret-key}")
    private String secretKey;

    @Value("${minio.bucket-name}")
    private String bucketName;

    @Value("${minio.public-endpoint:}")
    private String publicEndpoint;

    @Value("${minio.presigned-expiry-seconds:3600}")
    private long presignedExpirySeconds;

    private MinioClient minioClient;
    private MinioClient presignClient;
    private boolean initialized = false;

    @PostConstruct
    public void init() {
        ensureInitialized();
    }

    private synchronized void ensureInitialized() {
        if (initialized) {
            return;
        }
        try {
            minioClient = MinioClient.builder()
                    .endpoint(minioUrl)
                    .credentials(accessKey, secretKey)
                    .region("us-east-1")
                    .build();

            String presignEndpoint = (publicEndpoint != null && !publicEndpoint.isBlank())
                    ? publicEndpoint
                    : minioUrl;
            presignClient = MinioClient.builder()
                    .endpoint(presignEndpoint)
                    .credentials(accessKey, secretKey)
                    .region("us-east-1")
                    .build();

            boolean found = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
            if (!found) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
            }
            for (String bucket : java.util.List.of("poczekalnia", "feed-uploads")) {
                if (!minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucket).build())) {
                    minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucket).build());
                }
            }
            try {
                minioClient.statObject(StatObjectArgs.builder().bucket(bucketName).object(DEFAULT_AVATAR_OBJECT).build());
            } catch (Exception missingDefaultAvatar) {
                try (InputStream avatar = getClass().getResourceAsStream("/default_avatar.png")) {
                    if (avatar != null) {
                        byte[] content = avatar.readAllBytes();
                        minioClient.putObject(PutObjectArgs.builder().bucket(bucketName).object(DEFAULT_AVATAR_OBJECT)
                                .stream(new java.io.ByteArrayInputStream(content), content.length, -1)
                                .contentType("image/svg+xml").build());
                    }
                }
            }
            initialized = true;
        } catch (Exception e) {
            initialized = false;
        }
    }

    public java.util.Optional<String> uploadAvatar(MultipartFile file) {
        ensureInitialized();
        if (!initialized) {
            return java.util.Optional.empty();
        }

        if (file.isEmpty()) {
            throw new IllegalArgumentException("File cannot be empty");
        }

        // Validate content type
        String contentType = file.getContentType();
        if (contentType == null || !(contentType.equals("image/jpeg") || 
                                     contentType.equals("image/png") || 
                                     contentType.equals("image/gif") ||
                                     contentType.equals("image/webp"))) {
            throw new IllegalArgumentException("Only JPEG, PNG, GIF, and WEBP images are allowed");
        }

        // Validate file extension
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            throw new IllegalArgumentException("Invalid file name");
        }
        String lowerFilename = originalFilename.toLowerCase();
        if (!(lowerFilename.endsWith(".jpg") || 
              lowerFilename.endsWith(".jpeg") || 
              lowerFilename.endsWith(".png") || 
              lowerFilename.endsWith(".gif") ||
              lowerFilename.endsWith(".webp"))) {
            throw new IllegalArgumentException("File extension must be one of: .jpg, .jpeg, .png, .gif, .webp");
        }

        String objectName = UUID.randomUUID().toString() + "-" + originalFilename;

        try (InputStream inputStream = file.getInputStream()) {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .stream(inputStream, file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build()
            );
            return java.util.Optional.of(objectName);
        } catch (Exception e) {
            return java.util.Optional.empty();
        }
    }

    public java.util.Optional<String> uploadChatMedia(MultipartFile file) {
        ensureInitialized();
        if (!initialized) {
            return java.util.Optional.empty();
        }

        if (file.isEmpty()) {
            throw new IllegalArgumentException("File cannot be empty");
        }

        String contentType = file.getContentType();
        if (contentType == null) {
            contentType = "application/octet-stream";
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            throw new IllegalArgumentException("Invalid file name");
        }

        String objectName = UUID.randomUUID().toString() + "-" + originalFilename;

        try (InputStream inputStream = file.getInputStream()) {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .stream(inputStream, file.getSize(), -1)
                            .contentType(contentType)
                            .build()
            );
            return java.util.Optional.of(objectName);
        } catch (Exception e) {
            return java.util.Optional.empty();
        }
    }

    public java.util.Optional<io.minio.GetObjectResponse> downloadAvatar(String objectName) {
        ensureInitialized();
        if (!initialized) {
            return java.util.Optional.empty();
        }

        String targetObject = (objectName == null || objectName.isBlank() || objectName.equals("default-avatar.svg")) 
                ? DEFAULT_AVATAR_OBJECT : objectName;
        try {
            return java.util.Optional.of(minioClient.getObject(
                    io.minio.GetObjectArgs.builder()
                            .bucket(bucketName)
                            .object(targetObject)
                            .build()
            ));
        } catch (Exception e) {
            try {
                return java.util.Optional.of(minioClient.getObject(
                        io.minio.GetObjectArgs.builder()
                                .bucket(bucketName)
                                .object(DEFAULT_AVATAR_OBJECT)
                                .build()
                ));
            } catch (Exception fallbackEx) {
                return java.util.Optional.empty();
            }
        }
    }

    public java.util.Optional<String> getPresignedObjectUrl(String objectName) {
        ensureInitialized();
        if (!initialized) {
            return java.util.Optional.empty();
        }

        if (objectName == null || objectName.isBlank()) {
            throw new IllegalArgumentException("Object name is required");
        }
        try {
            return java.util.Optional.of(presignClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(bucketName)
                            .object(objectName)
                            .expiry((int) presignedExpirySeconds, TimeUnit.SECONDS)
                            .build()
            ));
        } catch (Exception e) {
            return java.util.Optional.empty();
        }
    }

    public java.util.Optional<String> getPresignedObjectUrl(String customBucket, String objectName) {
        ensureInitialized();
        if (!initialized) {
            return java.util.Optional.empty();
        }

        if (objectName == null || objectName.isBlank()) {
            throw new IllegalArgumentException("Object name is required");
        }
        try {
            return java.util.Optional.of(presignClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(customBucket)
                            .object(objectName)
                            .expiry((int) presignedExpirySeconds, TimeUnit.SECONDS)
                            .build()
            ));
        } catch (Exception e) {
            return java.util.Optional.empty();
        }
    }

    public long getPresignedExpiryEpochSeconds() {
        return (System.currentTimeMillis() / 1000L) + presignedExpirySeconds;
    }

    public String getBucketName() {
        return bucketName;
    }
}
