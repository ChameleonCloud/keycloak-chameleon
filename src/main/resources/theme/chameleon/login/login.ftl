<#import "template.ftl" as layout>
<@layout.registrationLayout displayInfo=social.displayInfo displayWide=false; section>
    <#if section = "header">
        ${msg("doLogIn")}
    <#elseif section = "form">
    <div id="kc-form" <#if social.providers??>class="${properties.kcContentWrapperClass!}"</#if>>
        <#if social.providers?? && (social.providers?size > 0)>
            <#assign globus = social.providers?filter(p -> p.alias == "globus")?first>
            <#assign tacc = social.providers?filter(p -> p.alias == "tacc")?first>
            <div class="kc-form-col">
                <p>
                    Chameleon supports login and registration via <strong><a href="https://www.globus.org/what-we-do" target="_blank" rel="noreferrer">Globus Auth</a></strong>.
                    In most cases, you will not need to sign up for any new account, and can
                    log in with your existing host institution credentials. If your host institution
                    is not recognized by Globus, you can log in with any Google account, or
                    register for a free <a href="https://globusid.org/what" target="_blank" rel="noreferrer">Globus ID</a>.
                </p>
            </div>
            <div class="kc-form-col">
                <div class="kc-form-login-options">
                    <a href="${globus.loginUrl}" class="btn btn-default btn-block btn-lg btn-globus">
                        <img src="${url.resourcesPath}/img/globus.png" alt="Globus logo" /><span>Sign in with Globus</span>
                    </a>
                </div>
                <a href="${tacc.loginUrl}">Looking to log in with your old Chameleon account?</a>
            </div>
        <#else>
            <p>
                <strong>Error:</strong> no enabled identity providers available.
            </p>
        </#if>
      </div>
    <#elseif section = "info" >
    </#if>
</@layout.registrationLayout>
