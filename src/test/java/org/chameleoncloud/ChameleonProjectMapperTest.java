package org.chameleoncloud;

import org.junit.Test;
import org.keycloak.models.*;
import org.keycloak.protocol.ProtocolMapperUtils;
import org.keycloak.protocol.oidc.mappers.FullNameMapper;
import org.keycloak.protocol.oidc.mappers.OIDCAttributeMapperHelper;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.representations.AccessToken;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class ChameleonProjectMapperTest {

    static final String CLAIM_NAME = "testClaimName";
    static final String FLAT_CLAIM_NAME = "testFlatClaimName";

    @Test
    public void shouldTokenMapperDisplayCategory() {
        final String tokenMapperDisplayCategory = new FullNameMapper().getDisplayCategory();
        assertThat(new ChameleonProjectMapper().getDisplayCategory()).isEqualTo(tokenMapperDisplayCategory);
    }

    @Test
    public void shouldHaveDisplayType() {
        assertThat(new ChameleonProjectMapper().getDisplayType()).isNotBlank();
    }

    @Test
    public void shouldHaveHelpText() {
        assertThat(new ChameleonProjectMapper().getHelpText()).isNotBlank();
    }

    @Test
    public void shouldHaveIdId() {
        assertThat(new ChameleonProjectMapper().getId()).isNotBlank();
    }

    @Test
    public void shouldHaveProperties() {
        final List<String> configPropertyNames = new ChameleonProjectMapper().getConfigProperties().stream()
                .map(ProviderConfigProperty::getName).collect(Collectors.toList());
        assertThat(configPropertyNames).containsExactly(ProtocolMapperUtils.MULTIVALUED,
                OIDCAttributeMapperHelper.TOKEN_CLAIM_NAME, ChameleonProjectMapper.TOKEN_FLAT_CLAIM_NAME,
                OIDCAttributeMapperHelper.INCLUDE_IN_ID_TOKEN, OIDCAttributeMapperHelper.INCLUDE_IN_ACCESS_TOKEN,
                OIDCAttributeMapperHelper.INCLUDE_IN_USERINFO);
    }

    @Test
    public void shouldAddProjectClaims() {
        final Stream<GroupModel> groups = Stream.of(groupModel("project-1", null), groupModel("project-2", "nickname-2"));
        final UserSessionModel userSession = givenUserSession(groups);
        final AccessToken accessToken = transformAccessToken(givenKeycloakSession(), userSession);
        assertThat(accessToken.getOtherClaims().get(CLAIM_NAME))
                .usingRecursiveComparison().isEqualTo(List.of(new ChameleonProject("project-1", ""),
                        new ChameleonProject("project-2", "nickname-2")));
        assertThat(accessToken.getOtherClaims().get(FLAT_CLAIM_NAME))
                .isEqualTo(List.of("project-1", "project-2"));
    }

    @Test
    public void shouldFilterOutInactiveProjects() {
        final GroupModel inactiveGroup = groupModel("project-inactive", null);
        inactiveGroup.setSingleAttribute("has_active_allocation", "false");
        final UserSessionModel userSession = givenUserSession(Stream.of(groupModel("project-1", null), inactiveGroup));
        final AccessToken accessToken = transformAccessToken(givenKeycloakSession(), userSession);
        assertThat(accessToken.getOtherClaims().get(CLAIM_NAME))
                .usingRecursiveComparison().isEqualTo(List.of(new ChameleonProject("project-1", "")));
        assertThat(accessToken.getOtherClaims().get(FLAT_CLAIM_NAME))
                .isEqualTo(List.of("project-1"));
    }

    private UserSessionModel givenUserSession(Stream<GroupModel> userGroups) {
        UserModel user = mock(UserModel.class);
        when(user.getGroupsStream()).thenReturn(userGroups);
        UserSessionModel userSession = mock(UserSessionModel.class);
        when(userSession.getUser()).thenReturn(user);
        return userSession;
    }

    private GroupModel groupModel(String name, String nickname) {
        final GroupModel group = new HardcodedGroupModel(name);
        group.setName(name);
        group.setSingleAttribute("nickname", nickname);
        group.setSingleAttribute("has_active_allocation", "true");
        return group;
    }

    private KeycloakSession givenKeycloakSession() {
        return mock(KeycloakSession.class);
    }

    private AccessToken transformAccessToken(KeycloakSession keycloakSession, UserSessionModel userSessionModel) {
        final ProtocolMapperModel mappingModel = new ProtocolMapperModel();
        mappingModel.setConfig(createConfig());
        return new ChameleonProjectMapper().transformAccessToken(new AccessToken(), mappingModel, keycloakSession,
                userSessionModel, null);
    }

    private Map<String, String> createConfig() {
        final Map<String, String> result = new HashMap<>();
        result.put("access.token.claim", "true");
        result.put("claim.name", CLAIM_NAME);
        result.put("claim.flat.name", FLAT_CLAIM_NAME);
        result.put("multivalued", "true");
        return result;
    }

    // There is no easy Group representation to override in tests
    // as of the current Keycloak version ;_;
    private static class HardcodedGroupModel implements GroupModel {

        private String name;
        private final Map<String, List<String>> attributes;
        private GroupModel parent;

        public HardcodedGroupModel(String name) {
            this.name = name;
            this.attributes = new HashMap<>();
        }

        public Set<RoleModel> getRealmRoleMappings() {
            return null;
        }

        public Set<RoleModel> getClientRoleMappings(ClientModel app) {
            return null;
        }

        public boolean hasRole(RoleModel role) {
            return false;
        }

        public void grantRole(RoleModel role) {
        }

        public Set<RoleModel> getRoleMappings() {
            return null;
        }

        public void deleteRoleMapping(RoleModel role) {
        }

        public String getId() {
            return null;
        }

        public String getName() {
            return this.name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public void setSingleAttribute(String name, String value) {
            if (value == null) {
                setAttribute(name, List.of());
            } else {
                setAttribute(name, List.of(value));
            }
        }

        public void setAttribute(String name, List<String> values) {
            this.attributes.put(name, values);
        }

        public void removeAttribute(String name) {
        }

        public String getFirstAttribute(String name) {
            return null;
        }

        public List<String> getAttribute(String name) {
            return this.attributes.get(name);
        }

        public Map<String, List<String>> getAttributes() {
            return this.attributes;
        }

        public GroupModel getParent() {
            return this.parent;
        }

        public String getParentId() {
            return null;
        }

        public Set<GroupModel> getSubGroups() {
            return null;
        }

        public void setParent(GroupModel group) {
            this.parent = group;
        }

        public void addChild(GroupModel subGroup) {
        }

        public void removeChild(GroupModel subGroup) {
        }

        @Override
        public Stream<RoleModel> getRealmRoleMappingsStream() {
            throw new UnsupportedOperationException("Unimplemented method 'getRealmRoleMappingsStream'");
        }

        @Override
        public Stream<RoleModel> getClientRoleMappingsStream(ClientModel app) {
            throw new UnsupportedOperationException("Unimplemented method 'getClientRoleMappingsStream'");
        }

        @Override
        public Stream<RoleModel> getRoleMappingsStream() {
            throw new UnsupportedOperationException("Unimplemented method 'getRoleMappingsStream'");
        }

        @Override
        public Stream<String> getAttributeStream(String name) {
            throw new UnsupportedOperationException("Unimplemented method 'getAttributeStream'");
        }

        @Override
        public Stream<GroupModel> getSubGroupsStream() {
            throw new UnsupportedOperationException("Unimplemented method 'getSubGroupsStream'");
        }
    }
}
