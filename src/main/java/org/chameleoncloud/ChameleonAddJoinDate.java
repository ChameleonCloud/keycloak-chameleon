package org.chameleoncloud;

import java.util.Arrays;
import java.util.List;

import org.keycloak.Config;
import org.keycloak.authentication.RequiredActionContext;
import org.keycloak.authentication.RequiredActionFactory;
import org.keycloak.authentication.RequiredActionProvider;
import org.keycloak.common.util.Time;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.models.UserModel;

public class ChameleonAddJoinDate implements RequiredActionProvider, RequiredActionFactory {

  public static final String PROVIDER_ID = "chameleon_add_join_date";

  public static final String USER_ATTRIBUTE = "joinDate";

  @Override
  public void close() {
    // NOOP
  }

  @Override
  public void evaluateTriggers(final RequiredActionContext context) {
    final UserModel user = context.getUser();
    final List<String> list = user.getAttribute(USER_ATTRIBUTE);

    if (list == null || list.isEmpty()) {
      user.setAttribute(USER_ATTRIBUTE, Arrays.asList(Integer.toString(Time.currentTime())));
    }
  }

  @Override
  public void requiredActionChallenge(RequiredActionContext context) {
    // NOOP
  }

  @Override
  public void processAction(final RequiredActionContext context) {
    context.success();
  }

  @Override
  public String getDisplayText() {
    return "Notify of TACC requirement";
  }

  @Override
  public String getId() {
    return PROVIDER_ID;
  }

  @Override
  public RequiredActionProvider create(KeycloakSession session) {
    return this;
  }

  @Override
  public void init(Config.Scope config) {
    // NOOP
  }

  @Override
  public void postInit(KeycloakSessionFactory factory) {
    // NOOP
  }
}
