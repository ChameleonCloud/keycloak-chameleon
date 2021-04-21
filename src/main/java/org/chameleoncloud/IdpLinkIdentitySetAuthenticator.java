package org.chameleoncloud;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jboss.logging.Logger;
import java.util.List;
import java.util.Map;

import org.chameleoncloud.representations.GlobusIdentity;

import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.authenticators.broker.AbstractIdpAuthenticator;
import org.keycloak.authentication.authenticators.broker.util.ExistingUserInfo;
import org.keycloak.authentication.authenticators.broker.util.SerializedBrokeredIdentityContext;

import org.keycloak.broker.oidc.OIDCIdentityProvider;
import org.keycloak.broker.provider.BrokeredIdentityContext;

import org.keycloak.models.FederatedIdentityModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.UserProvider;
import org.keycloak.models.RealmModel;
import org.keycloak.models.KeycloakSession;

import org.keycloak.representations.JsonWebToken;

import org.keycloak.sessions.AuthenticationSessionModel;

public class IdpLinkIdentitySetAuthenticator extends AbstractIdpAuthenticator {

    String IDENTITY_SET_CLAIM = "identity_set";
    // String GLOBUS_ALIAS = "globus";

    private static Logger logger = Logger.getLogger(IdpLinkIdentitySetAuthenticator.class);

    @Override
    protected void authenticateImpl(AuthenticationFlowContext context, SerializedBrokeredIdentityContext serializedCtx,
            BrokeredIdentityContext brokerContext) {

        KeycloakSession session = context.getSession();
        RealmModel realm = context.getRealm();
        AuthenticationSessionModel authSession = context.getAuthenticationSession();

        String providerId = brokerContext.getIdpConfig().getAlias();
        UserProvider cachedUsers = session.users();

        FederatedIdentityModel newIdentity = new FederatedIdentityModel(providerId, brokerContext.getId(),
                brokerContext.getUsername(), brokerContext.getToken());

        List<GlobusIdentity> identitySet = getIdentitiesFromToken(brokerContext);
        for (GlobusIdentity identity : identitySet) {

            // Check linked identities for match. If found, overwrite to prevent DB error
            // This match implies that the broker is already authenticated for that
            // identity, so this is safe without confirmation.
            UserModel federatedUser = getUserByGlobusID(identity, cachedUsers, providerId, realm);
            if (federatedUser != null) {
                // Overwrite linked provider ID with replacement
                cachedUsers.removeFederatedIdentity(realm, federatedUser, providerId);
                context.setUser(federatedUser);
                context.success();
                return;
            }

            // Check keycloak username for match, and set flag for later handling
            UserModel userByUsername = cachedUsers.getUserByUsername(identity.getUsername(), realm);
            if (userByUsername != null) {
                ExistingUserInfo existingUser = new ExistingUserInfo(userByUsername.getId(), UserModel.USERNAME,
                        userByUsername.getUsername());
                authSession.setAuthNote(EXISTING_USER_INFO, existingUser.serialize());
                context.attempted();
                return;
            }

            // Check keycloak email for match, and set flag for later handling
            // Only run if email present, and emails are unique
            if (identity.getEmail() != null && !realm.isDuplicateEmailsAllowed()) {
                UserModel userByEmail = cachedUsers.getUserByEmail(identity.getEmail(), realm);
                if (userByEmail != null) {
                    ExistingUserInfo existingUser = new ExistingUserInfo(userByEmail.getId(), UserModel.EMAIL,
                            userByEmail.getEmail());
                    authSession.setAuthNote(EXISTING_USER_INFO, existingUser.serialize());
                    context.attempted();
                    return;
                }
            }

        }

        logger.warnv("No match in identity set for found for {0} username {1}", providerId,
                brokerContext.getUsername());
        context.attempted();
    }

    private UserModel getUserByGlobusID(GlobusIdentity identity, UserProvider cachedUsers, String providerId,
            RealmModel realm) {

        // create model from identity parameters
        FederatedIdentityModel oldIdentity = new FederatedIdentityModel(providerId, identity.getSub(),
                identity.getUsername());
        logger.warnv("Checking for existing users with {0} sub matching {1}", oldIdentity.getIdentityProvider(),
                oldIdentity.getUserId());
        UserModel federatedUser = cachedUsers.getUserByFederatedIdentity(oldIdentity, realm);
        if (federatedUser != null) {
            logger.warnv("Username {0} has existing link with provider {1}, removing linked id {2}",
                    federatedUser.getUsername(), providerId, oldIdentity.getUserId());
            return federatedUser;
        } else {
            logger.warnv("No match found for {0} username {1}", oldIdentity.getIdentityProvider(),
                    oldIdentity.getUserName());
        }
        return null;

    }

    private List<GlobusIdentity> getIdentitiesFromToken(BrokeredIdentityContext brokerContext) {
        JsonWebToken token = (JsonWebToken) brokerContext.getContextData().get(OIDCIdentityProvider.VALIDATED_ID_TOKEN);
        Map<String, Object> otherClaims = token.getOtherClaims();
        ObjectMapper mapper = new ObjectMapper();
        List<GlobusIdentity> identity_set = mapper.convertValue(otherClaims.get(this.IDENTITY_SET_CLAIM),
                new TypeReference<List<GlobusIdentity>>() {
                });
        return identity_set;
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
