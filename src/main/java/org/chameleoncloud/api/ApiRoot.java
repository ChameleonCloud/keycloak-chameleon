package org.chameleoncloud.api;

import javax.ws.rs.Path;
import org.chameleoncloud.api.admin.ChameleonAdminRoot;
import org.keycloak.models.KeycloakSession;

public class ApiRoot {

  private KeycloakSession session;

  public ApiRoot(KeycloakSession session) {
    this.session = session;
  }

  /**
   * @return The admin API
   */
  @Path("admin")
  public Object getAdminApiRoot() {
    return new ChameleonAdminRoot(session);
  }

}
