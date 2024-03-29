package org.chameleoncloud;

import org.keycloak.authentication.RequiredActionContext;
import org.keycloak.authentication.RequiredActionProvider;
import org.keycloak.authentication.forms.RegistrationPage;
import org.keycloak.authentication.requiredactions.UpdateProfile;
import org.keycloak.authentication.requiredactions.util.UpdateProfileContext;
import org.keycloak.authentication.requiredactions.util.UserUpdateProfileContext;
import org.keycloak.events.EventBuilder;
import org.keycloak.events.EventType;
import org.keycloak.forms.login.LoginFormsProvider;
import org.keycloak.forms.login.freemarker.model.ProfileBean;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.utils.FormMessage;
import org.keycloak.services.messages.Messages;
import org.keycloak.services.validation.Validation;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

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

        final List<FormMessage> errors = this.validateForm(formData);
        if (!errors.isEmpty()) {
            final LoginFormsProvider form = context.form()
                    .setErrors(errors)
                    .setFormData(formData);
            final Response challenge = this.createChallenge(form, user, realm, formData);
            context.challenge(challenge);
            return;
        }

        user.setFirstName(formData.getFirst(RegistrationPage.FIELD_FIRST_NAME));
        user.setLastName(formData.getFirst(RegistrationPage.FIELD_LAST_NAME));
        user.setAttribute(COUNTRY_OF_RESIDENCE, formData.get(this.userAttributeField(COUNTRY_OF_RESIDENCE)));
        user.setAttribute(COUNTRY_OF_CITIZENSHIP, formData.get(this.userAttributeField(COUNTRY_OF_CITIZENSHIP)));

        context.success();

    }

    private String userAttributeField(String field) {
        return "user.attributes." + field;
    }

    private List<FormMessage> validateForm(MultivaluedMap<String, String> formData) {
        List<FormMessage> errors = new ArrayList<>();

        if (Validation.isBlank(formData.getFirst(RegistrationPage.FIELD_FIRST_NAME))) {
            errors.add(new FormMessage(RegistrationPage.FIELD_FIRST_NAME, Messages.MISSING_FIRST_NAME));
        }

        if (Validation.isBlank(formData.getFirst(RegistrationPage.FIELD_LAST_NAME))) {
            errors.add(new FormMessage(RegistrationPage.FIELD_LAST_NAME, Messages.MISSING_LAST_NAME));
        }

        final String countryField = this.userAttributeField(COUNTRY_OF_RESIDENCE);

        if (Validation.isBlank(formData.getFirst(countryField))) {
            errors.add(new FormMessage(countryField, MISSING_COUNTRY));
        }

        final String citizenshipField = this.userAttributeField(COUNTRY_OF_CITIZENSHIP);

        if (Validation.isBlank(formData.getFirst(citizenshipField))) {
            errors.add(new FormMessage(citizenshipField, MISSING_CITIZENSHIP));
        }

        return errors;
    }

    private Response createChallenge(LoginFormsProvider form, UserModel user, RealmModel realm, MultivaluedMap<String, String> formData) {
        final UpdateProfileContext updateProfileCtx = new UserUpdateProfileContext(realm, user);
        form.setAttribute("user", new ProfileBean(updateProfileCtx, formData));
        if (formData != null) {
            // The ProfileBean class tries to read the email from the form data,
            // but we are not sending it (it is disabled in the form). We have
            // to therefore spoof it here from the actual user model.
            formData.add("email", user.getEmail());
        }
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
