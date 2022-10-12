package org.chameleoncloud;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.Test;
import org.keycloak.models.FederatedIdentityModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.ProtocolMapperModel;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.UserProvider;
import org.keycloak.models.UserSessionModel;
import org.keycloak.protocol.ProtocolMapperUtils;
import org.keycloak.protocol.oidc.mappers.FullNameMapper;
import org.keycloak.protocol.oidc.mappers.OIDCAttributeMapperHelper;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.representations.AccessToken;

public class FederatedIdentitiesMapperTest {

    static final String CLAIM_NAME = "handlerIdClaimNameExample";

    @Test
    public void shouldTokenMapperDisplayCategory() {
        final String tokenMapperDisplayCategory = new FullNameMapper().getDisplayCategory();
        assertThat(new FederatedIdentitiesMapper().getDisplayCategory()).isEqualTo(tokenMapperDisplayCategory);
    }

    @Test
    public void shouldHaveDisplayType() {
        assertThat(new FederatedIdentitiesMapper().getDisplayType()).isNotBlank();
    }

    @Test
    public void shouldHaveHelpText() {
        assertThat(new FederatedIdentitiesMapper().getHelpText()).isNotBlank();
    }

    @Test
    public void shouldHaveIdId() {
        assertThat(new FederatedIdentitiesMapper().getId()).isNotBlank();
    }

    @Test
    public void shouldHaveProperties() {
        final List<String> configPropertyNames = new FederatedIdentitiesMapper().getConfigProperties().stream()
                .map(ProviderConfigProperty::getName).collect(Collectors.toList());
        assertThat(configPropertyNames).containsExactly(ProtocolMapperUtils.MULTIVALUED,
                OIDCAttributeMapperHelper.TOKEN_CLAIM_NAME, OIDCAttributeMapperHelper.INCLUDE_IN_ID_TOKEN,
                OIDCAttributeMapperHelper.INCLUDE_IN_ACCESS_TOKEN, OIDCAttributeMapperHelper.INCLUDE_IN_USERINFO);
    }

    @Test
    public void shouldAddClaimSingleProvider() {
        final FederatedIdentityModel identity = new FederatedIdentityModel("idp-1", "user id", "user name");
        final KeycloakSession keycloakSession = givenKeycloakSession(Set.of(identity));
        final UserSessionModel userSession = givenUserSession();
        final AccessToken accessToken = transformAccessToken(keycloakSession, userSession);

        assertThat(accessToken.getOtherClaims().get(CLAIM_NAME)).isEqualTo(List.of("idp-1"));
    }

    @Test
    public void shouldAddClaimMultipleProviders() {
        final FederatedIdentityModel identity = new FederatedIdentityModel("idp-1", "user id", "user name");
        final FederatedIdentityModel identity2 = new FederatedIdentityModel("idp-2", "user id", "user name");
        final KeycloakSession keycloakSession = givenKeycloakSession(Set.of(identity, identity2));
        final UserSessionModel userSession = givenUserSession();
        final AccessToken accessToken = transformAccessToken(keycloakSession, userSession);

        assertThat(accessToken.getOtherClaims().get(CLAIM_NAME)).isEqualTo(List.of("idp-1", "idp-2"));
    }

    private UserSessionModel givenUserSession() {
        UserSessionModel userSession = mock(UserSessionModel.class);
        UserModel user = mock(UserModel.class);
        when(userSession.getUser()).thenReturn(user);
        RealmModel realm = mock(RealmModel.class);
        when(userSession.getRealm()).thenReturn(realm);
        return userSession;
    }

    private KeycloakSession givenKeycloakSession(Set<FederatedIdentityModel> identityProviders) {
        KeycloakSession keycloakSession = mock(KeycloakSession.class);
        UserProvider userProvider = mock(UserProvider.class);
        when(keycloakSession.users()).thenReturn(userProvider);
        when(userProvider.getFederatedIdentitiesStream(any(RealmModel.class), any(UserModel.class)).collect(Collectors.toSet()))
                .thenReturn(identityProviders);
        return keycloakSession;
    }

    private AccessToken transformAccessToken(KeycloakSession keycloakSession, UserSessionModel userSessionModel) {
        final ProtocolMapperModel mappingModel = new ProtocolMapperModel();
        mappingModel.setConfig(createConfig());
        return new FederatedIdentitiesMapper().transformAccessToken(new AccessToken(), mappingModel, keycloakSession,
                userSessionModel, null);
    }

    private Map<String, String> createConfig() {
        final Map<String, String> result = new HashMap<>();
        result.put("access.token.claim", "true");
        result.put("claim.name", CLAIM_NAME);
        result.put("multivalued", "true");
        return result;
    }
}
