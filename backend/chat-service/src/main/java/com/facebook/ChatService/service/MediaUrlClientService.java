package com.facebook.ChatService.service;

import com.facebook.ChatService.util.MediaReferenceNormalizer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class MediaUrlClientService {

    private final MinioService minioService;

    @org.springframework.beans.factory.annotation.Value("${media.stable-url-base:http://localhost:8080}")
    private String stableUrlBase;

    public MediaUrlClientService(MinioService minioService) {
        this.minioService = minioService;
    }

    public String resolveForClient(String storedValue) {
        if (storedValue == null || storedValue.isBlank()) {
            return storedValue;
        }
        // Keep third-party URLs as-is (legacy external GIFs etc.)
        if ((storedValue.startsWith("http://") || storedValue.startsWith("https://"))
                && !MediaReferenceNormalizer.isManagedMediaUrl(storedValue)) {
            return storedValue;
        }
        try {
            String objectKey = storedValue;
            if (storedValue.startsWith("http://") || storedValue.startsWith("https://") || storedValue.contains("/")) {
                objectKey = extractObjectKeyFromUrl(storedValue);
            }
            String base = stableUrlBase.endsWith("/") ? stableUrlBase.substring(0, stableUrlBase.length() - 1) : stableUrlBase;
            return base + "/api/chat/media/" + objectKey;
        } catch (Exception e) {
            log.warn("Failed to resolve media URL locally in chat-service: {}", e.getMessage());
            return storedValue;
        }
    }

    private String extractObjectKeyFromUrl(String url) {
        try {
            String urlPart = url.split("#")[0].split("\\?")[0];
            java.net.URI uri = java.net.URI.create(urlPart);
            String path = uri.getPath();
            if (path == null || path.isBlank()) {
                return url;
            }
            String chatMarker = "/api/chat/media/";
            int chatIdx = path.indexOf(chatMarker);
            if (chatIdx >= 0) {
                return java.net.URLDecoder.decode(path.substring(chatIdx + chatMarker.length()), java.nio.charset.StandardCharsets.UTF_8);
            }
            String proxyMarker = "/api/users/avatar/";
            int proxyIdx = path.indexOf(proxyMarker);
            if (proxyIdx >= 0) {
                return java.net.URLDecoder.decode(path.substring(proxyIdx + proxyMarker.length()), java.nio.charset.StandardCharsets.UTF_8);
            }
            return url;
        } catch (Exception ignored) {
        }
        return url;
    }
}
