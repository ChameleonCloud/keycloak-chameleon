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
                    <a href="${tacc.loginUrl}" id="tas-login" class="btn btn-default btn-lg">
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
    <style>
        #tas-modal {
            display: none;
            position: fixed;
            z-index: 9999;
            left: 0; top: 0; width: 100%; height: 100%;
            background-color: rgba(0,0,0,0.6);
        }
        #tas-modal .content {
            background: #fff; padding: 20px; border-radius: 10px;
            max-width: 400px; margin: 10% auto; text-align: center;
        }
    </style>

    <div id="tas-modal">
        <div class="content">
            <p>
                Login via TAS is being deprecated in January 2026.
                We recommend that you sign in via Globus federated login.
            </p>
            <p>
                If you have an existing Chameleon account via TAS, you can follow our
                <a href="https://chameleoncloud.readthedocs.io/en/latest/user/federation.html#account-linking" target="_blank">documentation</a>
                to link it to Globus.
            </p>
            <button id="tas-continue" class="btn btn-default btn-lg">Continue with TAS</button>
            <button id="tas-cancel" class="btn btn-default btn-lg">Cancel</button>
        </div>
    </div>

    <script>
        document.addEventListener("DOMContentLoaded", function() {
            const tasLink = document.getElementById("tas-login");
            const modal = document.getElementById("tas-modal");
            const continueBtn = document.getElementById("tas-continue");
            const cancelBtn = document.getElementById("tas-cancel");

            tasLink.addEventListener("click", function(event) {
                event.preventDefault();
                modal.style.display = "block";
            });

            continueBtn.addEventListener("click", function() {
                window.location.href = tasLink.href;
            });

            cancelBtn.addEventListener("click", function() {
                modal.style.display = "none";
            });
        });
    </script>
</@layout.registrationLayout>
