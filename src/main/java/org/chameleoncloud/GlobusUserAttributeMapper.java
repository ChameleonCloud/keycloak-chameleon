
package org.chameleoncloud;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.jboss.logging.Logger;
import org.keycloak.broker.oidc.OIDCIdentityProviderFactory;
import org.keycloak.broker.oidc.mappers.AbstractClaimMapper;
import org.keycloak.broker.provider.BrokeredIdentityContext;
import org.keycloak.models.IdentityProviderMapperModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.provider.ProviderConfigProperty;

public class GlobusUserAttributeMapper extends AbstractClaimMapper {

    public static final String BROKER_ATTRIBUTE = "broker.attribute";
    public static final String USER_ATTRIBUTE = "user.attribute";
    public static final String IDENTITY_SET = "identity_set";
    public static final String PROVIDER_ID = "globus-user-attribute-mapper";

    private static final String[] cp = new String[] { OIDCIdentityProviderFactory.PROVIDER_ID };
    private static final List<ProviderConfigProperty> configProperties = new ArrayList<>();
    private static final Logger logger = Logger.getLogger(GlobusUserAttributeMapper.class);

    @Override
    public String[] getCompatibleProviders() {
        return cp;
    }

    @Override
    public String getId() {
        return PROVIDER_ID;
    }

    private static final Map<String, String> attribute_map;
    static {
        attribute_map = new HashMap<>();
        attribute_map.put("sub", "id");
        attribute_map.put("username", "username");
        attribute_map.put("email", "email");
        attribute_map.put("name", "name");
        attribute_map.put("organization", "organization");
        attribute_map.put("last_authentication", "last_authentication");
        attribute_map.put("identity_provider", "provider_id");
        attribute_map.put("identity_provider_display_name", "provider_name");
    }

    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        return configProperties;
    }

    @Override
    public void updateBrokeredUser(KeycloakSession session, RealmModel realm, UserModel user,
            IdentityProviderMapperModel mapperModel, BrokeredIdentityContext context) {

        String providerId = context.getIdpConfig().getAlias();

        Object identity_set_claim = getClaimValue(context, IDENTITY_SET);
        if (identity_set_claim != null) {
            logger.warnv("Mapping IdentitySet for user {0}", user.getUsername());

            ObjectMapper mapper = new ObjectMapper();
            List<Map<String, String>> identity_set = mapper.convertValue(identity_set_claim,
                    new TypeReference<List<Map<String, String>>>() {
                    });

            // For each key1,key2 pair in the config, map broker(key1) to keycloak(key2)
            for (Map.Entry<String, String> entry : attribute_map.entrySet()) {
                String user_attribute = String.join("_", providerId, entry.getValue());
                List<String> values = identity_set.stream().map(identity -> identity.get(entry.getKey()))
                        .collect(Collectors.toList());

                if (values.isEmpty()) {
                    // remove entry if not in identity_set
                    user.removeAttribute(user_attribute);
                } else {
                    user.setAttribute(user_attribute, values);
                }
            }
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
}
