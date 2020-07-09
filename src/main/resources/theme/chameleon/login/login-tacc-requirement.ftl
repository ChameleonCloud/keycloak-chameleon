<#import "template.ftl" as layout>
<@layout.registrationLayout displayMessage=false; section>
    <#if section = "header">
        ${msg("loginTACCTitle")}
    <#elseif section = "form">
    <div id="chameleon-tacc-requirement">
        <p>
            In order to use the TACC sites, you must have an account.
            <a href="https://portal.tacc.utexas.edu/" target="_blank">TACC User Portal</a>
        </p>
    </div>
    <form class="form-actions" action="${url.loginAction}" method="POST">
        <input class="${properties.kcButtonClass!} ${properties.kcButtonPrimaryClass!} ${properties.kcButtonLargeClass!}" name="accept" id="kc-accept" type="submit" value="${msg("doContinue")}"/>
    </form>
    <div class="clearfix"></div>
    </#if>
</@layout.registrationLayout>
