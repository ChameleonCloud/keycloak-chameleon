package org.chameleoncloud;

// import com.fasterxml.jackson.databind.JsonNode;
// import org.keycloak.broker.oidc.mappers.AbstractClaimMapper;
// import org.keycloak.broker.oidc.mappers.UserAttributeMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.chameleoncloud.representations.GlobusIdentity;

import org.jboss.logging.Logger;

import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.authenticators.broker.IdpCreateUserIfUniqueAuthenticator;
import org.keycloak.authentication.authenticators.broker.util.ExistingUserInfo;
import org.keycloak.authentication.authenticators.broker.util.SerializedBrokeredIdentityContext;

import org.keycloak.authorization.policy.evaluation.Realm;

import org.keycloak.broker.oidc.OIDCIdentityProvider;
import org.keycloak.broker.provider.BrokeredIdentityContext;

import org.keycloak.common.util.ObjectUtil;

import org.keycloak.models.FederatedIdentityModel;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.UserProvider;
import org.keycloak.models.UserSessionModel;
import org.keycloak.models.IdentityProviderModel;
import org.keycloak.models.KeycloakSession;

import org.keycloak.sessions.AuthenticationSessionModel;

import org.keycloak.services.managers.AuthenticationSessionManager;

import org.keycloak.representations.JsonWebToken;

public class CreateIfIdentityset extends IdpCreateUserIfUniqueAuthenticator {

    String IDENTITY_SET_CLAIM = "identity_set";
    String GLOBUS_ALIAS = "globus";

    private static Logger logger = Logger.getLogger(CreateIfIdentityset.class);

    @Override
    protected ExistingUserInfo checkExistingUser(AuthenticationFlowContext context, String username,
            SerializedBrokeredIdentityContext serializedCtx, BrokeredIdentityContext brokerContext) {
        // find matching user, and overwrite stored token with the token from the
        // request

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
            // Find conflicting user
            UserModel conflictingUser = cachedUsers.getUserByFederatedIdentity(tokenIdentity, realm);
            if (conflictingUser != null) {
                // Build identity from from matching user
                FederatedIdentityModel storedIdentity = cachedUsers.getFederatedIdentity(conflictingUser, providerId,
                        realm);

                // Update stored token
                updateToken(context.getSession(), brokerContext, conflictingUser, storedIdentity, realm);

                // Update brokered user
                /*
                 * brokerContext.getIdp().updateBrokeredUser(context.getSession(), realm,
                 * conflictingUser, brokerContext); set_authenticated_user;
                 */
                context.success();
                return null;
            } else {
                logger.debugf("Federated user not found for provider '%s' and broker username '%s'", providerId,
                        brokerContext.getUsername());
            }
        }

        // If null is return, depending on the flow, keycloak will create a new user
        context.attempted();
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

    // Look up existing user by list of methods
    private UserModel find_existing_user(GlobusIdentity identity, UserProvider users, RealmModel realm) {

        String globusUsername = identity.getUsername();
        String globusSub = identity.getSub();
        String globusEmail = identity.getEmail();

        // Create federated identity from the token identity
        FederatedIdentityModel globusID = new FederatedIdentityModel(GLOBUS_ALIAS, globusSub, globusUsername);

        // Check for existing user matching the federated identity
        UserModel existingUserByFedId = users.getUserByFederatedIdentity(globusID, realm);
        if (existingUserByFedId != null) {
            return existingUserByFedId;
        }

        // Check for matching email in identity_set
        UserModel existingUserByEmail = users.getUserByEmail(globusEmail, realm);
        if (existingUserByEmail != null) {
            return existingUserByEmail;
        }

        // Check for matching username in identity_set
        UserModel existingUserByUsername = users.getUserByUsername(globusUsername, realm);
        if (existingUserByUsername != null) {
            return existingUserByUsername;
        }

        return null;
    }

    private void objectToJsonLog(Object input) {

        ObjectMapper objectMapper = new ObjectMapper();

        try {
            String json = objectMapper.writeValueAsString(input);
            logger.warn(json);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }
}
