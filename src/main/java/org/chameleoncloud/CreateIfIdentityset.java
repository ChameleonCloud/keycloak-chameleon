package org.chameleoncloud;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Map;

import org.chameleoncloud.representations.GlobusIdentity;

import org.jboss.logging.Logger;

import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.authenticators.broker.IdpCreateUserIfUniqueAuthenticator;
import org.keycloak.authentication.authenticators.broker.util.ExistingUserInfo;
import org.keycloak.authentication.authenticators.broker.util.SerializedBrokeredIdentityContext;

import org.keycloak.broker.oidc.OIDCIdentityProvider;
import org.keycloak.broker.provider.BrokeredIdentityContext;

import org.keycloak.common.util.ObjectUtil;

import org.keycloak.models.FederatedIdentityModel;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.UserProvider;

import org.keycloak.models.KeycloakSession;

import org.keycloak.representations.JsonWebToken;

public class CreateIfIdentityset extends IdpCreateUserIfUniqueAuthenticator {

    String IDENTITY_SET_CLAIM = "identity_set";
    String GLOBUS_ALIAS = "globus";

    private static Logger logger = Logger.getLogger(CreateIfIdentityset.class);

    @Override
    protected void authenticateImpl(AuthenticationFlowContext context, SerializedBrokeredIdentityContext serializedCtx,
            BrokeredIdentityContext brokerContext) {

        KeycloakSession session = context.getSession();
        RealmModel realm = context.getRealm();

        UserModel federatedUser = getExistingUser(context, serializedCtx, brokerContext);
        if (federatedUser != null) {
            // Update brokered user

            // Update stored token
            // FederatedIdentityModel storedIdentity =
            // session.users().getFederatedIdentity(federatedUser,
            // brokerContext.getIdpConfig().getAlias(), realm);

            // updateToken(context.getSession(), brokerContext, federatedUser,
            // storedIdentity, context.getRealm());
            // brokerContext.getIdp().updateBrokeredUser(context.getSession(), realm,
            // federatedUser, brokerContext);

            context.setUser(federatedUser);
            context.success();
            return;

        } else {
            context.attempted();
            return;
        }

    }

    protected UserModel getExistingUser(AuthenticationFlowContext context,
            SerializedBrokeredIdentityContext serializedCtx, BrokeredIdentityContext brokerContext) {
        // find matching user, overwrite stored token with the token from the request
        logger.debug("Entered ExistingUserInfo");
        // Get properties from contexts
        String providerId = brokerContext.getIdpConfig().getAlias();
        UserProvider cachedUsers = context.getSession().users();
        RealmModel realm = context.getRealm();

        // Unpack data from broker response
        List<GlobusIdentity> identity_set = extracted(brokerContext);
        // Check each identity in the set against the list of existing users in keycloak
        // If a match is found, return the match, otherwise, return null
        for (GlobusIdentity identity : identity_set) {
            // Build identity from token
            FederatedIdentityModel tokenIdentity = new FederatedIdentityModel(providerId, identity.getSub(),
                    identity.getUsername(), brokerContext.getToken());
            logger.debugf("searching for '%s' user with username '%s'", providerId, identity.getUsername());
            // Find conflicting user
            UserModel conflictingUser = cachedUsers.getUserByFederatedIdentity(tokenIdentity, realm);
            if (conflictingUser != null) {
                // Build identity from from matching user
                logger.debugf("found matching user with username '%s' and email '%s'", conflictingUser.getUsername(),
                        conflictingUser.getEmail());
                return conflictingUser;
            } else {
                logger.debugf("Federated user not found for provider '%s' and broker username '%s'", providerId,
                        brokerContext.getUsername());
            }
        }

        // If null is return, depending on the flow, keycloak will create a new user
        return null;
    }

    // If storing tokens is enabled, and tokens are non-null, update the tokens
    private void updateToken(KeycloakSession session, BrokeredIdentityContext context, UserModel federatedUser,
            FederatedIdentityModel storedIdentity, RealmModel realm) {
        if (context.getIdpConfig().isStoreToken()
                && !ObjectUtil.isEqualOrBothNull(context.getToken(), storedIdentity.getToken())) {
            storedIdentity.setToken(context.getToken());

            session.users().updateFederatedIdentity(realm, federatedUser, storedIdentity);

            logger.debugf("Identity [%s] update with response from identity provider [%s].", federatedUser,
                    context.getIdpConfig().getAlias());

        }
    }

    private List<GlobusIdentity> extracted(BrokeredIdentityContext brokerContext) {
        JsonWebToken token = (JsonWebToken) brokerContext.getContextData().get(OIDCIdentityProvider.VALIDATED_ID_TOKEN);
        Map<String, Object> otherClaims = token.getOtherClaims();
        ObjectMapper mapper = new ObjectMapper();
        List<GlobusIdentity> identity_set = mapper.convertValue(otherClaims.get(this.IDENTITY_SET_CLAIM),
                new TypeReference<List<GlobusIdentity>>() {
                });
        return identity_set;
    }
}
