package org.chameleoncloud;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Map;

import org.chameleoncloud.representations.GlobusIdentity;

import org.jboss.logging.Logger;

import org.keycloak.authentication.actiontoken.idpverifyemail.IdpVerifyAccountLinkActionToken;

import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.authenticators.broker.AbstractIdpAuthenticator;
import org.keycloak.authentication.authenticators.broker.IdpAutoLinkAuthenticator;
import org.keycloak.authentication.authenticators.broker.IdpConfirmLinkAuthenticator;
import org.keycloak.authentication.authenticators.broker.IdpEmailVerificationAuthenticator;
import org.keycloak.authentication.authenticators.broker.IdpCreateUserIfUniqueAuthenticator;
import org.keycloak.authentication.authenticators.broker.util.ExistingUserInfo;
import org.keycloak.authentication.authenticators.broker.util.SerializedBrokeredIdentityContext;

import org.keycloak.broker.oidc.OIDCIdentityProvider;
import org.keycloak.broker.provider.BrokeredIdentityContext;
import org.keycloak.broker.provider.IdentityProvider;

import org.keycloak.models.FederatedIdentityModel;
import org.keycloak.models.IdentityProviderModel;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;

import org.keycloak.models.KeycloakSession;
import org.keycloak.sessions.AuthenticationSessionModel;

import org.keycloak.representations.JsonWebToken;

public class CreateIfIdentityset extends AbstractIdpAuthenticator {

    String IDENTITY_SET_CLAIM = "identity_set";
    String GLOBUS_ALIAS = "globus";

    private static Logger logger = Logger.getLogger(CreateIfIdentityset.class);

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

    @Override
    protected void authenticateImpl(AuthenticationFlowContext context, SerializedBrokeredIdentityContext serializedCtx,
            BrokeredIdentityContext brokerContext) {

        KeycloakSession session = context.getSession();
        RealmModel realm = context.getRealm();
        AuthenticationSessionModel authSession = context.getAuthenticationSession();
        String providerId = brokerContext.getIdpConfig().getAlias();

        UserModel existingUser = findUserByIdentitySet(session, realm, brokerContext);

        if (existingUser != null) {
            // Build identity from from matching user
            logger.debugf("found matching user with username '%s' and email '%s'", existingUser.getUsername(),
                    existingUser.getEmail());

            // Remove existing link. TODO: This is dangerous.
            logger.warnf("removing linked identity '%s' with user '%s'", providerId, existingUser.getId());
            session.users().removeFederatedIdentity(realm, existingUser, providerId);

            logger.debugf(
                    "User '%s' is set to authentication context when link with identity provider '%s' . Identity provider username is '%s' ",
                    existingUser.getUsername(), brokerContext.getIdpConfig().getAlias(), brokerContext.getUsername());
            context.setUser(existingUser);
            context.success();
        } else {
            // no match found, return to next in flow
            context.attempted();
        }
    }

    /*
     * This method will return an ExistingUserInfo if a matching federated identity
     * is found in the identity set. If found, that status is passed on until later
     * in the flow, and the duplicate handled. However, in the case that a match is
     * not found, the superclass will create a new user.
     * 
     * TODO: keycloak cannot handle duplicate entries for the same IdP type. This
     * will unconditionally remove the linked identity. This must be moved to after
     * the email confirmation step, as the process can be interupted partway,
     * leaving orphan accounts.
     */
    protected UserModel findUserByIdentitySet(KeycloakSession session, RealmModel realm,
            BrokeredIdentityContext brokerContext) {

        logger.debug("Entered findConflictingUser");

        // Unpack data from broker response
        String providerId = brokerContext.getIdpConfig().getAlias();
        List<GlobusIdentity> identity_set = extracted(brokerContext);

        // Check each identity in the set against the list of existing users in keycloak
        // If a match is found, return the match, otherwise, return null
        for (GlobusIdentity identity : identity_set) {
            // Build identity from token
            FederatedIdentityModel tokenIdentity = new FederatedIdentityModel(providerId, identity.getSub(),
                    identity.getUsername(), brokerContext.getToken());
            // Find conflicting user
            UserModel federatedUser = session.users().getUserByFederatedIdentity(tokenIdentity, realm);
            logger.debugf("searching for '%s' user with username '%s'", providerId, identity.getUsername());
            return federatedUser;
        }

        logger.debugf("Federated user not found for provider '%s' and broker username '%s'", providerId,
                brokerContext.getUsername());
        // If null is return, depending on the flow, keycloak will create a new user
        return null;
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
