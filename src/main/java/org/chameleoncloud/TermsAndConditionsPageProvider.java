package org.chameleoncloud;

import org.keycloak.forms.login.LoginFormsProvider;
import org.keycloak.forms.login.freemarker.FreeMarkerLoginFormsProvider;
import org.keycloak.models.KeycloakSession;
import org.keycloak.services.resource.RealmResourceProvider;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.CacheControl;
import jakarta.ws.rs.core.Response;

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
    public Response get(@QueryParam("client_id") String clientId) {
        final LoginFormsProvider form = new FreeMarkerLoginFormsProvider(session);
        form.setAttribute("hideActions", true);
        Response r = form.createForm("terms.ftl");
        CacheControl cc = new CacheControl();
        cc.setNoCache(true);
        return Response.fromResponse(r).cacheControl(cc).build();
    }

    @Override
    public void close() {
    }
}
