package com.example.keycloak;

import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.authenticators.browser.UsernamePasswordForm;
import org.keycloak.models.UserModel;

import jakarta.ws.rs.core.Cookie;

import java.security.MessageDigest;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class FacebookStyleAuthenticator extends UsernamePasswordForm {

    private static final String DEVICE_TOKENS_ATTR = "device_tokens";
    private static final int MAX_DEVICES_PER_USER = 5;
    private static final String ACTUAL_LOA_NOTE = "actual-loa";

    @Override
    public void authenticate(AuthenticationFlowContext context) {
        String loginHint = context.getUriInfo().getQueryParameters().getFirst("login_hint");
        String deviceLogin = context.getUriInfo().getQueryParameters().getFirst("device_login");

        if (loginHint != null && "true".equalsIgnoreCase(deviceLogin)) {
            boolean secure = context.getUriInfo().getRequestUri().getScheme().equalsIgnoreCase("https");
            String prefix = secure ? "__Secure-device_auth_" : "device_auth_";
            String cookieName = prefix + hashUsername(loginHint.trim().toLowerCase());
            Cookie cookie = context.getHttpRequest().getHttpHeaders().getCookies().get(cookieName);

            if (cookie != null) {
                String tokenValue = cookie.getValue();
                UserModel user = context.getSession().users().getUserByUsername(context.getRealm(), loginHint);

                if (user != null && user.isEnabled() && verifyToken(user, tokenValue, context)) {
                    context.setUser(user);
                    // Mark authentication session as LoA 1 (one-click login)
                    context.getAuthenticationSession().setAuthNote(ACTUAL_LOA_NOTE, "1");
                    context.success();
                    return; // Auto-login successful, stop flow
                }
            }
        }

        // Fall back to standard username/password form
        super.authenticate(context);
    }

    @Override
    public void action(AuthenticationFlowContext context) {
        super.action(context);

        // If login succeeded, issue the device cookie and set LoA to 2 (password login)
        if (context.getUser() != null) {
            context.getAuthenticationSession().setAuthNote(ACTUAL_LOA_NOTE, "2");
            setDeviceTokenCookie(context, context.getUser());
        }
    }

    private void setDeviceTokenCookie(AuthenticationFlowContext context, UserModel user) {
        String username = user.getUsername().trim().toLowerCase();
        String tokenId = UUID.randomUUID().toString();
        
        // Save the token ID in Keycloak user attributes for stateful revocation
        List<String> activeTokens = user.getAttributeStream(DEVICE_TOKENS_ATTR)
                .collect(Collectors.toCollection(ArrayList::new));
        
        // Evict oldest device if user exceeds limit
        if (activeTokens.size() >= MAX_DEVICES_PER_USER) {
            activeTokens.remove(0);
        }
        activeTokens.add(tokenId);
        user.setAttribute(DEVICE_TOKENS_ATTR, activeTokens);

        // Path should match Keycloak's base auth path
        String path = context.getUriInfo().getBaseUri().getPath();

        // Cookie lifetime: 90 days
        int maxAge = 3600 * 24 * 90;

        boolean secure = context.getUriInfo().getRequestUri().getScheme().equalsIgnoreCase("https");
        String userAgent = getUserAgent(context);

        String token = generateToken(username, tokenId, userAgent);
        String prefix = secure ? "__Secure-device_auth_" : "device_auth_";
        String cookieName = prefix + hashUsername(username);

        // Manually build the Set-Cookie header value for maximum compatibility
        StringBuilder cookieHeader = new StringBuilder();
        cookieHeader.append(cookieName).append("=").append(token);
        cookieHeader.append("; Path=").append(path);
        cookieHeader.append("; Max-Age=").append(maxAge);
        if (secure) {
            cookieHeader.append("; Secure");
        }
        cookieHeader.append("; HttpOnly");
        cookieHeader.append("; SameSite=Lax");

        context.getSession().getContext().getHttpResponse().addHeader("Set-Cookie", cookieHeader.toString());
    }

    private String getSecret() {
        String secret = System.getenv("FACEBOOK_LOGIN_SECRET");
        if (secret == null || secret.trim().isEmpty()) {
            secret = System.getProperty("facebook.login.secret");
        }
        if (secret == null || secret.trim().isEmpty()) {
            throw new IllegalStateException("FACEBOOK_LOGIN_SECRET environment variable or facebook.login.secret system property is not set. For security reasons, a secret must be configured.");
        }
        return secret;
    }

    private String generateToken(String username, String tokenId, String userAgent) {
        long expirationTime = System.currentTimeMillis() + (1000L * 3600 * 24 * 90); // 90 days
        String data = username + ":" + tokenId + ":" + expirationTime + ":" + userAgent;
        String signature = hmacSha256(data, getSecret());
        return username + ":" + tokenId + ":" + expirationTime + ":" + signature;
    }

    private boolean verifyToken(UserModel user, String tokenValue, AuthenticationFlowContext context) {
        if (tokenValue == null || tokenValue.isEmpty()) {
            return false;
        }
        String[] parts = tokenValue.split(":");
        if (parts.length != 4) {
            return false;
        }
        String tokenUsername = parts[0];
        String tokenId = parts[1];
        String expirationStr = parts[2];
        String signature = parts[3];

        if (!tokenUsername.equalsIgnoreCase(user.getUsername())) {
            return false;
        }

        try {
            long expirationTime = Long.parseLong(expirationStr);
            if (System.currentTimeMillis() > expirationTime) {
                return false; // Token expired
            }
        } catch (NumberFormatException e) {
            return false;
        }

        // 1. Verify JAX-RS User-Agent binding
        String userAgent = getUserAgent(context);
        String data = tokenUsername + ":" + tokenId + ":" + expirationStr + ":" + userAgent;
        String expectedSignature = hmacSha256(data, getSecret());

        if (!MessageDigest.isEqual(
                signature.getBytes(StandardCharsets.UTF_8),
                expectedSignature.getBytes(StandardCharsets.UTF_8))) {
            return false; // Signature mismatch
        }

        // 2. Verify stateful Token ID in Keycloak user attributes (allows remote revocation)
        List<String> activeTokens = user.getAttributeStream(DEVICE_TOKENS_ATTR)
                .collect(Collectors.toList());
        
        return activeTokens.contains(tokenId);
    }

    private String getUserAgent(AuthenticationFlowContext context) {
        String userAgent = context.getHttpRequest().getHttpHeaders().getHeaderString("User-Agent");
        return userAgent != null ? userAgent.trim() : "";
    }

    private String hmacSha256(String data, String key) {
        try {
            javax.crypto.Mac sha256_HMAC = javax.crypto.Mac.getInstance("HmacSHA256");
            javax.crypto.spec.SecretKeySpec secret_key = new javax.crypto.spec.SecretKeySpec(
                key.getBytes(StandardCharsets.UTF_8),
                "HmacSHA256"
            );
            sha256_HMAC.init(secret_key);
            byte[] raw = sha256_HMAC.doFinal(data.getBytes(StandardCharsets.UTF_8));

            StringBuilder sb = new StringBuilder();
            for (byte b : raw) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("Failed to calculate HMAC SHA256", e);
        }
    }

    private String hashUsername(String username) {
        return hmacSha256(username.trim().toLowerCase(), getSecret());
    }
}
