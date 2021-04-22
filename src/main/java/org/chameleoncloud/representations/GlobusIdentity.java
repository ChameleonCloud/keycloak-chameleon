package org.chameleoncloud.representations;

import com.fasterxml.jackson.annotation.JsonProperty;

public class GlobusIdentity {

    @JsonProperty("name")
    protected String name;

    @JsonProperty("username")
    protected String username;

    @JsonProperty("identity_provider_display_name")
    protected String identity_provider_display_name;

    @JsonProperty("identity_provider")
    protected String identity_provider;

    @JsonProperty("last_authentication")
    protected Long last_authentication;

    @JsonProperty("sub")
    protected String sub;

    @JsonProperty("email")
    protected String email;

    @JsonProperty("organization")
    protected String organization;

    public String getName() {
        return this.name;
    }

    public String getUsername() {
        return this.username;
    }

    public String getIdentityProviderDisplayName() {
        return this.identity_provider_display_name;
    }

    public String getIdentityProvider() {
        return this.identity_provider;
    }

    public Long getLastAuthentication() {
        return this.last_authentication;
    }

    public String getSub() {
        return this.sub;
    }

    public String getEmail() {
        return this.email;
    }

    public String getOrganization() {
        return this.organization;
    }

}
