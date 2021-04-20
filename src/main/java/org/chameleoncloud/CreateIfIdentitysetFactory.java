package org.chameleoncloud;

import org.keycloak.authentication.Authenticator;
import org.keycloak.authentication.authenticators.broker.IdpCreateUserIfUniqueAuthenticatorFactory;
import org.keycloak.models.KeycloakSession;

public class CreateIfIdentitysetFactory extends IdpCreateUserIfUniqueAuthenticatorFactory {

    public static final String PROVIDER_ID = "create-user-if-identityset";
    static CreateIfIdentityset SINGLETON = new CreateIfIdentityset();

    @Override
    public Authenticator create(KeycloakSession session) {
        return SINGLETON;
    }

    @Override
    public String getId() {
        return PROVIDER_ID;
    }

    @Override
    public String getDisplayType() {
        return "Create user if not in identity set.";
    }

    @Override
    public String getHelpText() {
        return "Detect if there is existing Keycloak account matching the IdP identity_set. Create a new one if no match.";
    }

}
