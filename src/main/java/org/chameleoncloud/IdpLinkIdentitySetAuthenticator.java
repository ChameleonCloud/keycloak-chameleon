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

        List<GlobusIdentity> identitySet = getIdentitiesFromToken(brokerContext);

        FederatedIdentityModel newIdentity = new FederatedIdentityModel(providerId, brokerContext.getId(),
                brokerContext.getUsername(), brokerContext.getToken());

        for (GlobusIdentity identity : identitySet) {
            // create model from identity parameters
            FederatedIdentityModel oldIdentity = new FederatedIdentityModel(providerId, identity.getSub(),
                    identity.getUsername(), brokerContext.getToken());
            logger.debugf("Checking for existing users with '%s' sub matching '%s'", oldIdentity.getIdentityProvider(),
                    oldIdentity.getUserId());

            UserModel federatedUser = session.users().getUserByFederatedIdentity(oldIdentity, realm);
            if (federatedUser != null) {
                logger.debugf("Username '%s' has existing link with provider '%s'", federatedUser.getUsername(),
                        providerId);
                // TODO: Handle duplicate globus case
                // Link existing user to this token
                session.users().updateFederatedIdentity(realm, federatedUser, newIdentity);
                context.setUser(federatedUser);
                context.success();
                return;
            } else {
                logger.debugf("No match found for '%s' username '%s'", oldIdentity.getIdentityProvider(),
                        oldIdentity.getUserName());
            }
        }

        logger.debugf("No match in identity set for found for '%s' username '%s'", providerId,
                brokerContext.getUsername());
        context.attempted();

        // TODO match on username / email of existing accounts from identityset

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
