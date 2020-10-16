package org.chameleoncloud;

import javax.ws.rs.GET;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import org.jboss.resteasy.annotations.cache.NoCache;
import org.keycloak.forms.login.LoginFormsProvider;
import org.keycloak.forms.login.freemarker.FreeMarkerLoginFormsProvider;
import org.keycloak.models.KeycloakSession;
import org.keycloak.services.resource.RealmResourceProvider;
import org.keycloak.theme.FreeMarkerUtil;

public class TermsAndConditionsPageProvider implements RealmResourceProvider {
  private KeycloakSession session;

  public TermsAndConditionsPageProvider(KeycloakSession session) {
    this.session = session;
  }

  @Override
  public Object getResource() {
    return this;
  }

  @GET
  @NoCache
  public Response get(@QueryParam("client_id") String clientId) {
    final LoginFormsProvider form = new FreeMarkerLoginFormsProvider(session, new FreeMarkerUtil());
    form.setAttribute("hideActions", true);
    return form.createForm("terms.ftl");
  }

  @Override
  public void close() {
  }
}
