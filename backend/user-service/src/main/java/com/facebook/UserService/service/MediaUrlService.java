package com.facebook.UserService.service;

import com.facebook.UserService.dto.MediaAccessResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;

@Service
public class MediaUrlService {

    private static final Pattern OBJECT_KEY_PATTERN =
            Pattern.compile("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}-.+$");

    private final MinioService minioService;

    @Value("${media.stable-url-base:http://localhost:8080}")
    private String stableUrlBase;

    public MediaUrlService(MinioService minioService) {
        this.minioService = minioService;
    }

    public MediaAccessResponse buildMediaAccess(String objectKey) {
        if (objectKey == null || objectKey.isBlank()) {
            return new MediaAccessResponse("", "", "", 0L);
        }
        String normalizedKey = normalizeForStorage(objectKey);
        if (!isMinioObjectKey(normalizedKey)) {
            return new MediaAccessResponse(normalizedKey, normalizedKey, normalizedKey, 0L);
        }
        String presignedUrl = minioService.getPresignedObjectUrl(normalizedKey).orElse("");
        long expiresAt = minioService.getPresignedExpiryEpochSeconds();
        String stableUrl = toStableUrl(normalizedKey);
        return new MediaAccessResponse(normalizedKey, stableUrl, presignedUrl, expiresAt);
    }

    public String resolveForClient(String storedValue) {
        if (storedValue == null || storedValue.isBlank()) {
            return storedValue;
        }
        String objectKey = normalizeForStorage(storedValue);
        if (!isMinioObjectKey(objectKey)) {
            return storedValue;
        }
        return minioService.getPresignedObjectUrl(objectKey).orElse(storedValue);
    }

    public String normalizeForStorage(String value) {
        if (value == null || value.isBlank()) {
            return value;
        }
        String trimmed = value.trim();

        if (isMinioObjectKey(trimmed)) {
            return trimmed;
        }

        if (trimmed.contains("X-Amz-") || trimmed.contains("x-amz-")) {
            return extractObjectKeyFromPresignedUrl(trimmed);
        }

        String proxyKey = extractAfterMarker(trimmed, "/api/users/avatar/");
        if (proxyKey != null) {
            return URLDecoder.decode(proxyKey, StandardCharsets.UTF_8);
        }

        String bucketKey = extractFromBucketPath(trimmed, minioService.getBucketName());
        if (bucketKey != null) {
            return bucketKey;
        }

        return trimmed;
    }

    public String toStableUrl(String objectKey) {
        if (objectKey == null || objectKey.isBlank()) {
            return objectKey;
        }
        String base = stableUrlBase.endsWith("/") ? stableUrlBase.substring(0, stableUrlBase.length() - 1) : stableUrlBase;
        return base + "/api/users/avatar/" + objectKey;
    }

    public boolean isMinioObjectKey(String value) {
        if (value == null || value.isBlank()) {
            return false;
        }
        if (value.startsWith("http://") || value.startsWith("https://") || value.startsWith("/files/")) {
            return false;
        }
        return OBJECT_KEY_PATTERN.matcher(value).matches();
    }

    private String extractObjectKeyFromPresignedUrl(String url) {
        try {
            URI uri = URI.create(url.split("#")[0]);
            String path = uri.getPath();
            if (path == null || path.isBlank()) {
                return url;
            }
            String bucket = minioService.getBucketName();
            String bucketPrefix = "/" + bucket + "/";
            if (path.startsWith(bucketPrefix)) {
                return URLDecoder.decode(path.substring(bucketPrefix.length()), StandardCharsets.UTF_8);
            }
            if (path.startsWith("/")) {
                return URLDecoder.decode(path.substring(1), StandardCharsets.UTF_8);
            }
        } catch (Exception ignored) {
            // fall through
        }
        return url;
    }

    private String extractFromBucketPath(String url, String bucketName) {
        String marker = "/" + bucketName + "/";
        int idx = url.indexOf(marker);
        if (idx < 0) {
            return null;
        }
        String remainder = url.substring(idx + marker.length());
        int queryIdx = remainder.indexOf('?');
        if (queryIdx >= 0) {
            remainder = remainder.substring(0, queryIdx);
        }
        return URLDecoder.decode(remainder, StandardCharsets.UTF_8);
    }

    private String extractAfterMarker(String url, String marker) {
        int idx = url.indexOf(marker);
        if (idx < 0) {
            return null;
        }
        String remainder = url.substring(idx + marker.length());
        int queryIdx = remainder.indexOf('?');
        if (queryIdx >= 0) {
            remainder = remainder.substring(0, queryIdx);
        }
        int hashIdx = remainder.indexOf('#');
        if (hashIdx >= 0) {
            remainder = remainder.substring(0, hashIdx);
        }
        return remainder.isBlank() ? null : remainder;
    }
}
