package org.chameleoncloud;

import org.keycloak.forms.login.LoginFormsProvider;
import org.keycloak.forms.login.freemarker.FreeMarkerLoginFormsProvider;
import org.keycloak.forms.login.freemarker.model.ClientBean;
import org.keycloak.models.ClientModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.services.resource.RealmResourceProvider;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.CacheControl;
import jakarta.ws.rs.core.Response;

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
    public Response get(@QueryParam("client_id") String clientId) {
        final LoginFormsProvider form = new FreeMarkerLoginFormsProvider(session);

        if (clientId != null) {
            final RealmModel realm = session.getContext().getRealm();
            final ClientModel client = session.clients().getClientById(realm, clientId);
            if (client != null) {
                form.setAttribute("client", new ClientBean(session, client));
            }
        }

        Response r = form.createForm("post-logout.ftl");
        CacheControl cc = new CacheControl();
        cc.setNoCache(true);
        return Response.fromResponse(r).cacheControl(cc).build();
    }

    @Override
    public void close() {
    }
}
