package ch.jacem.for_keycloak.email_otp_authenticator;

import org.keycloak.Config;
import org.keycloak.authentication.RequiredActionFactory;
import org.keycloak.authentication.RequiredActionProvider;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;

public class EmailOTPRequiredActionFactory implements RequiredActionFactory {
    private static final EmailOTPRequiredAction SINGLETON = new EmailOTPRequiredAction();

    @Override
    public String getDisplayText() {
        return "Email OTP Verification";
    }

    @Override
    public RequiredActionProvider create(KeycloakSession session) {
        return SINGLETON;
    }

    @Override
    public void init(Config.Scope config) {}

    @Override
    public void postInit(KeycloakSessionFactory factory) {}

    @Override
    public void close() {}

    @Override
    public String getId() {
        return EmailOTPRequiredAction.PROVIDER_ID;
    }
}
