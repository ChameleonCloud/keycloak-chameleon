package org.chameleoncloud;

import java.util.Arrays;
import java.util.List;

import org.keycloak.authentication.RequiredActionContext;
import org.keycloak.authentication.RequiredActionProvider;
import org.keycloak.common.util.Time;
import org.keycloak.models.UserModel;

public class AddUserSignupDate implements RequiredActionProvider {

    public static final String USER_ATTRIBUTE = "joinDate";

    @Override
    public void close() {
        // NOOP
    }

    @Override
    public void evaluateTriggers(final RequiredActionContext context) {
        final UserModel user = context.getUser();
        final List<String> list = user.getAttribute(USER_ATTRIBUTE);

        if (list == null || list.isEmpty()) {
          user.setAttribute(USER_ATTRIBUTE, Arrays.asList(Integer.toString(Time.currentTime())));
        }
    }

    @Override
    public void requiredActionChallenge(RequiredActionContext context) {
        // NOOP
    }

    @Override
    public void processAction(final RequiredActionContext context) {
        context.success();
    }
}
