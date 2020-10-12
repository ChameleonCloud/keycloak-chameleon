<#import "template.ftl" as layout>
<@layout.registrationLayout displayInfo=social.displayInfo displayWide=false; section>
    <#if section = "header">
        ${msg("doLogIn")}
    <#elseif section = "form">
    <div id="kc-form" <#if social.providers??>class="${properties.kcContentWrapperClass!}"</#if>>
        <#if social.providers??>
            <#assign globus = social.providers?filter(p -> p.alias == "globus")?first!"missing">
            <#assign tacc = social.providers?filter(p -> p.alias == "tacc")?first!"missing">
            <#if globus != "missing" && tacc != "missing">
                <#-- Login workflow -->
                <#if client.baseUrl??>
                <div class="kc-form-login-main-option">
                    <a href="${client.baseUrl}auth/force-password-login" class="btn btn-default btn-primary btn-xlg">
                        <span>Go to the old sign-in page</span>
                    </a>
                </div>
                <div class="kc-form-divider">or</div>
                </#if>
                <div class="kc-form-login-main-option">
                    <div class="alert alert-warning text-left">
                        <h4 class="alert-heading">Attention Chameleon users!</h4>
                        <p>
                            Through the months of October and November we will
                            be migrating to federated identity (read more
                            <a class="alert-link" href="https://www.chameleoncloud.org/blog/2020/10/05/chameleon-access-federated-login-coming-soon/" target="_blank" rel="noopener noreferrer">here</a>.)
                            If you would like use to use the old login, use the
                            above sign-in button.
                        </p>
                        <p>
                            <strong>Important!</strong> Make sure you
                            <a class="alert-link" href="https://chameleoncloud.readthedocs.io/en/latest/user/federation/federation_migration.html" target="_blank" rel="noopener noreferrer">migrate your account</a>
                            before signing in via federated login for the first
                            time, if you already have a Chameleon account.
                        </p>
                    </div>
                    <a href="${globus.loginUrl}" class="btn btn-default btn-lg">
                        <span>Sign in via federated login</span>
                    </a>
                </div>
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
    </#if>
</@layout.registrationLayout>
