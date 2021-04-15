package org.chameleoncloud;

import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.authenticators.broker.util.SerializedBrokeredIdentityContext;
import org.keycloak.broker.provider.BrokeredIdentityContext;
import org.keycloak.authentication.authenticators.broker.IdpCreateUserIfUniqueAuthenticator;
import org.keycloak.authentication.authenticators.broker.util.ExistingUserInfo;
import org.keycloak.models.UserModel;
import org.jboss.logging.Logger;

public class CreateIfIdentityset extends IdpCreateUserIfUniqueAuthenticator {

    private static Logger logger = Logger.getLogger(CreateIfIdentityset.class);

    // I figured out that the userId field has to match the value in the subject
    // claim and the username field has to match the preferred_username claim of the
    // token issued from the IdP.

    @Override
    protected ExistingUserInfo checkExistingUser(AuthenticationFlowContext context, String username,
            SerializedBrokeredIdentityContext serializedCtx, BrokeredIdentityContext brokerContext) {

        // Print identity_set to debug log

        String context_log = brokerContext.getContextData().toString();
        logger.warn(context_log);

        // Default check, attempt to get user by username, return user ef exists.
        UserModel existingUser = context.getSession().users().getUserByUsername(username, context.getRealm());
        if (existingUser != null) {
            return new ExistingUserInfo(existingUser.getId(), UserModel.USERNAME, existingUser.getUsername());
        }

        return null;
    }
}
