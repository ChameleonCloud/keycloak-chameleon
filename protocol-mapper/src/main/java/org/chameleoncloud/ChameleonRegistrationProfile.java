package org.chameleoncloud;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.MultivaluedMap;

import org.keycloak.authentication.FormContext;
import org.keycloak.authentication.ValidationContext;
import org.keycloak.authentication.forms.RegistrationPage;
import org.keycloak.authentication.forms.RegistrationProfile;
import org.keycloak.events.Details;
import org.keycloak.events.Errors;
import org.keycloak.models.utils.FormMessage;
import org.keycloak.models.UserModel;
import org.keycloak.services.messages.Messages;
import org.keycloak.services.validation.Validation;

public class ChameleonRegistrationProfile extends RegistrationProfile {
    public static final String PROVIDER_ID = "chameleon-registration-action";

    public static final String COUNTRY_OF_RESIDENCE = "country";

    public static final String COUNTRY_OF_CITIZENSHIP = "citizenship";

    public static final String MISSING_COUNTRY = "missingCountryMessage";

    public static final String MISSING_CITIZENSHIP = "missingCitizenshipMessage";

    @Override
    public String getHelpText() {
        return "Validates required user attributes and saves them to the user.";
    }

    @Override
    public void validate(ValidationContext context) {
        MultivaluedMap<String, String> formData = context.getHttpRequest().getDecodedFormParameters();
        List<FormMessage> errors = new ArrayList<>();

        context.getEvent().detail(Details.REGISTER_METHOD, "form");
        String eventError = Errors.INVALID_REGISTRATION;

        if (Validation.isBlank(formData.getFirst((RegistrationPage.FIELD_FIRST_NAME)))) {
            errors.add(new FormMessage(RegistrationPage.FIELD_FIRST_NAME, Messages.MISSING_FIRST_NAME));
        }

        if (Validation.isBlank(formData.getFirst((RegistrationPage.FIELD_LAST_NAME)))) {
            errors.add(new FormMessage(RegistrationPage.FIELD_LAST_NAME, Messages.MISSING_LAST_NAME));
        }

        if (Validation.isBlank(formData.getFirst(COUNTRY_OF_RESIDENCE))) {
          errors.add(new FormMessage(COUNTRY_OF_RESIDENCE, MISSING_COUNTRY));
        }

        if (Validation.isBlank(formData.getFirst(COUNTRY_OF_CITIZENSHIP))) {
          errors.add(new FormMessage(COUNTRY_OF_CITIZENSHIP, MISSING_CITIZENSHIP));
        }

        String email = formData.getFirst(Validation.FIELD_EMAIL);
        boolean emailValid = true;
        if (Validation.isBlank(email)) {
            errors.add(new FormMessage(RegistrationPage.FIELD_EMAIL, Messages.MISSING_EMAIL));
            emailValid = false;
        } else if (!Validation.isEmailValid(email)) {
            context.getEvent().detail(Details.EMAIL, email);
            errors.add(new FormMessage(RegistrationPage.FIELD_EMAIL, Messages.INVALID_EMAIL));
            emailValid = false;
        }

        if (emailValid && !context.getRealm().isDuplicateEmailsAllowed() && context.getSession().users().getUserByEmail(email, context.getRealm()) != null) {
            eventError = Errors.EMAIL_IN_USE;
            formData.remove(Validation.FIELD_EMAIL);
            context.getEvent().detail(Details.EMAIL, email);
            errors.add(new FormMessage(RegistrationPage.FIELD_EMAIL, Messages.EMAIL_EXISTS));
        }

        if (errors.size() > 0) {
            context.error(eventError);
            context.validationError(formData, errors);
            return;

        } else {
            context.success();
        }
    }

    @Override
    public void success(FormContext context) {
        UserModel user = context.getUser();
        MultivaluedMap<String, String> formData = context.getHttpRequest().getDecodedFormParameters();
        user.setFirstName(formData.getFirst(RegistrationPage.FIELD_FIRST_NAME));
        user.setLastName(formData.getFirst(RegistrationPage.FIELD_LAST_NAME));
        user.setEmail(formData.getFirst(RegistrationPage.FIELD_EMAIL));
        user.setAttribute(COUNTRY_OF_RESIDENCE, formData.get(COUNTRY_OF_RESIDENCE));
        user.setAttribute(COUNTRY_OF_CITIZENSHIP, formData.get(COUNTRY_OF_CITIZENSHIP));
    }

    @Override
    public String getDisplayType() {
        return "Profile Validation";
    }

    @Override
    public String getId() {
        return PROVIDER_ID;
    }

}
