
package org.chameleoncloud;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.type.TypeReference;

import org.jboss.logging.Logger;
import org.keycloak.broker.oidc.OIDCIdentityProviderFactory;
import org.keycloak.broker.oidc.mappers.AbstractClaimMapper;
import org.keycloak.broker.provider.BrokeredIdentityContext;
import org.keycloak.models.IdentityProviderMapperModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.util.JsonSerialization;

public class GlobusUserAttributeMapper extends AbstractClaimMapper {
    public static final String PROVIDER_ID = "globus-user-attribute-mapper";
    private static final String[] cp = new String[] { OIDCIdentityProviderFactory.PROVIDER_ID };
    private static final List<ProviderConfigProperty> configProperties = new ArrayList<>();
    private static final Logger logger = Logger.getLogger(GlobusUserAttributeMapper.class);

    public static final String BROKER_ATTRIBUTE = "broker.attribute";
    public static final String USER_ATTRIBUTE = "user.attribute";
    public static final String IDENTITY_SET = "identity_set";

    private Set<String> getLinkedSubs(String providerId, UserModel user) {
        // Get all attributes from user
        String prefix = String.join("_", providerId, "sub");
        Map<String, List<String>> attributes = user.getAttributes();
        Set<String> linkedSubs = attributes.keySet().stream().filter(k -> k.startsWith(prefix))
                .map(k -> user.getFirstAttribute(k)).collect(Collectors.toSet());

        return linkedSubs;
    }

    private void addLinkedSubs(String providerId, UserModel user, Set<String> tokenSubs) {
        for (String sub : tokenSubs) {
            // Set Key in the form <alias>_sub_<sub>: linked
            // We don't store list due to max length of DB field
            String prefix = String.join("_", providerId, "sub", sub);
            user.setSingleAttribute(prefix, "linked");
        }

    }

    @Override
    public void updateBrokeredUser(KeycloakSession session, RealmModel realm, UserModel user,
            IdentityProviderMapperModel mapperModel, BrokeredIdentityContext context) {
        /*
         * Add each "sub" in the identity set to a user attribute. Each sub will be used
         * in an authenticator later as equivelant identities.
         */

        String providerId = context.getIdpConfig().getAlias();

        // Get identity_set from any of ID token, access token, or userinfo endpoint
        Object identity_set_claim = getClaimValue(context, IDENTITY_SET);
        if (identity_set_claim != null) {
            logger.debugv("Mapping IdentitySet for user {0}", user.getUsername());
            List<Map<String, String>> tokenIdentities = JsonSerialization.mapper.convertValue(identity_set_claim,
                    new TypeReference<List<Map<String, String>>>() {
                    });
            // Add each sub to the set of identities
            Set<String> tokenSubs = tokenIdentities.stream().map(id -> id.get("sub")).collect(Collectors.toSet());
            addLinkedSubs(providerId, user, tokenSubs);
        }
    }

    @Override
    public String getDisplayCategory() {
        return "IdentitySet Importer";
    }

    @Override
    public String getDisplayType() {
        return "IdentitySet Importer";
    }

    @Override
    public String getHelpText() {
        return "Import identity set if it exists in ID, access token or the claim set returned by the user profile endpoint into the identity_set attribute";
    }

    @Override
    public String[] getCompatibleProviders() {
        return cp;
    }

    @Override
    public String getId() {
        return PROVIDER_ID;
    }

    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        return configProperties;
    }
}
