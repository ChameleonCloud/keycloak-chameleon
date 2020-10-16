package org.chameleoncloud;

import org.keycloak.models.ClientSessionContext;
import org.keycloak.models.FederatedIdentityModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.ProtocolMapperModel;
import org.keycloak.models.UserSessionModel;
import org.keycloak.protocol.ProtocolMapperUtils;
import org.keycloak.protocol.oidc.mappers.AbstractOIDCProtocolMapper;
import org.keycloak.protocol.oidc.mappers.OIDCAccessTokenMapper;
import org.keycloak.protocol.oidc.mappers.OIDCAttributeMapperHelper;
import org.keycloak.protocol.oidc.mappers.OIDCIDTokenMapper;
import org.keycloak.protocol.oidc.mappers.UserInfoTokenMapper;
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
                .getFederatedIdentities(userSession.getUser(), userSession.getRealm());

        final List<String> identityNames = identities.stream()
                .map(identity -> identity.getIdentityProvider())
                .collect(Collectors.toList());

        identityNames.sort(Comparator.naturalOrder());

        OIDCAttributeMapperHelper.mapClaim(token, mappingModel, identityNames);
    }

}
