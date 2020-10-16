<#import "template.ftl" as layout>
<@layout.registrationLayout displayInfo=social.displayInfo displayWide=false; section>
    <#if section = "header">
      ${msg("doRegister")}
    <#elseif section = "form">
    <div id="kc-form" <#if social.providers??>class="${properties.kcContentWrapperClass!}"</#if>>
      <div>
        <p>
          Welcome to Chameleon! We're happy you're here.
        </p>
      </div>
        <#if social.providers??>
            <#assign globus = social.providers?filter(p -> p.alias == "globus")?first!"missing">
            <#assign tacc = social.providers?filter(p -> p.alias == "tacc")?first!"missing">
            <#if globus != "missing" && tacc != "missing">
                <#-- Registration workflow -->
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
            <#else>
                <#-- Misconfiguration -->
                <p>
                    <strong>Error:</strong> no enabled identity providers available.
                </p>
            </#if>
        </#if>
    </div>
    <#elseif section = "info">
    <div>
      <p>
        Already have an account? How did you get here?<br>
        <a href="${url.loginUrl}">Never fear, just log in here.</a>
      </p>
    </div>
    </#if>
</@layout.registrationLayout>
