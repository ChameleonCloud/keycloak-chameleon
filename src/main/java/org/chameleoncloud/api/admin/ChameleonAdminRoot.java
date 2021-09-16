package org.chameleoncloud.api.admin;

import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import org.jboss.logging.Logger;
import org.jboss.resteasy.spi.HttpRequest;
import org.jboss.resteasy.spi.HttpResponse;
import org.jboss.resteasy.spi.ResteasyProviderFactory;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.services.resources.admin.AdminRoot;
import org.keycloak.services.resources.admin.permissions.AdminPermissionEvaluator;
import org.keycloak.services.resources.admin.permissions.AdminPermissions;

public class ChameleonAdminRoot extends AdminRoot {

  protected static final Logger logger = Logger.getLogger(ChameleonAdminRoot.class);

  public ChameleonAdminRoot(KeycloakSession session) {
    super();
    super.session = session;
  }

  @Path("user-group-roles")
  public UserGroupRolesResource userGroupRoles(@Context final HttpRequest request,
      @Context HttpResponse response) {
    RealmModel realm = session.getContext().getRealm();
    AdminPermissionEvaluator auth = AdminPermissions.evaluator(session, realm,
        authenticateRealmAdminRequest(request.getHttpHeaders()));
    if (auth == null) {
      throw new NotAuthorizedException("Can't get AdminAuth");
    }

    UserGroupRolesResource resource = new UserGroupRolesResource(session, auth, realm);
    ResteasyProviderFactory.getInstance().injectProperties(resource);

    return resource;
  }

}
