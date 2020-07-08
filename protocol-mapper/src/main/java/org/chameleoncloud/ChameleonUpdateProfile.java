package org.chameleoncloud;

import java.util.List;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import org.keycloak.authentication.RequiredActionContext;
import org.keycloak.authentication.RequiredActionProvider;
import org.keycloak.authentication.requiredactions.UpdateProfile;
import org.keycloak.events.EventBuilder;
import org.keycloak.events.EventType;
import org.keycloak.models.utils.FormMessage;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.UserModel.RequiredAction;
import org.keycloak.services.resources.AttributeFormDataProcessor;
import org.keycloak.services.validation.Validation;

public class ChameleonUpdateProfile extends UpdateProfile {
    public static final String PROVIDER_ID = "chameleon-update-profile-action";

    public static final String COUNTRY_OF_RESIDENCE = "country";

    public static final String COUNTRY_OF_CITIZENSHIP = "citizenship";

    public static final String MISSING_COUNTRY = "missingCountryMessage";

    public static final String MISSING_CITIZENSHIP = "missingCitizenshipMessage";


    @Override
    public void requiredActionChallenge(RequiredActionContext context) {
        Response challenge = context.form().createResponse(RequiredAction.UPDATE_PROFILE);
        context.challenge(challenge);
    }

    @Override
    public void processAction(RequiredActionContext context) {
        EventBuilder event = context.getEvent();
        event.event(EventType.UPDATE_PROFILE);
        MultivaluedMap<String, String> formData = context.getHttpRequest().getDecodedFormParameters();
        UserModel user = context.getUser();
        RealmModel realm = context.getRealm();

        List<FormMessage> errors = Validation.validateUpdateProfileForm(realm, formData);
        if (errors != null && !errors.isEmpty()) {
            Response challenge = context.form()
                    .setErrors(errors)
                    .setFormData(formData)
                    .createResponse(RequiredAction.UPDATE_PROFILE);
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

    @Override
    public String getId() {
        return PROVIDER_ID;
    }

    @Override
    public String getDisplayText() {
        return "Update Profile";
    }

    private static final ChameleonUpdateProfile SINGLETON = new ChameleonUpdateProfile();

    @Override
    public RequiredActionProvider create(KeycloakSession session) {
        return SINGLETON;
    }

}
