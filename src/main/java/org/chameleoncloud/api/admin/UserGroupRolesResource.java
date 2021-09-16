package org.chameleoncloud.api.admin;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.stream.Collectors;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.chameleoncloud.representations.UserGroupRoles;
import org.jboss.resteasy.annotations.cache.NoCache;
import org.json.JSONArray;
import org.json.JSONObject;
import org.keycloak.authorization.model.Policy;
import org.keycloak.authorization.model.ResourceServer;
import org.keycloak.authorization.model.Scope;
import org.keycloak.authorization.store.PolicyStore;
import org.keycloak.models.GroupModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.representations.idm.authorization.GroupPolicyRepresentation;
import org.keycloak.services.resources.admin.permissions.AdminPermissionEvaluator;
import org.keycloak.services.resources.admin.permissions.AdminPermissionManagement;
import org.keycloak.services.resources.admin.permissions.AdminPermissions;
import org.keycloak.services.resources.admin.permissions.GroupPermissionManagement;

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

    Set<String> result = new HashSet<String>();
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

    SortedMap<Integer, String> result = new TreeMap<Integer, String>();
    Map<String, Set<String>> policyGroups = new HashMap<String, Set<String>>();
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
      Policy associatedPolicy = policyStore.findByName(associatedPolicyName, rs.getId());
      if (associatedPolicy == null) {
        // create a policy
        GroupPolicyRepresentation rep = new GroupPolicyRepresentation();
        rep.setName(associatedPolicyName);
        rep.addGroup(subGroup.getId(), true);
        associatedPolicy = policyStore.create(rep, groupScopePolicy.getResourceServer());
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
  @NoCache
  @Produces(MediaType.APPLICATION_JSON)
  public Response getUserGroupRoles(@QueryParam("group") String groupId,
      @PathParam("user") String userId) {

    UserModel user = session.users().getUserById(userId, realm);
    if (user == null) {
      return Response.status(Response.Status.NOT_FOUND).entity("user not found!").build();
    }

    Set<GroupModel> userGroups = user.getGroups();

    if (groupId != null) {
      GroupModel group = session.realms().getGroupById(groupId, realm);
      Set<String> filterGroups = new HashSet<String>();
      filterGroups.add(groupId);
      for (GroupModel g : group.getSubGroups()) {
        filterGroups.add(g.getId());
      }
      userGroups = userGroups.stream().filter(g -> filterGroups.contains(g.getId()))
          .collect(Collectors.toSet());
    }

    Set<String> userGroupIds = new HashSet<String>();
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

    Set<UserGroupRoles> result = new HashSet<UserGroupRoles>();
    for (GroupModel group : userGroups) {

      if (setFineGrainedPermissions(group)) {

        UserGroupRoles ugr = new UserGroupRoles();
        ugr.setGroupId(group.getId());
        ugr.setGroupName(group.getName());
        SortedMap<Integer, String> policies = new TreeMap<Integer, String>();
        Set<String> scopes = new HashSet<>();

        for (String permissionName : groupPermission.getPermissions(group).keySet()) {
          Policy permission = getPolicyByPermissionName(permissionName, group);
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

    return Response.status(Response.Status.OK).entity(result).build();
  }

  @PUT
  @Path("user/{user}")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public Response setUserGroupRole(UserGroupRoles userGroupRoles,
      @PathParam("user") String userId) {
    UserModel user = session.users().getUserById(userId, realm);
    if (user == null) {
      return Response.status(Response.Status.NOT_FOUND).entity("user not found!").build();
    }

    String requestGroupId = userGroupRoles.getGroupId();
    String requestGroupName = userGroupRoles.getGroupName();
    String requestPolicy = userGroupRoles.getPolicy();
    if (requestPolicy == null) {
      return Response.status(Response.Status.NOT_FOUND).entity("policy not found!").build();
    }

    GroupModel group = null;
    if (requestGroupId != null) {
      group = session.realms().getGroupById(requestGroupId, realm);
    } else {
      group = session.realms().getGroups(realm).stream()
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

    GroupModel adminSubGroup = group.getSubGroups().stream()
        .filter(g -> g.getName().endsWith(UserGroupRoles.ADMIN_GROUP_SUFFIX)).findFirst()
        .orElse(null);
    GroupModel managerSubGroup = group.getSubGroups().stream()
        .filter(g -> g.getName().endsWith(UserGroupRoles.MANAGER_GROUP_SUFFIX)).findFirst()
        .orElse(null);
    if (requestPolicy.equals(UserGroupRoles.ADMIN)) {
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
    } else if (requestPolicy.equals(UserGroupRoles.MANAGER)) {
      if (managerSubGroup == null) {
        // create manager sub group
        managerSubGroup = realm.createGroup(group.getName() + UserGroupRoles.MANAGER_GROUP_SUFFIX);
        group.addChild(managerSubGroup);
      }
      user.joinGroup(managerSubGroup);
      user.leaveGroup(adminSubGroup);
      grantManagerPermissions(group, managerSubGroup);
    } else if (requestPolicy.equals(UserGroupRoles.MEMBER)) {
      if (managerSubGroup != null) {
        user.leaveGroup(managerSubGroup);
      }
      user.leaveGroup(adminSubGroup);
    }

    return Response.status(Response.Status.OK).entity("").build();

  }

  @GET
  @Path("group/{group}")
  @NoCache
  @Produces(MediaType.APPLICATION_JSON)
  public Response getGroupMemberRoles(@PathParam("group") String groupId) {

    GroupModel group = session.realms().getGroupById(groupId, realm);
    if (group == null) {
      return Response.status(Response.Status.NOT_FOUND).entity("group not found!").build();
    }

    Map<String, String> result = new HashMap<>();

    session.users().getGroupMembers(realm, group).stream()
        .forEach(member -> result.put(member.getUsername(), UserGroupRoles.MEMBER));

    String resourceServerId = adminPermissionManagement.clients()
        .resourceServer(adminPermissionManagement.getRealmManagementClient()).getId();

    Policy adminPolicy = policyStore.findByName(
        UserGroupRoles.formatPolicyName(UserGroupRoles.ADMIN, groupId), resourceServerId);
    Set<String> adminSubGroupIds = new HashSet<String>();
    if (adminPolicy != null) {
      adminSubGroupIds = getGroupIdsFromPolicyConfig(adminPolicy.getConfig());
    }

    Policy managerPolicy = policyStore.findByName(
        UserGroupRoles.formatPolicyName(UserGroupRoles.MANAGER, groupId), resourceServerId);
    Set<String> managerSubGroupIds = new HashSet<String>();
    if (managerPolicy != null) {
      managerSubGroupIds = getGroupIdsFromPolicyConfig(managerPolicy.getConfig());
    }

    for (GroupModel subGroup : group.getSubGroups()) {
      String role = null;
      String subGroupId = subGroup.getId();
      if (adminSubGroupIds.contains(subGroupId)) {
        role = UserGroupRoles.ADMIN;
      } else if (managerSubGroupIds.contains(subGroupId)) {
        role = UserGroupRoles.MANAGER;
      }

      if (role != null) {
        for (UserModel user : session.users().getGroupMembers(realm, subGroup)) {
          String username = user.getUsername();
          if (result.containsKey(username)) {
            result.put(username, role);
          }
        }
      }
    }

    return Response.status(Response.Status.OK).entity(result).build();
  }

}
