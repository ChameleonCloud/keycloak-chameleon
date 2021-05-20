
package org.chameleoncloud;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.type.TypeReference;

import org.chameleoncloud.representations.GlobusIdentity;
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
    public static final String SUB_LINKED = "linked";

    public static final String getSubAttribute(String providerId, String sub) {
        return String.join("_", providerId, "sub", sub);
    }

    public Set<String> getLinkedSubs(String providerId, UserModel user) {
        // Get all attributes from user
        String prefix = String.join("_", providerId, "sub");
        Map<String, List<String>> attributes = user.getAttributes();
        Set<String> linkedSubs = attributes.keySet().stream().filter(k -> k.startsWith(prefix))
                .map(k -> user.getFirstAttribute(k)).collect(Collectors.toSet());

        return linkedSubs;
    }

    private void storeIdentityAttribute(GlobusIdentity id, BrokeredIdentityContext context) {
        /*
         * Store a user attribute for each linked ID Each attribute will have key =
         * <provider_alias>_<sub> The value is a list of strings, in order:
         * <id_provider>,<organization>,<username>,<email>,<name> For example:
         * ["google","uchicago","shermanm@uchicago.edu@accounts.google.com",
         * "shermanm@uchicago.edu","Michael Shernam"]
         */

        // generate list from the attributes
        List<String> identityValues = new ArrayList<String>();
        identityValues.add(id.getIdentityProviderDisplayName());
        identityValues.add(id.getOrganization());
        identityValues.add(id.getUsername());
        identityValues.add(id.getEmail());
        identityValues.add(id.getName());

        // generate stable key scoped to IDP alias
        String providerId = context.getIdpConfig().getAlias();
        String identityKey = getSubAttribute(providerId, id.getSub());

        // add key to user attribute
        context.setUserAttribute(identityKey, identityValues);

    }

    private List<GlobusIdentity> getLinkedIdentities(BrokeredIdentityContext context) {
        // Get identity_set from any of ID token, access token, or userinfo endpoint
        Object identity_set_claim = getClaimValue(context, IDENTITY_SET);
        if (identity_set_claim != null) {
            List<GlobusIdentity> tokenIdentities = JsonSerialization.mapper.convertValue(identity_set_claim,
                    new TypeReference<List<GlobusIdentity>>() {
                    });
            return tokenIdentities;
        }
        return null;
    }

    @Override
    public void preprocessFederatedIdentity(KeycloakSession session, RealmModel realm,
            IdentityProviderMapperModel mapperModel, BrokeredIdentityContext context) {

        List<GlobusIdentity> tokenIdentities = getLinkedIdentities(context);
        tokenIdentities.stream().forEach(id -> storeIdentityAttribute(id, context));

    }

    @Override
    public void updateBrokeredUser(KeycloakSession session, RealmModel realm, UserModel user,
            IdentityProviderMapperModel mapperModel, BrokeredIdentityContext context) {

        List<GlobusIdentity> tokenIdentities = getLinkedIdentities(context);
        tokenIdentities.stream().forEach(id -> storeIdentityAttribute(id, context));
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
