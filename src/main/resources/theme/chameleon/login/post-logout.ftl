<#import "template.ftl" as layout>
<@layout.registrationLayout displayInfo=true displayWide=false; section>
  <#if section = "header">
    You have been logged out.
  <#elseif section = "form">
    <#if client?? && client.name??>
    <p>You are now logged out of <strong>${client.name}</strong>.</p>
    </#if>
    <p>
      If you are using a public computer or wish to securely log out, you should
      additionally log out your authenticating service. <strong>Note:</strong>
      if you logged in using Google or ORCiD, your authenticating service is
      Globus.
    </p>
    <ul>
      <li><strong>Globus</strong>: <a href="https://auth.globus.org/v2/web/logout" title="Log out of Globus" rel="noopener noreferrer" target="_blank">logout</a>
      <li><strong>TAS</strong>: <a href="https://identity.tacc.cloud/auth/realms/chameleon/protocol/openid-connect/logout" title="Log out of TAS" rel="noopener noreferrer" target="_blank">logout</a>
    </ul>
    <p>
      You may still have active sessions on other Chameleon applications or
      experiment sites. If you wish to log out everywhere, you must visit each
      and explicitly sign out using the built-in logout functionality.
    </p>
    <#--
    <ul>
      <li>Jupyter environment: <a href="https://jupyter.chameleoncloud.org/auth/logout" title="Log out of Jupyter environment" rel="noopener noreferrer" target="_blank">logout</a></li>
      <li>Chameleon user portal: <a href="https://www.chameleoncloud.org/logout" title="Log out of Chameleon user portal" rel="noopener noreferrer" target="_blank">logout</a></li>
      <li>CHI@TACC (GUI): <a href="https://chi.tacc.chameleoncloud.org/auth/logout" title="Log out of CHI@TACC (GUI)" rel="noopener noreferrer" target="_blank">logout</a></li>
      <li>CHI@UC (GUI): <a href="https://chi.uc.chameleoncloud.org/auth/logout" title="Log out of CHI@UC (GUI)" rel="noopener noreferrer" target="_blank">logout</a></li>
      <li>KVM@TACC (GUI): <a href="https://kvm.tacc.chameleoncloud.org/auth/logout" title="Log out of KVM@TACC (GUI)" rel="noopener noreferrer" target="_blank">logout</a></li>
    </ul>
    -->
  <#elseif section = "info" >
  </#if>
</@layout.registrationLayout>
