package org.chameleoncloud.api.admin;

import org.jboss.logging.Logger;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.services.resources.admin.AdminRoot;
import org.keycloak.services.resources.admin.permissions.AdminPermissionEvaluator;
import org.keycloak.services.resources.admin.permissions.AdminPermissions;

import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;

public class ChameleonAdminRoot extends AdminRoot {

    protected static final Logger logger = Logger.getLogger(ChameleonAdminRoot.class);

    public ChameleonAdminRoot(KeycloakSession session) {
        super();
        super.session = session;
    }

    @Path("user-group-roles")
    public UserGroupRolesResource userGroupRoles(@Context HttpHeaders headers) {
        RealmModel realm = session.getContext().getRealm();
        AdminPermissionEvaluator auth = AdminPermissions.evaluator(session, realm,
                authenticateRealmAdminRequest(headers));

        return new UserGroupRolesResource(session, auth, realm);
    }

}
