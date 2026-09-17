package com.facebook.FeedService.grpc.handler;

import org.springframework.stereotype.Component;

@Component
public class MediaUrlSigner {

    public String extractFileId(String src) {
        if (src == null) return null;
        String path = cleanUrl(src);
        if (path.contains("/media/")) {
            path = path.substring(path.lastIndexOf("/media/") + "/media/".length());
        } else if (path.contains("/files/")) {
            path = path.substring(path.lastIndexOf("/files/") + "/files/".length());
        } else if (path.contains("/stories/")) {
            path = path.substring(path.lastIndexOf("/stories/") + "/stories/".length());
        } else if (path.contains("/videos/")) {
            path = path.substring(path.lastIndexOf("/videos/") + "/videos/".length());
        }
        int qIdx = path.indexOf('?');
        if (qIdx != -1) path = path.substring(0, qIdx);
        int hIdx = path.indexOf('#');
        if (hIdx != -1) path = path.substring(0, hIdx);
        int plusIdx = path.indexOf('+');
        if (plusIdx != -1) path = path.substring(0, plusIdx);
        return path;
    }

    public String cleanUrl(String url) {
        if (url == null) return null;
        int qIdx = url.indexOf('?');
        if (qIdx != -1) {
            String query = url.substring(qIdx);
            if (query.contains("signature=") || query.contains("expires=")) {
                return url.substring(0, qIdx);
            }
        }
        return url;
    }

    public String signUrl(String path) {
        return cleanUrl(path);
    }

    public String generateSignature(String path, String expires, String secret) {
        return "";
    }

    public String reconstructStoryUrl(String src) {
        if (src == null) return null;
        String cleaned = cleanUrl(src);
        if (cleaned.startsWith("http://") || cleaned.startsWith("https://")) {
            return cleaned;
        }
        if (cleaned.startsWith("/stories/") || cleaned.startsWith("/media/") || cleaned.startsWith("/files/")) {
            return cleaned;
        }
        if (cleaned.startsWith("stories/")) {
            return "/" + cleaned;
        }
        if (cleaned.startsWith("/")) {
            return cleaned;
        }
        return "/stories/" + cleaned;
    }

    public String reconstructUrl(String src) {
        if (src == null) return null;
        String cleaned = cleanUrl(src);
        if (cleaned.startsWith("http://") || cleaned.startsWith("https://")) {
            return cleaned;
        }
        if (cleaned.startsWith("/media/") || cleaned.startsWith("/files/") || cleaned.startsWith("/stories/") || cleaned.startsWith("/videos/")) {
            return cleaned;
        }
        if (cleaned.startsWith("media/") || cleaned.startsWith("files/") || cleaned.startsWith("stories/") || cleaned.startsWith("videos/")) {
            return "/" + cleaned;
        }
        if (cleaned.startsWith("/")) {
            return cleaned;
        }
        return "/media/" + cleaned;
    }
}
