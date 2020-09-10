package org.chameleoncloud;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;

import org.keycloak.models.ClientSessionContext;
import org.keycloak.models.GroupModel;
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
import org.keycloak.util.JsonSerialization;

/*
 * Our own example protocol mapper.
 */
public class ChameleonProjectMapper extends AbstractOIDCProtocolMapper
        implements OIDCAccessTokenMapper, OIDCIDTokenMapper, UserInfoTokenMapper {

    static {
        JsonSerialization.mapper.registerModule(new Jdk8Module());
    }

    /*
     * A config which keycloak uses to display a generic dialog to configure the
     * token.
     */
    private static final List<ProviderConfigProperty> CONFIG_PROPERTIES = new ArrayList<>();

    /*
     * The ID of the token mapper. Is public, because we need this id in our
     * data-setup project to configure the protocol mapper in keycloak.
     */
    public static final String PROVIDER_ID = "oidc-chameleon-project-mapper";

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
        return "Chameleon project mapper";
    }

    @Override
    public String getHelpText() {
        return "Adds a list of Chameleon project entities the user is a member of, with name and other properties, to the claim.";
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
        Function<GroupModel, ChameleonProject> toProjectRepresentation = this::toProjectRepresentation;
        List<ChameleonProject> projects = userSession.getUser().getGroups().stream()
            .map(toProjectRepresentation).collect(Collectors.toList());

        String protocolClaim = mappingModel.getConfig().get(OIDCAttributeMapperHelper.TOKEN_CLAIM_NAME);

        token.getOtherClaims().put(protocolClaim, projects);
    }

    protected ChameleonProject toProjectRepresentation(final GroupModel group) {
        final Optional<String> nickname = group.getAttributes()
            .getOrDefault("nickname", Collections.emptyList())
            .stream().findFirst();
        return new ChameleonProject(group.getName(), nickname);
    }

}
