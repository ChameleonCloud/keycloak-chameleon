package org.chameleoncloud.representations;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.keycloak.representations.IDToken;

public class GlobusIDToken extends IDToken {
    /*
     * openid: Requests that an OpenID Connect id_token be returned as part of the
     * OAuth2 Access Token Response, with the following claims: sub: The Globus Auth
     * identity id of the effective identity of the logged in Globus account. This
     * effective may be the primary identity, or the appropriate linked identity if
     * this client requires an identity from a particular provider. iss: The URL
     * "https://auth.globus.org" at_hash: Per OpenID Connect specification. aud: Per
     * OpenID Connect specification. exp: Per OpenID Connect specification. iat: Per
     * OpenID Connect specification. nonce: Per OpenID Connect specification.
     * last_authentication: The last time that this identity authenticated, returned
     * as a Unix/epoch timestamp. identity_set: The identities linked to this
     * account.
     * 
     * email: Adds the following claim in the id_token: email: The email address
     * associated with the identity provided in the "sub" claim.
     * 
     * profile: Adds the following claim in the id_token: name: The identity’s full
     * name (e.g. Jane Doe) in displayable form. organization: The identity’s
     * organization. preferred_username: The identity username for the effective
     * identity ID provided by the 'sub' claim. identity_provider: The ID of the
     * identity provider for this identity. identity_provider_display_name: The name
     * of the identity provider for this identity.
     */

    private static final long serialVersionUID = 1L;

    @JsonProperty("")
    protected Long last_authentication;

    @JsonProperty("identity_set")
    protected GlobusIdentity[] identity_set;

    @JsonProperty("")
    protected String organization;

    @JsonProperty("")
    protected String identity_provider_display_name;

    @JsonProperty("")
    protected String identity_provider;

}
