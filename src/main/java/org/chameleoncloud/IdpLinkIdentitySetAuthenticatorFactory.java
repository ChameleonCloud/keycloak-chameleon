package org.chameleoncloud;

import java.util.List;
import org.keycloak.Config;
import org.keycloak.authentication.Authenticator;
import org.keycloak.authentication.AuthenticatorFactory;
import org.keycloak.models.AuthenticationExecutionModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.provider.ProviderConfigProperty;

public class IdpLinkIdentitySetAuthenticatorFactory implements AuthenticatorFactory {

    public static final String PROVIDER_ID = "link-user-in-identityset";
    private static final IdpLinkIdentitySetAuthenticator SINGLETON = new IdpLinkIdentitySetAuthenticator();

    @Override
    public Authenticator create(KeycloakSession session) {
        return SINGLETON;
    }

    @Override
    public String getId() {
        return PROVIDER_ID;
    }

    @Override
    public void init(Config.Scope config) {

    }

    @Override
    public void postInit(KeycloakSessionFactory factory) {

    }

    @Override
    public void close() {

    }

    @Override
    public String getReferenceCategory() {
        return "autoLink";
    }

    @Override
    public String getDisplayType() {
        return "IdP Link Identity if in IdentitySet";
    }

    @Override
    public String getHelpText() {
        return "If existing user has linked identity in the identity set, override that link.";
    }

    @Override
    public AuthenticationExecutionModel.Requirement[] getRequirementChoices() {
        return REQUIREMENT_CHOICES;
    }

    @Override
    public boolean isUserSetupAllowed() {
        return false;
    }

    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        return null;
    }

    @Override
    public boolean isConfigurable() {
        return false;
    }

}
