package com.example.register;

import org.keycloak.authentication.FormAction;
import org.keycloak.authentication.FormContext;
import org.keycloak.authentication.ValidationContext;
import org.keycloak.forms.login.LoginFormsProvider;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.events.Errors;
import org.jboss.logging.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import jakarta.ws.rs.core.MultivaluedMap;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class UserDataRegistrationAction implements FormAction {

    private static final Logger logger = Logger.getLogger(UserDataRegistrationAction.class);

    // Używamy statycznego obiektu ObjectMapper i klienta HTTP dla optymalizacji
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();

    @Override
    public void buildPage(FormContext context, LoginFormsProvider form) {
        // Formularz renderowany przez Keycloakify
    }

    @Override
    public void validate(ValidationContext context) {
        logger.info("UserDataRegistrationAction: validating registration form");
        MultivaluedMap<String, String> formData = context.getHttpRequest().getDecodedFormParameters();

        String userServiceUrl = null;
        if (context.getAuthenticatorConfig() != null) {
            userServiceUrl = context.getAuthenticatorConfig().getConfig().get(UserDataRegistrationActionFactory.CONFIG_USER_SERVICE_URL);
        }

        if (userServiceUrl == null || userServiceUrl.trim().isEmpty()) {
            userServiceUrl = "http://127.0.0.1:5000/register-user";
        }

        // Build payload to send to user-service
        String keycloakUserId = context.getUser() != null ? context.getUser().getId() : null;
        String payload = buildJsonPayload(formData, keycloakUserId);
        logger.infof("Sending registration data to user-service: %s", userServiceUrl);

        boolean success = sendToEndpoint(userServiceUrl, payload);

        if (!success) {
            logger.warn("User-service rejected registration");
            context.error(Errors.INVALID_REGISTRATION);
            return;
        }

        logger.info("User-service confirmed registration. Keycloak accepts user.");
        context.success();
    }

    @Override
    public void success(FormContext context) {
        logger.info("UserDataRegistrationAction: registration successful");
    }

    private String buildJsonPayload(MultivaluedMap<String, String> formData, String keycloakUserId) {
        ObjectNode json = MAPPER.createObjectNode();

        // Add Keycloak user ID
        if (keycloakUserId != null) {
            json.put("keycloakUserId", keycloakUserId);
        }

        if (formData != null) {
            for (String key : formData.keySet()) {
                // Skip password fields and temporary/internal fields
                if (key.equalsIgnoreCase("password") || 
                    key.equalsIgnoreCase("password-confirm") ||
                    key.equalsIgnoreCase("recaptcha") ||
                    key.equalsIgnoreCase("terms-agree")) {
                    continue;
                }
                
                String val = formData.getFirst(key);
                if (val != null && !val.trim().isEmpty()) {
                    json.put(key, val);
                }
            }
        }
        
        return json.toString();
    }

    private boolean sendToEndpoint(String endpointUrl, String jsonPayload) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(endpointUrl))
                    .timeout(Duration.ofSeconds(5))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                    .build();

            HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
            logger.infof("Odpowiedź z backendu: Status=%d", response.statusCode());

            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                String body = response.body();
                if (body != null) {
                    body = body.trim().toLowerCase();
                    if (body.equals("false") || body.contains("\"status\":\"false\"") || body.contains("\"success\":false")) {
                        return false;
                    }
                }
                return true;
            }
            return false;
        } catch (Exception e) {
            logger.error("Błąd połączenia z Orchestratorem: " + e.getMessage(), e);
            return false;
        }
    }

    @Override
    public boolean requiresUser() {
        return false;
    }

    @Override
    public boolean configuredFor(KeycloakSession session, RealmModel realm, UserModel user) {
        return true;
    }

    @Override
    public void setRequiredActions(KeycloakSession session, RealmModel realm, UserModel user) {
    }

    @Override
    public void close() {
    }
}