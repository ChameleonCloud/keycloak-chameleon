package org.chameleoncloud.api.admin;

import org.chameleoncloud.representations.UserGroupRoles;
import org.json.JSONArray;
import org.json.JSONObject;
import org.keycloak.authorization.model.Policy;
import org.keycloak.authorization.model.ResourceServer;
import org.keycloak.authorization.model.Scope;
import org.keycloak.authorization.store.PolicyStore;
import org.keycloak.models.ClientModel;
import org.keycloak.models.Constants;
import org.keycloak.models.GroupModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.representations.idm.authorization.GroupPolicyRepresentation;
import org.keycloak.services.resources.admin.permissions.AdminPermissionEvaluator;
import org.keycloak.services.resources.admin.permissions.AdminPermissionManagement;
import org.keycloak.services.resources.admin.permissions.AdminPermissions;
import org.keycloak.services.resources.admin.permissions.GroupPermissionManagement;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.CacheControl;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class UserGroupRolesResource {

    private AdminPermissionEvaluator auth;
    private KeycloakSession session;
    private RealmModel realm;
    private AdminPermissionManagement adminPermissionManagement;
    private GroupPermissionManagement groupPermission;
    private PolicyStore policyStore;

    private final String GROUP_POLICY_TYPE = "group";

    public UserGroupRolesResource(KeycloakSession session, AdminPermissionEvaluator auth,
                                  RealmModel realm) {
        this.auth = auth;
        this.session = session;
        this.realm = realm;
        this.adminPermissionManagement = AdminPermissions.management(session, realm);
        this.groupPermission = adminPermissionManagement.groups();
        this.policyStore = adminPermissionManagement.authz().getStoreFactory().getPolicyStore();
    }

    private Set<String> getGroupIdsFromPolicyConfig(Map<String, String> policyConfig) {

        // policy config returns a type of Map<String, String>
        // e.g.
        // {"groups":"[{\"id\":\"b770dfde-038b-493e-bc7f-a51a5ce86741\",\"extendChildren\":true}]"}
        // this function parses the value of "groups" and extract the group ids into a
        // hashset

        Set<String> result = new HashSet<>();
        if (policyConfig.containsKey("groups")) {
            JSONArray jsonArr = new JSONArray(policyConfig.get("groups"));

            for (int i = 0; i < jsonArr.length(); i++) {
                JSONObject jsonObj = jsonArr.getJSONObject(i);
                String groupId = jsonObj.getString("id");
                result.add(groupId);
            }
        }
        return result;

    }

    private SortedMap<Integer, String> getPolicies(Policy permission, Set<String> groupIds) {

        // get the names of the group type policies from a permission
        // and check if a user applies to the policies by comparing the group ids

        SortedMap<Integer, String> result = new TreeMap<>();
        Map<String, Set<String>> policyGroups = new HashMap<>();
        permission.getAssociatedPolicies().stream()
                .filter(policy -> policy.getType().equals(GROUP_POLICY_TYPE)).forEach(policy -> policyGroups
                        .put(policy.getName(), getGroupIdsFromPolicyConfig(policy.getConfig())));

        for (String policy : policyGroups.keySet()) {
            Set<String> policyGroupIds = policyGroups.get(policy);
            // get overlap groups between policy groups and user groups
            policyGroupIds.retainAll(groupIds);
            if (!policyGroupIds.isEmpty()) {
                // the user applies to the policy
                String policyName = UserGroupRoles.parsePolicyName(policy)[0];
                result.put(UserGroupRoles.POLICY_HIERARCHY.get(policyName), policyName);
            }
        }

        return result;
    }

    private Set<String> getPermissionScopes(Policy permission) {

        // get the scopes of a permission

        return permission.getScopes().stream().map(Scope::getName).collect(Collectors.toSet());
    }

    private Policy getPolicyByPermissionName(String permissionName, GroupModel group) {

        switch (permissionName) {
            case "view":
                return groupPermission.viewPermission(group);
            case "manage":
                return groupPermission.managePermission(group);
            case "view-members":
                return groupPermission.viewMembersPermission(group);
            case "manage-members":
                return groupPermission.manageMembersPermission(group);
            case "manage-membership":
                return groupPermission.manageMembershipPermission(group);
            default:
                return null;
        }
    }

    private Boolean setFineGrainedPermissions(GroupModel group) {

        // check if the group is a root group
        // if a root group, check if fine grained permission is enabled
        // enable fine grained admin permission if not

        if (group.getParent() != null) {
            groupPermission.setPermissionsEnabled(group, false);
            return false;
        }
        if (!groupPermission.isPermissionsEnabled(group)) {
            groupPermission.setPermissionsEnabled(group, true);
        }
        return true;
    }

    private void grantPermission(String permissionName, GroupModel group, GroupModel subGroup,
                                 String policyName) throws RuntimeException {

        // associate the group policy to the scope permissions
        // if policy does not exist, create one lazily

        Policy groupScopePolicy = getPolicyByPermissionName(permissionName, group);
        if (groupScopePolicy == null) {
            throw new RuntimeException(
                    String.format("Can not get policy from fine grained permission %s", permissionName));
        }
        String groupId = group.getId();
        Set<String> associatedPolicies = groupScopePolicy.getAssociatedPolicies().stream()
                .filter(p -> p.getType().equals(GROUP_POLICY_TYPE)).map(Policy::getName)
                .collect(Collectors.toSet());
        String associatedPolicyName = UserGroupRoles.formatPolicyName(policyName, groupId);
        if (!associatedPolicies.contains(associatedPolicyName)) {
            // policy not associated
            // check if the associated policy exists
            ResourceServer rs = groupScopePolicy.getResourceServer();
            Policy associatedPolicy = policyStore.findByName(rs, associatedPolicyName);
            if (associatedPolicy == null) {
                // create a policy
                GroupPolicyRepresentation rep = new GroupPolicyRepresentation();
                rep.setName(associatedPolicyName);
                rep.addGroup(subGroup.getId(), true);
                associatedPolicy = policyStore.create(groupScopePolicy.getResourceServer(), rep);
            }
            groupScopePolicy.addAssociatedPolicy(associatedPolicy);
        }
    }

    private void grantMemberPermissions(GroupModel group) {
        grantPermission("view", group, group, UserGroupRoles.MEMBER);
        grantPermission("view-members", group, group, UserGroupRoles.MEMBER);
    }

    private void grantManagerPermissions(GroupModel group, GroupModel managerSubGroup) {
        grantPermission("manage-membership", group, managerSubGroup, UserGroupRoles.MANAGER);
    }

    private void grantAdminPermissions(GroupModel group, GroupModel adminSubGroup) {
        grantPermission("manage", group, adminSubGroup, UserGroupRoles.ADMIN);
        grantPermission("manage-members", group, adminSubGroup, UserGroupRoles.ADMIN);
        grantPermission("manage-membership", group, adminSubGroup, UserGroupRoles.ADMIN);
    }

    @GET
    @Path("user/{user}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUserGroupRoles(@QueryParam("group") String groupId,
                                      @PathParam("user") String userId) {

        UserModel user = session.users().getUserById(realm, userId);
        if (user == null) {
            return Response.status(Response.Status.NOT_FOUND).entity("user not found!").build();
        }

        Set<GroupModel> userGroups = user.getGroupsStream().collect(Collectors.toSet());

        if (groupId != null) {
            GroupModel group = session.groups().getGroupsStream(realm, Stream.of(groupId)).findFirst().orElseThrow();
            Set<String> filterGroups = new HashSet<>();
            filterGroups.add(groupId);
            for (GroupModel g : group.getSubGroupsStream().collect(Collectors.toSet())) {
                filterGroups.add(g.getId());
            }
            userGroups = userGroups.stream().filter(g -> filterGroups.contains(g.getId()))
                    .collect(Collectors.toSet());
        }

        Set<String> userGroupIds = new HashSet<>();
        // lazily grant permissions to user groups
        for (GroupModel userGroup : userGroups) {
            String userGroupName = userGroup.getName();
            if (setFineGrainedPermissions(userGroup)) {
                grantMemberPermissions(userGroup);
            } else {
                if (userGroupName.endsWith(UserGroupRoles.ADMIN_GROUP_SUFFIX)) {
                    grantAdminPermissions(userGroup.getParent(), userGroup);
                } else if (userGroupName.endsWith(UserGroupRoles.MANAGER_GROUP_SUFFIX)) {
                    grantManagerPermissions(userGroup.getParent(), userGroup);
                }
            }
            userGroupIds.add(userGroup.getId());
        }

        Set<UserGroupRoles> result = new HashSet<>();
        for (GroupModel group : userGroups) {

            if (setFineGrainedPermissions(group)) {

                UserGroupRoles ugr = new UserGroupRoles();
                ugr.setGroupId(group.getId());
                ugr.setGroupName(group.getName());
                SortedMap<Integer, String> policies = new TreeMap<>();
                Set<String> scopes = new HashSet<>();

                for (String permissionName : groupPermission.getPermissions(group).keySet()) {
                    Policy permission = getPolicyByPermissionName(permissionName, group);
                    if (permission == null) {
                        throw new RuntimeException(
                                String.format("Can not get policy from fine grained permission %s", permissionName));

                    }
                    SortedMap<Integer, String> groupPolicies = getPolicies(permission, userGroupIds);
                    if (!groupPolicies.isEmpty()) {
                        policies.putAll(groupPolicies);
                        scopes.addAll(getPermissionScopes(permission));
                    }
                }

                ugr.setPolicy(policies.get(policies.firstKey()));
                ugr.setScopes(scopes);
                result.add(ugr);
            }
        }

        CacheControl cc = new CacheControl();
        cc.setNoCache(true);
        return Response.status(Response.Status.OK).entity(result).cacheControl(cc).build();
    }

    @PUT
    @Path("user/{user}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response setUserGroupRole(UserGroupRoles userGroupRoles,
                                     @PathParam("user") String userId) {
        UserModel user = session.users().getUserById(realm, userId);
        if (user == null) {
            return Response.status(Response.Status.NOT_FOUND).entity("user not found!").build();
        }

        String requestGroupId = userGroupRoles.getGroupId();
        String requestGroupName = userGroupRoles.getGroupName();
        String requestPolicy = userGroupRoles.getPolicy();
        if (requestPolicy == null) {
            return Response.status(Response.Status.NOT_FOUND).entity("policy not found!").build();
        }

        GroupModel group;
        if (requestGroupId != null) {
            group = session.groups().getGroupsStream(realm, Stream.of(requestGroupId)).findFirst().orElseThrow();
        } else {
            group = session.groups().getGroupsStream(realm)
                    .filter(g -> requestGroupName.equals(g.getName())).findFirst().orElse(null);
        }
        if (group == null) {
            return Response.status(Response.Status.NOT_FOUND).entity("group not found!").build();
        }

        if (!setFineGrainedPermissions(group)) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Not a root group!")
                    .build();
        }

        user.joinGroup(group);
        grantMemberPermissions(group);

        GroupModel adminSubGroup = group.getSubGroupsStream()
                .filter(g -> g.getName().endsWith(UserGroupRoles.ADMIN_GROUP_SUFFIX)).findFirst()
                .orElse(null);
        GroupModel managerSubGroup = group.getSubGroupsStream()
                .filter(g -> g.getName().endsWith(UserGroupRoles.MANAGER_GROUP_SUFFIX)).findFirst()
                .orElse(null);
        switch (requestPolicy) {
            case UserGroupRoles.ADMIN:
                if (adminSubGroup == null) {
                    // create admin sub group
                    adminSubGroup = realm.createGroup(group.getName() + UserGroupRoles.ADMIN_GROUP_SUFFIX);
                    group.addChild(adminSubGroup);
                }
                user.joinGroup(adminSubGroup);
                grantAdminPermissions(group, adminSubGroup);
                if (managerSubGroup != null) {
                    user.leaveGroup(managerSubGroup);
                }
                break;
            case UserGroupRoles.MANAGER:
                if (managerSubGroup == null) {
                    // create manager sub group
                    managerSubGroup = realm.createGroup(group.getName() + UserGroupRoles.MANAGER_GROUP_SUFFIX);
                    group.addChild(managerSubGroup);
                }
                user.joinGroup(managerSubGroup);
                user.leaveGroup(adminSubGroup);
                grantManagerPermissions(group, managerSubGroup);
                break;
            case UserGroupRoles.MEMBER:
                if (managerSubGroup != null) {
                    user.leaveGroup(managerSubGroup);
                }
                user.leaveGroup(adminSubGroup);
                break;
        }

        return Response.status(Response.Status.OK).entity("").build();

    }

    @GET
    @Path("group/{group}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getGroupMemberRoles(@PathParam("group") String groupId) {

        Optional<GroupModel> groupSearch = session.groups().getGroupsStream(realm, Stream.of(groupId)).findFirst();
        if (groupSearch.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND).entity("group not found!").build();
        }
        GroupModel group = groupSearch.get();

        Map<String, String> result = new HashMap<>();

        session.users().getGroupMembersStream(realm, group)
                .forEach(member -> result.put(member.getUsername(), UserGroupRoles.MEMBER));

        ClientModel client = session.clients().getClientById(realm, Constants.REALM_MANAGEMENT_CLIENT_ID);
        ResourceServer resourceServer = adminPermissionManagement.clients().resourceServer(client);

        Policy adminPolicy = policyStore.findByName(resourceServer,
                UserGroupRoles.formatPolicyName(UserGroupRoles.ADMIN, groupId));
        Set<String> adminSubGroupIds = new HashSet<>();
        if (adminPolicy != null) {
            adminSubGroupIds = getGroupIdsFromPolicyConfig(adminPolicy.getConfig());
        }

        Policy managerPolicy = policyStore.findByName(resourceServer,
                UserGroupRoles.formatPolicyName(UserGroupRoles.MANAGER, groupId));
        Set<String> managerSubGroupIds = new HashSet<>();
        if (managerPolicy != null) {
            managerSubGroupIds = getGroupIdsFromPolicyConfig(managerPolicy.getConfig());
        }

        for (GroupModel subGroup : group.getSubGroupsStream().collect(Collectors.toSet())) {
            String role = null;
            String subGroupId = subGroup.getId();
            if (adminSubGroupIds.contains(subGroupId)) {
                role = UserGroupRoles.ADMIN;
            } else if (managerSubGroupIds.contains(subGroupId)) {
                role = UserGroupRoles.MANAGER;
            }

            if (role != null) {
                for (UserModel user : session.users().getGroupMembersStream(realm, subGroup).collect(Collectors.toSet())) {
                    String username = user.getUsername();
                    if (result.containsKey(username)) {
                        result.put(username, role);
                    }
                }
            }
        }

        CacheControl cc = new CacheControl();
        cc.setNoCache(true);
        return Response.status(Response.Status.OK).entity(result).cacheControl(cc).build();
    }

}
