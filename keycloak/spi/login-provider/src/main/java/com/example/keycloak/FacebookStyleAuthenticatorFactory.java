package com.example.keycloak;

import org.keycloak.Config;
import org.keycloak.authentication.Authenticator;
import org.keycloak.authentication.AuthenticatorFactory;
import org.keycloak.models.AuthenticationExecutionModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.provider.ProviderConfigProperty;

import java.util.Collections;
import java.util.List;

public class FacebookStyleAuthenticatorFactory implements AuthenticatorFactory {

    public static final String PROVIDER_ID = "facebook-style-authenticator";
    private static final FacebookStyleAuthenticator SINGLETON = new FacebookStyleAuthenticator();

    @Override
    public String getDisplayType() {
        return "Facebook-style Device Token Authenticator";
    }

    @Override
    public String getReferenceCategory() {
        return "device-auth";
    }

    @Override
    public boolean isConfigurable() {
        return false;
    }

    @Override
    public AuthenticationExecutionModel.Requirement[] getRequirementChoices() {
        return new AuthenticationExecutionModel.Requirement[] {
            AuthenticationExecutionModel.Requirement.REQUIRED,
            AuthenticationExecutionModel.Requirement.ALTERNATIVE,
            AuthenticationExecutionModel.Requirement.DISABLED
        };
    }

    @Override
    public boolean isUserSetupAllowed() {
        return false;
    }

    @Override
    public String getHelpText() {
        return "Allows one-click login on remembered devices after logout using a secure signed device cookie.";
    }

    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        return Collections.emptyList();
    }

    @Override
    public Authenticator create(KeycloakSession session) {
        return SINGLETON;
    }

    @Override
    public void init(Config.Scope config) {
        // No init config needed
    }

    @Override
    public void postInit(KeycloakSessionFactory factory) {
        // No postInit needed
    }

    @Override
    public void close() {
        // No close needed
    }

    @Override
    public String getId() {
        return PROVIDER_ID;
    }
}
