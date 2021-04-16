package org.chameleoncloud;

import org.chameleoncloud.representations.GlobusIDToken;
import org.chameleoncloud.representations.GlobusIdentity;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

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

    // I figured out that the userId field has to match the value in the subject
    // claim and the username field has to match the preferred_username claim of the
    // token issued from the IdP.

    public void convertMapToJson(Map<String, Object> input) {

        ObjectMapper objectMapper = new ObjectMapper();

        try {
            String json = objectMapper.writeValueAsString(input);
            logger.warn(json);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected ExistingUserInfo checkExistingUser(AuthenticationFlowContext context, String username,
            SerializedBrokeredIdentityContext serializedCtx, BrokeredIdentityContext brokerContext) {

        logger.warn("Entered ExistingUserInfo");

        // Unpack data from broker response
        GlobusIDToken validatedIDToken = (GlobusIDToken) brokerContext.getContextData().get("VALIDATED_ID_TOKEN");

        String broker_user_person_name = validatedIDToken.getName(); // Person's name
        String broker_username = validatedIDToken.getPreferredUsername(); // Preferred username is "primary"
        String broker_user_email = validatedIDToken.getEmail(); // May or may not be the same as username

        // Print identity_set to debug log
        convertMapToJson(brokerContext.getContextData());

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
