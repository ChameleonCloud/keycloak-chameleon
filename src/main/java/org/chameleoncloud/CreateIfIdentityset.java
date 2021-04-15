package org.chameleoncloud;

import java.util.List;
import java.util.Map;

import org.jboss.logging.Logger;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.authenticators.broker.IdpCreateUserIfUniqueAuthenticator;
import org.keycloak.authentication.authenticators.broker.util.ExistingUserInfo;
import org.keycloak.authentication.authenticators.broker.util.SerializedBrokeredIdentityContext;
import org.keycloak.broker.provider.BrokeredIdentityContext;
import org.keycloak.models.UserModel;

import org.keycloak.broker.oidc.mappers.UserAttributeMapper;

public class CreateIfIdentityset extends IdpCreateUserIfUniqueAuthenticator {

    private static Logger logger = Logger.getLogger(CreateIfIdentityset.class);

    Map<String, Object> context_map;

    // I figured out that the userId field has to match the value in the subject
    // claim and the username field has to match the preferred_username claim of the
    // token issued from the IdP.

    private void findIdentitySet(BrokeredIdentityContext context) {
        context_map = context.getContextData();

        for (String key : context_map.keySet()) {
            logger.debug(key);
        }
    }

    @Override
    protected ExistingUserInfo checkExistingUser(AuthenticationFlowContext context, String username,
            SerializedBrokeredIdentityContext serializedCtx, BrokeredIdentityContext brokerContext) {

        logger.debug("Entered ExistingUserInfo");

        // Print identity_set to debug log
        findIdentitySet(brokerContext);

        String test_user_email = "shermanm@uchicago.edu";

        // Default check, attempt to get user by email, return user ef exists.
        UserModel existingUserbyEmail = context.getSession().users().getUserByEmail(test_user_email,
                context.getRealm());
        if (existingUserbyEmail != null) {
            return new ExistingUserInfo(existingUserbyEmail.getId(), UserModel.EMAIL, existingUserbyEmail.getEmail());
        }

        // Default check, attempt to get user by username, return user ef exists.
        // UserModel existingUser =
        // context.getSession().users().getUserByUsername(username, context.getRealm());
        // if (existingUser != null) {
        // return new ExistingUserInfo(existingUser.getId(), UserModel.USERNAME,
        // existingUser.getUsername());
        // }

        return null;
    }
}
