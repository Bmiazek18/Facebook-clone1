package com.facebook.ChatService.controller;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.PreparedStatement;
import com.datastax.oss.driver.api.core.cql.Row;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import io.agora.media.RtcTokenBuilder2;

import java.time.Duration;
import java.util.*;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private static final Logger log = LoggerFactory.getLogger(ChatController.class);

    private final CqlSession session;
    private final PreparedStatement selectReadReceiptsStmt;
    private final org.springframework.data.redis.core.StringRedisTemplate redisTemplate;
    private final com.facebook.ChatService.service.ChatWriteService chatWriteService;
    private final com.facebook.ChatService.service.MinioService minioService;
    private final com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();

    @org.springframework.beans.factory.annotation.Value("${media.stable-url-base:http://localhost:8080}")
    private String stableUrlBase;

    public ChatController(
            CqlSession session,
            PreparedStatement selectReadReceiptsStmt,
            org.springframework.data.redis.core.StringRedisTemplate redisTemplate,
            com.facebook.ChatService.service.ChatWriteService chatWriteService,
            com.facebook.ChatService.service.MinioService minioService) {
        this.session = session;
        this.selectReadReceiptsStmt = selectReadReceiptsStmt;
        this.redisTemplate = redisTemplate;
        this.chatWriteService = chatWriteService;
        this.minioService = minioService;
    }

    @GetMapping("/receipts")
    public Map<String, String> getReadReceipts(@RequestParam("conversationId") String conversationIdStr) {
        UUID conversationId = toUuid(conversationIdStr);
        var resultSet = session.execute(selectReadReceiptsStmt.bind(conversationId));
        Map<String, String> result = new HashMap<>();
        for (Row row : resultSet) {
            UUID userId = row.getUuid("user_id");
            UUID msgId = row.getUuid("last_read_message_id");
            result.put(userId.toString(), msgId.toString());
        }
        return result;
    }

    // ==========================================
    // AGORA CALLS
    // ==========================================

    @GetMapping("/calls/token")
    public Map<String, Object> getCallToken(
            @RequestParam("channelName") String channelName,
            @RequestParam("uid") int uid) {

        String appId = System.getenv().getOrDefault("AGORA_APP_ID", "29953eb024f04a80a256df1b23678da1");
        String appCertificate = System.getenv().getOrDefault("AGORA_APP_CERTIFICATE", "15a6b0c3df0a4ea80a256df1b23678da1");

        int expirationTimeInSeconds = 3600; // 1 hour

        RtcTokenBuilder2 tokenBuilder = new RtcTokenBuilder2();
        String token = tokenBuilder.buildTokenWithUid(
                appId,
                appCertificate,
                channelName,
                uid,
                RtcTokenBuilder2.Role.ROLE_PUBLISHER,
                expirationTimeInSeconds,
                expirationTimeInSeconds
        );

        Map<String, Object> response = new HashMap<>();
        response.put("token", token);
        response.put("appId", appId);
        response.put("channelName", channelName);
        response.put("uid", uid);
        return response;
    }

    @PostMapping("/calls/start")
    public Map<String, Object> startCall(
            @RequestParam("conversationId") String conversationId,
            @RequestParam("callerId") String callerId,
            @RequestParam("callType") String callType) {

        String key = "active_call:" + conversationId;
        Map<String, Object> meta = new HashMap<>();
        meta.put("conversationId", conversationId);
        meta.put("callerId", callerId);
        meta.put("callType", callType);
        meta.put("startedAt", System.currentTimeMillis());

        try {
            String json = objectMapper.writeValueAsString(meta);
            redisTemplate.opsForValue().set(key, json, Duration.ofSeconds(30));
        } catch (Exception e) {
            log.error("Failed to serialize call metadata to Redis for conversation: {}", conversationId, e);
            throw new RuntimeException("Failed to start call metadata", e);
        }

        return meta;
    }

    @PostMapping("/calls/heartbeat")
    public void heartbeatCall(@RequestParam("conversationId") String conversationId) {
        String key = "active_call:" + conversationId;
        redisTemplate.expire(key, Duration.ofSeconds(30));
    }

    @PostMapping("/calls/end")
    public void endCall(@RequestParam("conversationId") String conversationId) {
        String key = "active_call:" + conversationId;
        redisTemplate.delete(key);
    }

    @GetMapping("/calls/active")
    public Map<String, Object> getActiveCall(@RequestParam("conversationId") String conversationId) {
        String key = "active_call:" + conversationId;
        String json = redisTemplate.opsForValue().get(key);
        if (json == null) {
            return Collections.emptyMap();
        }
        try {
            return objectMapper.readValue(json, Map.class);
        } catch (Exception e) {
            log.warn("Failed to parse active call data for conversation: {}", conversationId, e);
            return Collections.emptyMap();
        }
    }

    @PostMapping("/calls/log")
    public Map<String, Object> saveCallLog(
            @RequestParam("conversationId") String conversationId,
            @RequestParam("senderId") String senderId,
            @RequestParam("callerId") String callerId,
            @RequestParam("duration") int duration,
            @RequestParam("status") String status,
            @RequestParam(value = "participantIds", required = false) List<String> participantIds) {

        String text;
        String actionType;
        String actionPayload;

        if ("rejected".equals(status)) {
            text = "SYSTEM_ACTION:call_rejected";
            actionType = "call_rejected";
            actionPayload = "";
        } else {
            text = "SYSTEM_ACTION:call_ended:" + duration;
            actionType = "call_ended";
            actionPayload = String.valueOf(duration);
        }

        List<String> resolvedParticipants = new ArrayList<>();
        if (participantIds != null) {
            for (String p : participantIds) {
                if (p != null) {
                    for (String sub : p.split(",")) {
                        String clean = sub.trim().replace("user_", "");
                        if (!clean.isEmpty() && !resolvedParticipants.contains(clean)) {
                            resolvedParticipants.add(clean);
                        }
                    }
                }
            }
        }
        if (resolvedParticipants.isEmpty()) {
            resolvedParticipants.add(senderId.replace("user_", ""));
            String cleanConv = conversationId.replace("user_", "");
            if (!senderId.replace("user_", "").equals(cleanConv)) {
                resolvedParticipants.add(cleanConv);
            }
        }

        return chatWriteService.sendMessage(
                senderId, conversationId, text, null, null, null, null, null,
                duration, null, null, null, null, resolvedParticipants, actionType, actionPayload
        );
    }

    @PostMapping("/calls/invite")
    public Map<String, Object> inviteToCall(
            @RequestParam("conversationId") String conversationId,
            @RequestParam("senderId") String senderId,
            @RequestParam("invitedUserId") String invitedUserId,
            @RequestParam(value = "channelName", required = false) String channelName,
            @RequestParam(value = "participantIds", required = false) List<String> participantIds) {

        String text = "SYSTEM_ACTION:call_started:group";
        String actionType = "call_started";
        String actionPayload = channelName != null ? channelName : conversationId;

        List<String> resolvedParticipants = new ArrayList<>();
        if (participantIds != null) {
            for (String p : participantIds) {
                if (p != null) {
                    for (String sub : p.split(",")) {
                        String clean = sub.trim().replace("user_", "");
                        if (!clean.isEmpty() && !resolvedParticipants.contains(clean)) {
                            resolvedParticipants.add(clean);
                        }
                    }
                }
            }
        }
        String cleanInvited = invitedUserId.replace("user_", "");
        if (!cleanInvited.isEmpty() && !resolvedParticipants.contains(cleanInvited)) {
            resolvedParticipants.add(cleanInvited);
        }
        String cleanSender = senderId.replace("user_", "");
        if (!cleanSender.isEmpty() && !resolvedParticipants.contains(cleanSender)) {
            resolvedParticipants.add(cleanSender);
        }

        return chatWriteService.sendMessage(
                senderId, conversationId, text, null, null, null, null, null,
                null, null, null, null, null, resolvedParticipants, actionType, actionPayload
        );
    }

    // ==========================================
    // E2EE SIGNAL PROTOCOL
    // ==========================================

    @PostMapping("/e2ee/prekeys/register")
    public ResponseEntity<Void> registerPrekeys(
            @RequestParam("userId") String userId,
            @RequestParam("bundleJson") String bundleJson) {

        String key = "e2ee_prekey_bundle:" + userId;
        redisTemplate.opsForValue().set(key, bundleJson);
        log.info("Successfully registered E2EE prekeys for user: {}", userId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/e2ee/prekeys/fetch")
    public ResponseEntity<Map<String, Object>> fetchPrekeyBundle(
            @RequestParam("userId") String userId,
            @RequestParam(value = "consume", required = false, defaultValue = "true") boolean consume) {
        String key = "e2ee_prekey_bundle:" + userId;
        String json = redisTemplate.opsForValue().get(key);
        Map<String, Object> response = new HashMap<>();

        if (json == null) {
            response.put("exists", false);
            return ResponseEntity.ok(response);
        }

        try {
            Map<String, Object> storedBundle = objectMapper.readValue(json, Map.class);

            // Dopasowane nazewnictwo do frontendu (klucz: preKeys zamiast oneTimePrekeys)
            List<Map<String, Object>> preKeys = (List<Map<String, Object>>) storedBundle.get("preKeys");
            Map<String, Object> poppedPreKey = null;

            if (preKeys != null && !preKeys.isEmpty()) {
                if (consume) {
                    poppedPreKey = preKeys.remove(0); // Zdejmujemy jeden klucz (Forward Secrecy)
                    storedBundle.put("preKeys", preKeys);
                    redisTemplate.opsForValue().set(key, objectMapper.writeValueAsString(storedBundle));
                } else {
                    poppedPreKey = preKeys.get(0); // Podgląd bez usuwania
                }
            }

            // Formatujemy obiekt idealnie pod strukturę `device.bundle` na kliencie
            Map<String, Object> bundle = new HashMap<>();
            bundle.put("registrationId", storedBundle.get("registrationId"));
            bundle.put("identityKey", storedBundle.get("identityKey"));
            bundle.put("signedPreKey", storedBundle.get("signedPreKey"));
            bundle.put("kyberPreKey", storedBundle.get("kyberPreKey"));
            bundle.put("preKey", poppedPreKey);

            response.put("exists", true);
            response.put("deviceId", storedBundle.getOrDefault("deviceId", 1));
            response.put("bundle", bundle);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Failed to fetch E2EE bundle for user: {}", userId, e);
            response.put("exists", false);
            response.put("error", "Internal server error reading the bundle");
            return ResponseEntity.status(500).body(response);
        }
    }

    @PostMapping("/e2ee/sender-key/register")
    public ResponseEntity<Void> registerSenderKey(
            @RequestParam("groupId") String groupId,
            @RequestParam("userId") String userId,
            @RequestParam("senderKeyCard") String senderKeyCard) {
        String key = "e2ee_sender_key:" + groupId + ":" + userId;
        redisTemplate.opsForValue().set(key, senderKeyCard);
        log.info("Successfully registered E2EE Sender Key for group: {}, user: {}", groupId, userId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/e2ee/sender-key/fetch")
    public ResponseEntity<Map<String, Object>> fetchSenderKey(
            @RequestParam("groupId") String groupId,
            @RequestParam("userId") String userId) {
        String key = "e2ee_sender_key:" + groupId + ":" + userId;
        String val = redisTemplate.opsForValue().get(key);
        Map<String, Object> response = new HashMap<>();
        if (val == null) {
            response.put("exists", false);
        } else {
            response.put("exists", true);
            response.put("senderKeyCard", val);
        }
        return ResponseEntity.ok(response);
    }

    @GetMapping("/e2ee/sender-key/fetch-all")
    public ResponseEntity<Map<String, String>> fetchAllSenderKeys(@RequestParam("groupId") String groupId) {
        Set<String> keys = redisTemplate.keys("e2ee_sender_key:" + groupId + ":*");
        Map<String, String> response = new HashMap<>();
        if (keys != null) {
            for (String key : keys) {
                String value = redisTemplate.opsForValue().get(key);
                String[] parts = key.split(":");
                if (parts.length >= 3) {
                    String userId = parts[2];
                    response.put(userId, value);
                }
            }
        }
        return ResponseEntity.ok(response);
    }

    // ==========================================
    // MEDIA / MINIO
    // ==========================================

    @PostMapping("/upload")
    public ResponseEntity<Map<String, String>> uploadChatFile(
            @RequestParam("file") MultipartFile file) {
        try {
            String objectName = minioService.uploadChatMedia(file);
            String presignedUrl = minioService.getPresignedObjectUrl(objectName);
            String base = stableUrlBase.endsWith("/") ? stableUrlBase.substring(0, stableUrlBase.length() - 1) : stableUrlBase;
            String stableUrl = base + "/api/chat/media/" + objectName;

            Map<String, String> response = new HashMap<>();
            response.put("presignedUrl", presignedUrl);
            response.put("url", stableUrl);
            response.put("stableUrl", stableUrl);
            response.put("objectKey", objectName);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error during file upload", e);
            Map<String, String> err = new HashMap<>();
            err.put("error", "Failed to upload file");
            return ResponseEntity.status(500).body(err);
        }
    }

    @GetMapping("/media/{key}")
    public ResponseEntity<InputStreamResource> downloadMedia(@PathVariable("key") String key) {
        try {
            io.minio.GetObjectResponse response = minioService.downloadChatMedia(key);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_TYPE, response.headers().get("Content-Type"))
                    .body(new InputStreamResource(response));
        } catch (Exception e) {
            log.error("Failed to download media with key: {}", key, e);
            return ResponseEntity.notFound().build();
        }
    }

    // ==========================================
    // UTILS
    // ==========================================

    private UUID toUuid(String idStr) {
        if (idStr == null) return null;
        try {
            return UUID.fromString(idStr);
        } catch (IllegalArgumentException e) {
            return UUID.nameUUIDFromBytes(idStr.getBytes());
        }
    }
}