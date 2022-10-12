package org.chameleoncloud;

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

import java.util.List;
import java.util.stream.Collectors;

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
        List<UserModel> existingUsers = session.users().searchForUserByUserAttributeStream(realm,
                attrName, GlobusUserAttributeMapper.SUB_LINKED).collect(Collectors.toList());
        if (existingUsers.size() > 1) {
            // Fail if more than one candidate user is found
            logger.errorv("More than one user matches identity {0}! List: {1}", tokenIdentity.getUserName(),
                    existingUsers.stream().map(UserModel::getId).collect(Collectors.joining(",")));
            context.failure(AuthenticationFlowError.USER_CONFLICT);
        } else if (existingUsers.size() == 1) {
            // Remove old identity and Link existing user to this token in one action.
            // If not done together, the account can be "orphaned"
            UserModel federatedUser = existingUsers.get(0);
            if (federatedUser != null) {
                session.users().removeFederatedIdentity(realm, federatedUser, providerId);
                context.setUser(federatedUser);
                context.success();
            } else {
                // Something is wrong with the attribute search
                logger.errorv("Search by attribute {0} has returned a null entry, escalate", attrName);
                context.failure(AuthenticationFlowError.INVALID_USER);
            }
        } else {
            // try next in flow if no matches found, new user flow.
            logger.debugv("No match in identity set for found for {0} username {1}", providerId,
                    brokerContext.getUsername());
            context.attempted();
        }
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
