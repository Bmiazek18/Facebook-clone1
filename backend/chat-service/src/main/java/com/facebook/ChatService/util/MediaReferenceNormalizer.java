package com.facebook.ChatService.util;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;

/**
 * Normalizes media references to MinIO object keys before persisting them.
 * Presigned URLs expire after ~1 hour, so only stable object keys belong in the database.
 * External URLs (e.g. legacy Giphy links) are left unchanged.
 */
public final class MediaReferenceNormalizer {

    private static final Pattern OBJECT_KEY_PATTERN =
            Pattern.compile("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}-.+$");

    private MediaReferenceNormalizer() {
    }

    public static boolean isManagedMediaUrl(String value) {
        if (value == null || value.isBlank()) return false;
        String trimmed = value.trim();
        if (OBJECT_KEY_PATTERN.matcher(trimmed).matches()) return true;
        return trimmed.contains("/api/chat/media/")
                || trimmed.contains("/api/users/avatar/")
                || trimmed.contains("/files/");
    }

    public static String normalize(String value) {
        if (value == null || value.isBlank()) {
            return value;
        }

        String trimmed = value.trim();
        if (OBJECT_KEY_PATTERN.matcher(trimmed).matches()) {
            return trimmed;
        }

        // Do not rewrite third-party URLs into fake object keys
        if ((trimmed.startsWith("http://") || trimmed.startsWith("https://")) && !isManagedMediaUrl(trimmed)) {
            return trimmed;
        }

        if (trimmed.startsWith("http://") || trimmed.startsWith("https://") || trimmed.contains("/")) {
            try {
                String urlPart = trimmed.split("#")[0].split("\\?")[0];
                URI uri = URI.create(urlPart);
                String path = uri.getPath();
                if (path != null && !path.isBlank()) {
                    String chatMarker = "/api/chat/media/";
                    int chatIdx = path.indexOf(chatMarker);
                    if (chatIdx >= 0) {
                        return URLDecoder.decode(path.substring(chatIdx + chatMarker.length()), StandardCharsets.UTF_8);
                    }
                    String proxyMarker = "/api/users/avatar/";
                    int proxyIdx = path.indexOf(proxyMarker);
                    if (proxyIdx >= 0) {
                        return URLDecoder.decode(path.substring(proxyIdx + proxyMarker.length()), StandardCharsets.UTF_8);
                    }
                    String filesMarker = "/files/";
                    int filesIdx = path.indexOf(filesMarker);
                    if (filesIdx >= 0) {
                        return URLDecoder.decode(path.substring(filesIdx + filesMarker.length()), StandardCharsets.UTF_8);
                    }
                }
            } catch (Exception ignored) {
            }
        }

        return trimmed;
    }
}
