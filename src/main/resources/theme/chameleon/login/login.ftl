<#import "template.ftl" as layout>
<@layout.registrationLayout displayInfo=social.displayInfo; section>
    <#if section = "header">
        ${msg("doLogIn")}
    <#elseif section = "form">
    <div id="kc-form" <#if social.providers??>class="${properties.kcContentWrapperClass!}"</#if>>
        <#if social.providers??>
            <#assign globus = social.providers?filter(p -> p.alias == "globus")?first!"missing">
            <#assign tacc = social.providers?filter(p -> p.alias == "tacc")?first!"missing">
            <#if globus != "missing" && tacc != "missing">
                <#-- Login workflow -->
                <div class="kc-form-login-main-option">
                    <a href="${globus.loginUrl}" class="btn btn-primary btn-xlg">
                        <span>Sign in via federated login</span>
                    </a>
                </div>
                <div class="kc-form-divider">or</div>
                <div class="kc-form-login-options">
                    <a href="${globus.loginUrl}" class="btn btn-default btn-lg">
                        <img src="${url.resourcesPath}/img/google_logo.svg" alt="Google logo" /><span>Google</span>
                    </a>
                    <a href="${globus.loginUrl}" class="btn btn-default btn-lg">
                        <img src="${url.resourcesPath}/img/orcid_logo.svg" alt="ORCiD logo" /><span>ORCiD</span>
                    </a>
                    <a href="${tacc.loginUrl}" class="btn btn-default btn-lg">
                        <span>TAS</span>
                    </a>
                </div>
            <#elseif tacc != "missing">
                <#-- "Link identity" workflow -->
                <div class="kc-form-login-main-option">
                    <p>
                        To finish linking your federated/SSO account to your
                        existing Chameleon account, you will be asked to confirm
                        your existing username and password.
                    </p>
                    <a href="${tacc.loginUrl}" class="btn btn-default btn-block btn-lg">
                        <span>Sign in with Chameleon username/password</span>
                    </a>
                </div>
            <#else>
                <#-- Misconfiguration -->
                <p>
                    <strong>Error:</strong> no enabled identity providers available.
                </p>
            </#if>
        </#if>
      </div>
    <#elseif section = "info" >
    <div>
        <p>
            Don't have an account? <a href="${url.registrationUrl}">Sign up now.</a>
        </p>
    </div>
    </#if>
</@layout.registrationLayout>
