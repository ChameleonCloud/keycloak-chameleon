package org.chameleoncloud;

import org.keycloak.Config;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.services.resource.RealmResourceProvider;
import org.keycloak.services.resource.RealmResourceProviderFactory;

public class ExtendedApiResourceProviderFactory implements RealmResourceProviderFactory {

  public static final String ID = "api";

  @Override
  public RealmResourceProvider create(KeycloakSession keycloakSession) {
    return new ExtendedApiResourceProvider(keycloakSession);
  }

  @Override
  public void init(Config.Scope scope) {

  }

  @Override
  public void postInit(KeycloakSessionFactory keycloakSessionFactory) {

  }

  @Override
  public void close() {

  }

  @Override
  public String getId() {
    return ID;
  }

}
