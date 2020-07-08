<#import "template.ftl" as layout>
<@layout.registrationLayout displayMessage=false; section>
    <#if section = "header">
        ${msg("termsTitle")}
    <#elseif section = "form">
    <div id="kc-terms-text">
        <h3>Acceptable Use Policy</h3>
        <p>
            Chameleon resources are deployed, configured, and operated by the
            University of Chicago and the Texas Advanced Computing Center (TACC)
            to serve a large research community. It is important that all users
            are aware of and abide by this Acceptable Use Policy. Failure to do
            so may result in suspension or cancellation of the project and
            associated allocation and closure of all associated logins. Illegal
            transgressions will be addressed through the University of Texas at
            Austin (UT-Austin), the University of Chicago, and/or legal
            authorities.
        </p>
        <h4>Policies</h4>
        <p>
            Users agree to abide by the following policies. Failure to do so
            will lead to disciplinary actions described in the next section.
        </p>
        <b>All Users</b>
        <ul>
            <li>
                Sharing of User Credentials is strictly prohibited.
            </li>
            <li>
                Computing resources may only be used to perform research work
                consistent with the project’s goals and may not be used for
                commercial purposes, financial gain, personal gain, any unlawful
                purpose, or in a way that makes the work of other users
                difficult.
            </li>
            <li>
                Users are only allowed one account per person.
            </li>
            <li>
                Never infringe upon someone else's copyright. It is a violation
                of policy and federal law to participate in copyright
                infringement.
            </li>
            <li>
                Never try to circumvent login procedures or otherwise attempt to
                gain access where you are not allowed.
            </li>
            <li>
                Never deliberately scan or probe any information resource
                without prior authorization.“With great power comes great
                responsibility” Users of Chameleon are given much more extensive
                administrative privileges than they would have on typical
                resources, and as such must take great care and personal
                responsibility for the security of their VM’s.
            </li>
        </ul>
        <b>Project Leaders/PI’s</b>
        <ul>
            <li>
                Project Leads/PI's are responsible for notifying the Chameleon
                project when project users should be deactivated due to the
                departure of the user or termination of the project.
            </li>
            <li>
                Project Leads/PI’s are responsible for ensuring that users
                conducting work on the Chameleon testbed as part of their
                project observe the user terms and conditions described herein.
            </li>
            <li>
                Project Leads/PI’s will provide the Chameleon team with progress
                reports on their use of Chameleon.
            </li>
        </ul>
        <p>
            Because Chameleon resources are physically located at TACC and the
            University of Chicago, users are additionally subject to and are
            agreeing to abide by the following policies:
        </p>
        <ul>
            <li>
                TACC usage policy:
                https://portal.tacc.utexas.edu/tacc-usage-policy
            </li>
            <li>
                Univ of Chicago User Policy:
                https://wiki.ci.uchicago.edu/Resources/UserPolicy
            </li>
            <li>
                Univ of Chicago Password Policy:
                https://wiki.ci.uchicago.edu/Resources/PasswordGuidelines
            </li>
        </ul>
        <h4>Cryptocurrencies</h4>
        <p>
            Users are prohibited from running applications that mine
            cryptocurrency and/or use block-chain technology for personal gain.
            Exceptions must be requested in advance and will be approved at
            TACC’s discretion. Violations of this policy will result in user's
            access to TACC resources being terminated.
        </p>
        <h4>Disciplinary Actions</h4>
        <p>
            Disciplinary actions for infractions includes, but is not limited to
            the following:
        </p>
        <ul>
            <li>Written or verbal warnings</li>
            <li>Revocation of access privileges to Chameleon</li>
            <li>Termination of project(s) on Chameleon</li>
            <li>Criminal prosecution</li>
        </ul>
        <h4>Passwords</h4>
        <p>
            Users must choose a strong password that should be unique to your
            Chameleon username. All passwords are required to meet the following
            criteria:
        </p>
        <ul>
            <li>
                Must not contain your account name or parts of your full name.
            </li>
            <li>
                Must be a minimum of 8 characters in length.
            </li>
            <li>
                Must contain characters from at least three of the following:
                uppercase letters, lowercase letters, numbers, and symbols.
            </li>
        </ul>
        <h4>Account Deactivation Policy</h4>
        <p>
            Accounts will be deactivated for any of the following reasons:
        </p>
        <b>PI Request</b>
        <p>
            An account deactivation request by a PI will result in the account
            being denied access to use the PI's project's allocation, and if the
            account does not have access to another active project it will be
            then additionally be deactivated.
        </p>
        <b>Project Expiration</b>
        <p>
            Upon project expiration, all accounts (PI and users) will be
            immediately denied access to the allocation.
        </p>
        <b>Violation of Policy</b>
        <p>
            Any user account determined to be in violation of this policy will
            be subject to the Disciplinary Actions described above, and may
            immediately be denied access to Chameleon without notification
            depending on the severity of the event.
        </p>
        <b>Account Inactivity</b>
        <p>
            User accounts will be deactivated due to inactivity after 120
            calendar days. This will be done automatically and users will have
            to submit a ticket to have their account reactivated. A successful
            login to the Chameleon portal, or any TACC resource (including the
            TACC User Portal) will reset this inactivity timer.
        </p>
        <h4>Use of Protected Software and Data</h4>
        <p>
            PIs and users agree to NOT install or use any software or data that
            falls under the following protected categories: International
            Traffic in Arms Regulations (ITAR), Export Administration
            Regulations (EAR), Health Insurance Portability and Accountability
            Act (HIPAA), Federal Information Security Management Act (FISMA),
            Personally Identifiable Information (PII), or any other protected
            control without first contacting help@chameleoncloud.org, so that an
            appropriate agreement such as a Business Associate Agreement (BAA),
            Technology Control Plan (TCP), Memorandum Of Understanding (MOU), or
            other relevant agreement between UT-Austin, University of Chicago,
            and the PI or home intuition will be in place before such software
            or data can be installed or used on Chameleon. Violations of this
            policy will result in the immediate removal of said software and/or
            data and deactivation of related projects, allocations, and user
            accounts.
        </p>
        <p>
            Chameleon provides FPGA resources, and the FPGA build environment
            includes software that is subject to export control regulations
            resulting in restricted access as follows:
        </p>
        <ul>
            <li>
                <u>Access is allowed</u>: if a user is neither a Resident nor a
                Citizen of a Country of Concern, currently Iran, Iraq, North
                Korea, Sudan, and Syria.
            </li>
            <li>
                <u>Access might be allowed</u>: if a user is a US Resident, but
                legally residing here on a visa as a Citizen of a Country of
                Concern. The user will be contacted and asked to send a copy of
                their current visa and passport, or other acceptable
                documentation from their institution.
            </li>
            <li>
                <u>Access not allowed</u>: if a user is a Resident of a Country
                of Concern regardless of Country of Citizenship.
            </li>
            <li>
                <u>Access not allowed</u>: if a user is a Citizen of a Country
                of Concern and is not residing in the US.
            </li>
        </ul>
        <h4>Data Retention</h4>
        <p>
            Upon project expiration or termination of an active project all data
            will remain on Chameleon resources for thirty (30) days. All data
            stored in the TACC archival storage system will be maintained for a
            period of eighteen (18) months and will be read only.
        </p>
        <h4>User Support</h4>
        <p>
            Chameleon users are encouraged to request assistance when necessary.
            All requests for support must be submitted through the Chameleon
            Help System https://www.chameleoncloud.org/user/help/
        </p>
        <h4>Refund Requests</h4>
        <p>
            Users may submit refund requests for jobs that terminate abnormally
            that can be attributed to hardware and system software failure.
            Users should implement safeguards such as snapshotting to minimize
            the amount of lost time due to job failure.
        </p>
        <h4>User News</h4>
        <p>
            Information regarding important Chameleon activities, including
            system availability and upgrades, training classes, allocation
            renewal notifications, and holiday consulting coverage will be
            communicated to the user community via User News. Users are
            automatically subscribed to User News. Users may unsubscribe from
            User News, but then must assume responsibility and outcomes for not
            receiving User News via email by finding the information in the
            appropriate locations on the Chameleon portal.
        </p>
        <h4>Acknowledgement of Chameleon in Publications</h4>
        <p>
            An acknowledgement of support from the Chameleon project and the
            National Science Foundation should appear in any publication of
            material, whether copyrighted or not, that describes work which
            benefited from access to Chameleon cyberinfrastructure resources.
            The suggested acknowledgement is as follows: “Results presented in
            this paper were obtained using the Chameleon testbed supported by
            the National Science Foundation”.
        </p>
    </div>
    <form class="form-actions" action="${url.loginAction}" method="POST">
        <input class="${properties.kcButtonClass!} ${properties.kcButtonPrimaryClass!} ${properties.kcButtonLargeClass!}" name="accept" id="kc-accept" type="submit" value="${msg("doAccept")}"/>
        <input class="${properties.kcButtonClass!} ${properties.kcButtonDefaultClass!} ${properties.kcButtonLargeClass!}" name="cancel" id="kc-decline" type="submit" value="${msg("doDecline")}"/>
    </form>
    <div class="clearfix"></div>
    </#if>
</@layout.registrationLayout>
