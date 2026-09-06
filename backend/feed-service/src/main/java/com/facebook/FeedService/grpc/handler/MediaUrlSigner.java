package com.facebook.FeedService.grpc.handler;

import org.springframework.stereotype.Component;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

@Component
public class MediaUrlSigner {

    private static final String SHARED_SECRET = "secret-media-key";

    public String extractFileId(String src) {
        if (src == null) return null;
        String path = src;
        if (path.contains("/media/")) {
            path = path.substring(path.lastIndexOf("/media/") + "/media/".length());
        } else if (path.contains("/files/")) {
            path = path.substring(path.lastIndexOf("/files/") + "/files/".length());
        }
        int qIdx = path.indexOf('?');
        if (qIdx != -1) path = path.substring(0, qIdx);
        int hIdx = path.indexOf('#');
        if (hIdx != -1) path = path.substring(0, hIdx);
        // tusd S3 object keys are the upload id without the "+{info}" suffix used in URLs
        int plusIdx = path.indexOf('+');
        if (plusIdx != -1) path = path.substring(0, plusIdx);
        return path;
    }

    public String signUrl(String path) {
        long expires = (System.currentTimeMillis() / 1000L) + 3600;
        String expiresStr = String.valueOf(expires);
        String signature = generateSignature(path, expiresStr, SHARED_SECRET);
        return path + "?expires=" + expiresStr + "&signature=" + signature;
    }

    public String generateSignature(String path, String expires, String secret) {
        try {
            SecretKeySpec signingKey = new SecretKeySpec(secret.getBytes(), "HmacSHA256");
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(signingKey);
            byte[] rawHmac = mac.doFinal((path + expires).getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : rawHmac) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate signature", e);
        }
    }

    public String reconstructStoryUrl(String src) {
        if (src == null) return null;
        if (src.startsWith("stories/")) {
            return signUrl("/" + src);
        }
        return reconstructUrl(src);
    }

    public String reconstructUrl(String src) {
        if (src == null) return null;
        if (src.startsWith("http://") || src.startsWith("https://")) {
            return src;
        }
        if (src.startsWith("/media/")) {
            return signUrl(src);
        }
        if (src.startsWith("/files/")) {
            return signUrl("/media/" + src.substring("/files/".length()));
        }
        return signUrl("/media/" + src);
    }
}
