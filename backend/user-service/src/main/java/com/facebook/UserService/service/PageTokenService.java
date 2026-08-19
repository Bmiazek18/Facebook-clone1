package com.facebook.UserService.service;

import com.facebook.UserService.dto.PageDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class PageTokenService {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final StringRedisTemplate redisTemplate;

    @Value("${jwt.secret:default-secret-key-for-facebook-clone-page-token-exchange-32-chars}")
    private String jwtSecret;

    @Value("${jwt.page-token.ttl-seconds:86400}")
    private long pageTokenTtlSeconds;

    @Autowired
    public PageTokenService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public String generatePageAccessToken(PageDto page, UUID userId) {
        try {
            long nowSeconds = System.currentTimeMillis() / 1000;
            long expSeconds = nowSeconds + pageTokenTtlSeconds; // Domyślnie 24 godziny (86400s)

            // Header
            Map<String, Object> header = new HashMap<>();
            header.put("alg", "HS256");
            header.put("typ", "JWT");

            // Payload
            Map<String, Object> payload = new HashMap<>();
            payload.put("sub", page.getId().toString());
            payload.put("entity_type", "PAGE");
            payload.put("page_id", page.getId().toString());
            payload.put("user_id", userId.toString());
            payload.put("actor_id", page.getId().toString());
            payload.put("page_name", page.getName());
            payload.put("page_avatar", page.getAvatar());
            payload.put("roles", List.of("PAGE_ADMIN"));
            payload.put("iat", nowSeconds);
            payload.put("exp", expSeconds);

            String headerB64 = Base64.getUrlEncoder().withoutPadding()
                    .encodeToString(objectMapper.writeValueAsBytes(header));
            String payloadB64 = Base64.getUrlEncoder().withoutPadding()
                    .encodeToString(objectMapper.writeValueAsBytes(payload));

            String dataToSign = headerB64 + "." + payloadB64;
            String signatureB64 = signHmacSha256(dataToSign, jwtSecret);
            String token = dataToSign + "." + signatureB64;

            // Zapis Page Tokena i aktywnego aktora w Redis obok sesji
            saveActivePageSession(userId, page.getId(), token);

            return token;
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate Page Access Token", e);
        }
    }

    public void saveActivePageSession(UUID userId, UUID pageId, String token) {
        if (redisTemplate != null) {
            Duration ttl = Duration.ofSeconds(pageTokenTtlSeconds);
            redisTemplate.opsForValue().set("user:session:" + userId + ":page_token", token, ttl);
            redisTemplate.opsForValue().set("user:session:" + userId + ":active_actor", pageId.toString(), ttl);
            redisTemplate.opsForValue().set("page:actor:" + pageId + ":" + userId, token, ttl);
        }
    }

    public String getActivePageToken(UUID userId) {
        if (redisTemplate != null) {
            return redisTemplate.opsForValue().get("user:session:" + userId + ":page_token");
        }
        return null;
    }

    public String getActiveActorId(UUID userId) {
        if (redisTemplate != null) {
            return redisTemplate.opsForValue().get("user:session:" + userId + ":active_actor");
        }
        return null;
    }

    public void clearActivePageSession(UUID userId) {
        if (redisTemplate != null) {
            String activeActor = getActiveActorId(userId);
            if (activeActor != null) {
                redisTemplate.delete("page:actor:" + activeActor + ":" + userId);
            }
            redisTemplate.delete("user:session:" + userId + ":page_token");
            redisTemplate.delete("user:session:" + userId + ":active_actor");
        }
    }

    public long getPageTokenTtlSeconds() {
        return pageTokenTtlSeconds;
    }

    private String signHmacSha256(String data, String secret)
            throws NoSuchAlgorithmException, InvalidKeyException {
        Mac sha256Hmac = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        sha256Hmac.init(secretKey);
        byte[] signedBytes = sha256Hmac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        return Base64.getUrlEncoder().withoutPadding().encodeToString(signedBytes);
    }
}
