package org.chameleoncloud.api.admin;

import org.jboss.logging.Logger;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.services.resources.admin.AdminRoot;
import org.keycloak.services.resources.admin.fgap.AdminPermissionEvaluator;
import org.keycloak.services.resources.admin.fgap.AdminPermissions;

import jakarta.ws.rs.Path;

public class ChameleonAdminRoot extends AdminRoot {

    protected static final Logger logger = Logger.getLogger(ChameleonAdminRoot.class);

    public ChameleonAdminRoot(KeycloakSession session) {
        super();
        super.session = session;
    }

    @Path("user-group-roles")
    public UserGroupRolesResource userGroupRoles() {
        RealmModel realm = session.getContext().getRealm();
        AdminPermissionEvaluator auth = AdminPermissions.evaluator(session, realm,
                authenticateRealmAdminRequest(session));

        return new UserGroupRolesResource(session, auth, realm);
    }

}
