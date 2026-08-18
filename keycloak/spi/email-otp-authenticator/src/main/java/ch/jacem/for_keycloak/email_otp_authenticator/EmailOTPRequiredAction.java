package ch.jacem.for_keycloak.email_otp_authenticator;

import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;
import org.keycloak.authentication.RequiredActionContext;
import org.keycloak.authentication.RequiredActionProvider;
import org.keycloak.email.EmailTemplateProvider;
import org.keycloak.events.Errors;
import org.keycloak.forms.login.LoginFormsProvider;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.services.messages.Messages;
import org.keycloak.sessions.AuthenticationSessionModel;
import org.jboss.logging.Logger;

import java.security.MessageDigest;
import java.security.SecureRandom;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class EmailOTPRequiredAction implements RequiredActionProvider {
    private static final Logger logger = Logger.getLogger(EmailOTPRequiredAction.class);

    public static final String PROVIDER_ID = "email-otp-required-action";
    
    public static final String AUTH_NOTE_OTP_KEY = "for-kc-email-otp-required-key";
    public static final String AUTH_NOTE_OTP_CREATED_AT = "for-kc-email-otp-required-created-at";

    public static final String OTP_FORM_TEMPLATE_NAME = "login-email-otp.ftl";
    public static final String OTP_FORM_CODE_INPUT_NAME = "email-otp";
    public static final String OTP_FORM_RESEND_ACTION_NAME = "resend-email";

    public static final String OTP_EMAIL_TEMPLATE_NAME = "otp-email.ftl";
    public static final String OTP_EMAIL_SUBJECT_KEY = "emailOtpSubject";

    @Override
    public void evaluateTriggers(RequiredActionContext context) {
        UserModel user = context.getUser();
        if (user != null && user.getEmail() != null && !user.getEmail().isEmpty() && !user.isEmailVerified()) {
            context.getAuthenticationSession().addRequiredAction(PROVIDER_ID);
        }
    }

    @Override
    public void requiredActionChallenge(RequiredActionContext context) {
        // Generate and send OTP if not already sent
        generateAndSendOtp(context, false);

        // Show the form
        Response challenge = buildOtpForm(context, null, null);
        context.challenge(challenge);
    }

    @Override
    public void processAction(RequiredActionContext context) {
        MultivaluedMap<String, String> inputData = context.getHttpRequest().getDecodedFormParameters();
        AuthenticationSessionModel authSession = context.getAuthenticationSession();
        UserModel user = context.getUser();

        if (inputData.containsKey(OTP_FORM_RESEND_ACTION_NAME)) {
            logger.debug("Resending a new OTP in Required Action");
            generateAndSendOtp(context, true);
            context.challenge(buildOtpForm(context, null, null));
            return;
        }

        // Handle Change Email action from the modal
        if (inputData.containsKey("change-email")) {
            String newEmail = inputData.getFirst("new-email");
            if (newEmail == null || newEmail.trim().isEmpty() || !newEmail.contains("@")) {
                context.challenge(buildOtpForm(context, "errorInvalidEmail", null));
                return;
            }
            newEmail = newEmail.trim().toLowerCase();

            try {
                // Check duplicate emails
                if (!context.getRealm().isDuplicateEmailsAllowed()) {
                    UserModel existingUser = context.getSession().users().getUserByEmail(context.getRealm(), newEmail);
                    if (existingUser != null && !existingUser.getId().equals(user.getId())) {
                        context.challenge(buildOtpForm(context, "errorEmailAlreadyExists", null));
                        return;
                    }
                }

                // Check duplicate username if email is username
                if (context.getRealm().isRegistrationEmailAsUsername()) {
                    UserModel existingUser = context.getSession().users().getUserByUsername(context.getRealm(), newEmail);
                    if (existingUser != null && !existingUser.getId().equals(user.getId())) {
                        context.challenge(buildOtpForm(context, "errorEmailAlreadyExists", null));
                        return;
                    }
                    user.setUsername(newEmail);
                }

                user.setEmail(newEmail);
                user.setEmailVerified(false);

                logger.debugf("Email updated for user %s to %s, sending new OTP in Required Action", user.getId(), newEmail);
                generateAndSendOtp(context, true);

                context.challenge(buildOtpForm(context, null, null));
                return;
            } catch (Exception e) {
                logger.error("Failed to update email for user in Required Action", e);
                context.challenge(buildOtpForm(context, "errorEmailAlreadyExists", null));
                return;
            }
        }

        String otp = inputData.getFirst(OTP_FORM_CODE_INPUT_NAME);
        if (otp == null) {
            context.challenge(buildOtpForm(context, null, null));
            return;
        }

        String expectedOtp = authSession.getAuthNote(AUTH_NOTE_OTP_KEY);
        if (otp.isEmpty() || expectedOtp == null || !MessageDigest.isEqual(
                otp.getBytes(StandardCharsets.UTF_8),
                expectedOtp.getBytes(StandardCharsets.UTF_8))) {
            context.getEvent().user(user).error(Errors.INVALID_USER_CREDENTIALS);
            context.challenge(buildOtpForm(context, "errorInvalidEmailOtp", OTP_FORM_CODE_INPUT_NAME));
            return;
        }

        // Check expiration (10 minutes)
        if (isOtpExpired(context)) {
            generateAndSendOtp(context, true);
            context.getEvent().user(user).error(Errors.EXPIRED_CODE);
            context.challenge(buildOtpForm(context, "errorExpiredEmailOtp", OTP_FORM_CODE_INPUT_NAME));
            return;
        }

        // Success
        authSession.removeAuthNote(AUTH_NOTE_OTP_KEY);
        authSession.removeAuthNote(AUTH_NOTE_OTP_CREATED_AT);
        user.setEmailVerified(true);
        context.success();
    }

    private void generateAndSendOtp(RequiredActionContext context, boolean forceRegenerate) {
        AuthenticationSessionModel authSession = context.getAuthenticationSession();
        String existingOtp = authSession.getAuthNote(AUTH_NOTE_OTP_KEY);

        if (!forceRegenerate && existingOtp != null && !isOtpExpired(context)) {
            return;
        }

        // Generate 6 digit code
        String alphabet = "23456789ABCDEFGHJKLMNPQRSTUVWXYZ";
        int length = 6;
        SecureRandom secureRandom = new SecureRandom();
        StringBuilder otpBuilder = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            otpBuilder.append(alphabet.charAt(secureRandom.nextInt(alphabet.length())));
        }
        String otp = otpBuilder.toString();

        authSession.setAuthNote(AUTH_NOTE_OTP_CREATED_AT, String.valueOf(System.currentTimeMillis() / 1000));
        authSession.setAuthNote(AUTH_NOTE_OTP_KEY, otp);

        // Send e-mail
        UserModel user = context.getUser();
        try {
            Map<String, Object> attributes = new HashMap<>();
            int ttlSeconds = 600;
            attributes.put("otp", otp);
            attributes.put("ttl", ttlSeconds);
            attributes.put("ttlMinutes", ttlSeconds / 60);
            attributes.put("realm", context.getRealm());

            context.getSession()
                .getProvider(EmailTemplateProvider.class)
                .setRealm(context.getRealm())
                .setUser(user)
                .send(
                    OTP_EMAIL_SUBJECT_KEY,
                    OTP_EMAIL_TEMPLATE_NAME,
                    attributes
                );
            logger.debug("Required Action OTP email sent to " + user.getUsername());
        } catch (Exception e) {
            logger.error("Failed to send Required Action OTP email", e);
        }
    }

    private boolean isOtpExpired(RequiredActionContext context) {
        String createdAtStr = context.getAuthenticationSession().getAuthNote(AUTH_NOTE_OTP_CREATED_AT);
        if (createdAtStr == null) return true;
        long createdAt = Long.parseLong(createdAtStr);
        long now = System.currentTimeMillis() / 1000;
        return ((now - 600) > createdAt);
    }

    private Response buildOtpForm(RequiredActionContext context, String errorMessage, String field) {
        LoginFormsProvider form = context.form();

        if (errorMessage != null) {
            if (field != null) {
                form.addError(new org.keycloak.models.utils.FormMessage(field, errorMessage));
            } else {
                form.setError(errorMessage);
            }
        }

        UserModel user = context.getUser();
        if (user != null && user.getEmail() != null) {
            form.setAttribute("email", user.getEmail());
            form.setAttribute("maskedEmail", maskEmail(user.getEmail()));
        }
        form.setAttribute("deviceTrustEnabled", false); // Not supported in required action

        return form.createForm(OTP_FORM_TEMPLATE_NAME);
    }

    private String maskEmail(String email) {
        if (email == null || email.isEmpty()) return email;
        int atIndex = email.indexOf('@');
        if (atIndex < 0) return email;
        String local = email.substring(0, atIndex);
        String domain = email.substring(atIndex + 1);
        if (local.isEmpty() || domain.isEmpty()) return email;
        String maskedLocal = local.substring(0, Math.min(2, local.length())) + "***";
        int lastDotIndex = domain.lastIndexOf('.');
        String maskedDomain;
        if (lastDotIndex < 0) {
            maskedDomain = domain.substring(0, Math.min(2, domain.length())) + "***";
        } else {
            String preTld = domain.substring(0, lastDotIndex);
            String tld = domain.substring(lastDotIndex + 1);
            maskedDomain = preTld.substring(0, Math.min(2, preTld.length())) + "***." + tld;
        }
        return maskedLocal + "@" + maskedDomain;
    }

    @Override
    public void close() {}
}
