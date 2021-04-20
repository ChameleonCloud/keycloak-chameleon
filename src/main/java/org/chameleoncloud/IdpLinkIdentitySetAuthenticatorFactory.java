package org.chameleoncloud;

import org.keycloak.authentication.Authenticator;
import org.keycloak.authentication.authenticators.broker.IdpCreateUserIfUniqueAuthenticatorFactory;
import org.keycloak.models.KeycloakSession;

public class IdpLinkIdentitySetAuthenticatorFactory extends IdpCreateUserIfUniqueAuthenticatorFactory {

    public static final String PROVIDER_ID = "link-user-in-identityset";
    static IdpLinkIdentitySetAuthenticator SINGLETON = new IdpLinkIdentitySetAuthenticator();

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
        return "IdP Link Identity if in IdentitySet";
    }

    @Override
    public String getHelpText() {
        return "If existing user has linked identity in the identity set, override that link.";
    }

}
