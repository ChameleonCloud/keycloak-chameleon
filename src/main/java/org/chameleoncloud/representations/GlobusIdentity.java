package org.chameleoncloud.representations;

import com.fasterxml.jackson.annotation.JsonProperty;

public class GlobusIdentity {

    @JsonProperty("")
    protected String name;

    @JsonProperty("")
    protected String username;

    @JsonProperty("")
    protected String identity_provider_display_name;

    @JsonProperty("")
    protected String identity_provider;

    @JsonProperty("")
    protected Long last_authentication;

    @JsonProperty("")
    protected String sub;

    @JsonProperty("")
    protected String email;

    @JsonProperty("")
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

    public String getSubject() {
        return this.sub;
    }

    public String getEmail() {
        return this.email;
    }

    public String getOrganization() {
        return this.organization;
    }

}
