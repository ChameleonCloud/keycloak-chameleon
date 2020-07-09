<#import "template.ftl" as layout>
<@layout.registrationLayout displayMessage=false; section>
    <#if section = "header">
        ${msg("loginTACCTitle")}
    <#elseif section = "form">
    <div id="chameleon-tacc-requirement">
        <p>
            As required by policy, if you use the Chameleon sites located at
            Texas Advanced Computing Center (TACC), you must sign up for and/or
            link a TACC user account to your Chameleon profile.
        </p>
        <p>
            If you do not yet have a TACC account, you can sign up for one at
            the <a href="https://portal.tacc.utexas.edu/" target="_blank">TACC User Portal</a>.
            If you already have a TACC account, you can link the account after
            you have finished your registration here by going to ${url.accountUrl}.
        </p>
        <p>
            Once you have linked your account, you should be able to log in to
            Chameleon with your TACC account; this authentication method is
            required when accessing either the baremetal site CHI@TACC or the
            KVM site KVM@TACC.
        </p>
    </div>
    <form class="form-actions" action="${url.loginAction}" method="POST">
        <input class="${properties.kcButtonClass!} ${properties.kcButtonPrimaryClass!} ${properties.kcButtonLargeClass!}" name="accept" id="kc-accept" type="submit" value="${msg("doContinue")}"/>
    </form>
    <div class="clearfix"></div>
    </#if>
</@layout.registrationLayout>
