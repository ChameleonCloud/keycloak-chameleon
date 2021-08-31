package org.chameleoncloud;

import org.chameleoncloud.api.ApiRoot;
import org.jboss.logging.Logger;
import org.keycloak.models.KeycloakSession;
import org.keycloak.services.resource.RealmResourceProvider;

public class ExtendedApiResourceProvider implements RealmResourceProvider {

  private static final Logger log = Logger.getLogger(ExtendedApiResourceProvider.class);

  private final KeycloakSession session;

  public ExtendedApiResourceProvider(KeycloakSession session) {
    this.session = session;
  }

  @Override
  public Object getResource() {
    return new ApiRoot(session);
  }

  @Override
  public void close() {

  }

}
