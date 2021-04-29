package org.chameleoncloud;

import java.util.List;

import org.jboss.logging.Logger;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.AuthenticationFlowError;
import org.keycloak.authentication.authenticators.broker.AbstractIdpAuthenticator;
import org.keycloak.authentication.authenticators.broker.util.SerializedBrokeredIdentityContext;
import org.keycloak.broker.provider.BrokeredIdentityContext;
import org.keycloak.models.FederatedIdentityModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;

public class IdpLinkIdentitySetAuthenticator extends AbstractIdpAuthenticator {
    String IDENTITY_SET_CLAIM = "identity_set";
    private static Logger logger = Logger.getLogger(IdpLinkIdentitySetAuthenticator.class);

    @Override
    protected void authenticateImpl(AuthenticationFlowContext context, SerializedBrokeredIdentityContext serializedCtx,
            BrokeredIdentityContext brokerContext) {

        KeycloakSession session = context.getSession();
        RealmModel realm = context.getRealm();
        String providerId = brokerContext.getIdpConfig().getAlias();

        FederatedIdentityModel tokenIdentity = new FederatedIdentityModel(providerId, brokerContext.getId(),
                brokerContext.getUsername(), brokerContext.getToken());

        // Check login against stored identityAttributes
        String attrName = GlobusUserAttributeMapper.getSubAttribute(providerId, tokenIdentity.getUserId());
        List<UserModel> existingUsers = session.users().searchForUserByUserAttribute(attrName,
                tokenIdentity.getUserId(), realm);
        if (existingUsers.size() != 1) {
            // Fail if more than one candidate user is found
            context.failure(AuthenticationFlowError.USER_CONFLICT);
            return;
        }
        UserModel federatedUser = existingUsers.get(0);
        if (federatedUser != null) {
            // Link existing user to this token.
            session.users().removeFederatedIdentity(realm, federatedUser, providerId);
            context.setUser(federatedUser);
            context.success();
            return;
        }
        logger.warnv("No match in identity set for found for {0} username {1}", providerId,
                brokerContext.getUsername());
        context.attempted();
    }

    @Override
    protected void actionImpl(AuthenticationFlowContext context, SerializedBrokeredIdentityContext serializedCtx,
            BrokeredIdentityContext brokerContext) {
        authenticateImpl(context, serializedCtx, brokerContext);
    }

    @Override
    public boolean requiresUser() {
        return false;
    }

    @Override
    public boolean configuredFor(KeycloakSession session, RealmModel realm, UserModel user) {
        return false;
    }
}
