package org.chameleoncloud;

import java.util.List;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import org.keycloak.authentication.RequiredActionContext;
import org.keycloak.authentication.RequiredActionProvider;
import org.keycloak.authentication.requiredactions.UpdateProfile;
import org.keycloak.authentication.requiredactions.util.UpdateProfileContext;
import org.keycloak.authentication.requiredactions.util.UserUpdateProfileContext;
import org.keycloak.events.EventBuilder;
import org.keycloak.events.EventType;
import org.keycloak.forms.login.LoginFormsProvider;
import org.keycloak.forms.login.freemarker.model.ProfileBean;
import org.keycloak.models.utils.FormMessage;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.services.resources.AttributeFormDataProcessor;
import org.keycloak.services.validation.Validation;

public class ChameleonUpdateProfile extends UpdateProfile {
    public static final String PROVIDER_ID = "chameleon-update-profile-action";

    public static final String UPDATE_PROFILE_FORM = "login-update-chameleon-profile.ftl";

    public static final String COUNTRY_OF_RESIDENCE = "country";

    public static final String COUNTRY_OF_CITIZENSHIP = "citizenship";

    public static final String MISSING_COUNTRY = "missingCountryMessage";

    public static final String MISSING_CITIZENSHIP = "missingCitizenshipMessage";

    @Override
    public void requiredActionChallenge(RequiredActionContext context) {
        final Response challenge = this.createChallenge(context.form(), context.getUser(), context.getRealm(), null);
        context.challenge(challenge);
    }

    @Override
    public void processAction(RequiredActionContext context) {
        final EventBuilder event = context.getEvent();
        event.event(EventType.UPDATE_PROFILE);
        final MultivaluedMap<String, String> formData = context.getHttpRequest().getDecodedFormParameters();
        final UserModel user = context.getUser();
        final RealmModel realm = context.getRealm();

        final List<FormMessage> errors = Validation.validateUpdateProfileForm(realm, formData);
        if (errors != null && !errors.isEmpty()) {
            final LoginFormsProvider form = context.form()
                .setErrors(errors)
                .setFormData(formData);
            final Response challenge = this.createChallenge(form, user, realm, formData);
            context.challenge(challenge);
            return;
        }

        user.setFirstName(formData.getFirst("firstName"));
        user.setLastName(formData.getFirst("lastName"));
        user.setAttribute(COUNTRY_OF_RESIDENCE, formData.get(COUNTRY_OF_RESIDENCE));
        user.setAttribute(COUNTRY_OF_CITIZENSHIP, formData.get(COUNTRY_OF_CITIZENSHIP));

        AttributeFormDataProcessor.process(formData, realm, user);

        context.success();

    }

    private Response createChallenge(LoginFormsProvider form, UserModel user, RealmModel realm, MultivaluedMap<String, String> formData) {
        final UpdateProfileContext updateProfileCtx = new UserUpdateProfileContext(realm, user);
        form.setAttribute("user", new ProfileBean(updateProfileCtx, formData));
        return form.createForm(UPDATE_PROFILE_FORM);
    }

    @Override
    public String getId() {
        return PROVIDER_ID;
    }

    @Override
    public String getDisplayText() {
        return "Update Chameleon Profile";
    }

    private static final ChameleonUpdateProfile SINGLETON = new ChameleonUpdateProfile();

    @Override
    public RequiredActionProvider create(KeycloakSession session) {
        return SINGLETON;
    }

}
