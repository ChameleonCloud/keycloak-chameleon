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
                <div class="kc-form-login-main-option">
                    <#if client.baseUrl??>
                    <div class="alert alert-warning">
                        <h4 class="alert-heading">Attention existing Chameleon users!</h4>
                        <p>
                            If you signed up before <strong>November 2020</strong>,
                            you should use the old sign in method until you
                            <a class="alert-link" href="https://chameleoncloud.readthedocs.io/en/latest/getting-started/federation.html" target="_blank" rel="noopener noreferrer">migrate your account</a>.
                            For more information, please refer to
                            <a class="alert-link" href="https://www.chameleoncloud.org/blog/2020/10/05/chameleon-access-federated-login-coming-soon/" target="_blank" rel="noopener noreferrer">this announcement</a>.
                        </p>
                        <a href="${client.baseUrl}auth/force-password-login" class="btn btn-default btn-primary btn-xlg">
                            <span>Go to old sign-in page</span>
                        </a>
                    </div>
                    </#if>
                    <div class="kc-form-login-new-option">
                        <p>
                            <span class="badge badge-primary">Beta</span>&nbsp;
                            Chameleon supports login from a wide variety of academic
                            institutions powered by <a href="https://www.globus.org" rel="noopener noreferrer" target="_blakn">Globus Auth</a>.
                            If your university, national lab or facility does not
                            support SSO, you can log in with any Google account,
                            <a href="https://orcid.org" rel="noopener noreferrer">ORCiD</a>, or with a
                            <a href="https://globusid.org/what" rel="noopener noreferrer" target="_blank">Globus ID</a>.
                        </p>
                        <a href="${globus.loginUrl}" class="btn btn-default btn-lg">
                            <span>Sign in via institutional SSO</span>
                        </a>
                    </div>
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
                        <span>Sign in with old Chameleon password</span>
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
