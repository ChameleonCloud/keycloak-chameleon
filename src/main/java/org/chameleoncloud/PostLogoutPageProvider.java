package org.chameleoncloud;

import javax.ws.rs.GET;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import org.jboss.resteasy.annotations.cache.NoCache;
import org.keycloak.forms.login.LoginFormsProvider;
import org.keycloak.forms.login.freemarker.FreeMarkerLoginFormsProvider;
import org.keycloak.forms.login.freemarker.model.ClientBean;
import org.keycloak.models.ClientModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.services.resource.RealmResourceProvider;
import org.keycloak.theme.FreeMarkerUtil;

public class PostLogoutPageProvider implements RealmResourceProvider {
  private KeycloakSession session;

  public PostLogoutPageProvider(KeycloakSession session) {
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

    if (clientId != null) {
      final RealmModel realm = session.getContext().getRealm();
      final ClientModel client = session.clientStorageManager().getClientByClientId(clientId, realm);
      if (client != null) {
        form.setAttribute("client", new ClientBean(session, client));
      }
    }

    return form.createForm("post-logout.ftl");
  }

  @Override
  public void close() {
  }
}
