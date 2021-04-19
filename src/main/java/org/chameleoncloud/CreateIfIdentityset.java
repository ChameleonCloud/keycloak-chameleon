package org.chameleoncloud;

import org.chameleoncloud.representations.GlobusIdentity;

import org.keycloak.models.FederatedIdentityModel;

import com.fasterxml.jackson.core.JsonProcessingException;
// import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.keycloak.models.UserProvider;

import org.jboss.logging.Logger;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.authenticators.broker.IdpCreateUserIfUniqueAuthenticator;
import org.keycloak.authentication.authenticators.broker.util.ExistingUserInfo;
import org.keycloak.authentication.authenticators.broker.util.SerializedBrokeredIdentityContext;
import org.keycloak.authorization.policy.evaluation.Realm;
// import org.keycloak.broker.oidc.mappers.AbstractClaimMapper;
// import org.keycloak.broker.oidc.mappers.UserAttributeMapper;
import org.keycloak.broker.oidc.OIDCIdentityProvider;

import org.keycloak.broker.provider.BrokeredIdentityContext;
import org.keycloak.models.UserModel;
import org.keycloak.models.RealmModel;

import org.keycloak.representations.JsonWebToken;

public class CreateIfIdentityset extends IdpCreateUserIfUniqueAuthenticator {

    String IDENTITY_SET_CLAIM = "identity_set";

    private static Logger logger = Logger.getLogger(CreateIfIdentityset.class);

    // I figured out that the userId field has to match the value in the subject
    // claim and the username field has to match the preferred_username claim of the
    // token issued from the IdP.

    public void convertToJson(Object input) {

        ObjectMapper objectMapper = new ObjectMapper();

        try {
            String json = objectMapper.writeValueAsString(input);
            logger.warn(json);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    private ExistingUserInfo test_existing_user(GlobusIdentity identity, UserProvider users, RealmModel realm) {
        // Check for matching email in identity_set
        UserModel existingUserByEmail = users.getUserByEmail(identity.getEmail(), realm);
        if (existingUserByEmail != null) {
            return new ExistingUserInfo(existingUserByEmail.getId(), UserModel.EMAIL, existingUserByEmail.getEmail());
        }

        // Check for matching username in identity_set
        UserModel existingUserByUsername = users.getUserByUsername(identity.getUsername(), realm);
        if (existingUserByUsername != null) {
            return new ExistingUserInfo(existingUserByUsername.getId(), UserModel.USERNAME,
                    existingUserByUsername.getEmail());
        }
        return null;
    }

    private Boolean updateLinkedIdentity(GlobusIdentity globusID, UserModel existingUser, UserProvider users,
            RealmModel realm) {
        logger.warn("updating linked identity");
        // TODO: Check for multiple, non-matching globus IDs
        Set<FederatedIdentityModel> identities = users.getFederatedIdentities(existingUser, realm);
        convertToJson(identities);
        FederatedIdentityModel newID = new FederatedIdentityModel("globus", globusID.getSub(), globusID.getUsername());
        convertToJson(newID);
        return identities.add(newID);
    }

    @Override
    protected ExistingUserInfo checkExistingUser(AuthenticationFlowContext context, String username,
            SerializedBrokeredIdentityContext serializedCtx, BrokeredIdentityContext brokerContext) {

        logger.warn("Entered ExistingUserInfo");

        // Unpack data from broker response
        JsonWebToken token = (JsonWebToken) brokerContext.getContextData().get(OIDCIdentityProvider.VALIDATED_ID_TOKEN);
        Map<String, Object> otherClaims = token.getOtherClaims();

        // Jackson deserializes this response earlier, this forces a conversion of the
        // identity set to our type. Alternately, we could convert the token to the
        // GlobusIDToken class
        ObjectMapper mapper = new ObjectMapper();
        List<GlobusIdentity> identity_set = mapper.convertValue(otherClaims.get(this.IDENTITY_SET_CLAIM),
                new TypeReference<List<GlobusIdentity>>() {
                });

        // Check each identity in the set against the list of existing users in keycloak
        // If a match is found, return the match, otherwise, return null
        UserProvider cachedUsers = context.getSession().users();
        RealmModel realm = context.getRealm();
        for (GlobusIdentity identity : identity_set) {
            ExistingUserInfo existingUser = test_existing_user(identity, cachedUsers, realm);
            if (existingUser != null) {
                UserModel conflictingUser = cachedUsers.getUserById(existingUser.getExistingUserId(), realm);
                Boolean result = updateLinkedIdentity(identity, conflictingUser, cachedUsers, realm);
                // if (result == true) {
                // context.resetFlow();
                // } else {
                // return existingUser;
                // }
            }
        }

        // If null is return, depending on the flow, keycloak will create a new user
        return null;
    }
}
