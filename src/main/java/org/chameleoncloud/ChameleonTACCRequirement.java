package org.chameleoncloud;

import java.util.Arrays;
import java.util.Set;

import javax.ws.rs.core.Response;

import org.keycloak.Config;
import org.keycloak.authentication.RequiredActionContext;
import org.keycloak.authentication.RequiredActionFactory;
import org.keycloak.authentication.RequiredActionProvider;
import org.keycloak.common.util.Time;
import org.keycloak.events.Details;
import org.keycloak.models.FederatedIdentityModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;

public class ChameleonTACCRequirement implements RequiredActionProvider, RequiredActionFactory {

  public static final String PROVIDER_ID = "chameleon_notify_tacc_requirement";
  public static final String USER_ATTRIBUTE = PROVIDER_ID;

  public static final String NOTIFY_REQUIREMENT_FORM = "login-tacc-requirement.ftl";

  private static final String TACC_IDP = "tacc";

  @Override
  public RequiredActionProvider create(KeycloakSession session) {
    return this;
  }

  @Override
  public void init(Config.Scope config) {

  }

  @Override
  public void postInit(KeycloakSessionFactory factory) {

  }

  @Override
  public String getId() {
    return PROVIDER_ID;
  }

  @Override
  public void evaluateTriggers(RequiredActionContext context) {

  }

  @Override
  public void requiredActionChallenge(RequiredActionContext context) {
    final UserModel user = context.getUser();
    final RealmModel realm = context.getRealm();
    final String identityProvider = context.getAuthenticationSession()
        .getUserSessionNotes().get(Details.IDENTITY_PROVIDER);
    final KeycloakSession session = context.getSession();
    final Set<FederatedIdentityModel> identities = session.users().getFederatedIdentities(user, realm);
    final Boolean isLinkedToTacc = identities.stream()
        .anyMatch(fim -> (fim.getIdentityProvider().equals(TACC_IDP)));

    if (identityProvider.equals(TACC_IDP) || isLinkedToTacc) {
      // Skip the form for users logging in via TACC
      context.ignore();
    } else {
      final Response challenge = context.form().createForm(NOTIFY_REQUIREMENT_FORM);
      context.challenge(challenge);
    }
  }

  @Override
  public void processAction(RequiredActionContext context) {
    this.completeAction(context);
  }

  @Override
  public String getDisplayText() {
    return "Notify of TACC requirement";
  }

  @Override
  public void close() {

  }

  private void completeAction(final RequiredActionContext context) {
    context.getUser().setAttribute(USER_ATTRIBUTE, Arrays.asList(Integer.toString(Time.currentTime())));
    context.success();
  }

}
