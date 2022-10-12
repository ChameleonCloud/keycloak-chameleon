package org.chameleoncloud;

import org.keycloak.models.*;
import org.keycloak.protocol.ProtocolMapperUtils;
import org.keycloak.protocol.oidc.mappers.*;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.representations.IDToken;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/*
 * Our own example protocol mapper.
 */
public class FederatedIdentitiesMapper extends AbstractOIDCProtocolMapper
        implements OIDCAccessTokenMapper, OIDCIDTokenMapper, UserInfoTokenMapper {

    /*
     * A config which keycloak uses to display a generic dialog to configure the
     * token.
     */
    private static final List<ProviderConfigProperty> CONFIG_PROPERTIES = new ArrayList<>();

    /*
     * The ID of the token mapper. Is public, because we need this id in our
     * data-setup project to configure the protocol mapper in keycloak.
     */
    public static final String PROVIDER_ID = "oidc-federated-identities-mapper";

    static {
        // Support multi-value claims (it's really the only sane option but
        // it is configurable nonetheless)
        ProviderConfigProperty multiValued = new ProviderConfigProperty();
        multiValued.setName(ProtocolMapperUtils.MULTIVALUED);
        multiValued.setLabel(ProtocolMapperUtils.MULTIVALUED_LABEL);
        multiValued.setHelpText(ProtocolMapperUtils.MULTIVALUED_HELP_TEXT);
        multiValued.setType(ProviderConfigProperty.BOOLEAN_TYPE);
        multiValued.setDefaultValue("true");
        CONFIG_PROPERTIES.add(multiValued);

        // Allow user to override claim name
        OIDCAttributeMapperHelper.addTokenClaimNameConfig(CONFIG_PROPERTIES);
        // Allow user to include in id_token/access_token/user_info
        OIDCAttributeMapperHelper.addIncludeInTokensConfig(CONFIG_PROPERTIES, FederatedIdentitiesMapper.class);
    }

    @Override
    public String getDisplayCategory() {
        return "Token mapper";
    }

    @Override
    public String getDisplayType() {
        return "Federated identities mapper";
    }

    @Override
    public String getHelpText() {
        return "Adds a list of linked federated identity providers to the claim";
    }

    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        return CONFIG_PROPERTIES;
    }

    @Override
    public String getId() {
        return PROVIDER_ID;
    }

    @Override
    protected void setClaim(final IDToken token, final ProtocolMapperModel mappingModel,
                            final UserSessionModel userSession, final KeycloakSession keycloakSession,
                            final ClientSessionContext clientSessionCtx) {
        final Set<FederatedIdentityModel> identities = keycloakSession.users()
                .getFederatedIdentitiesStream(userSession.getRealm(), userSession.getUser()).collect(
                        Collectors.toSet());

        final List<String> identityNames = identities.stream()
                .map(FederatedIdentityModel::getIdentityProvider).sorted(Comparator.naturalOrder())
                .collect(Collectors.toList());

        OIDCAttributeMapperHelper.mapClaim(token, mappingModel, identityNames);
    }

}
