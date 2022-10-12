package org.chameleoncloud;

import org.jboss.logging.Logger;
import org.keycloak.Config;
import org.keycloak.authentication.RequiredActionContext;
import org.keycloak.authentication.RequiredActionFactory;
import org.keycloak.authentication.RequiredActionProvider;
import org.keycloak.common.util.Time;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.models.UserModel;

public class ChameleonAddJoinDate implements RequiredActionProvider, RequiredActionFactory {

    // convention seems to indicate that this SHOULD_BE_ALL_CAPS
    public static final String PROVIDER_ID = "chameleon_add_join_date";

    public static final String USER_ATTRIBUTE = "joinDate";

    private static final Logger logger = Logger.getLogger(ChameleonAddJoinDate.class);

    @Override
    public void close() {
        // NOOP
    }

    @Override
    public void evaluateTriggers(final RequiredActionContext context) {
        // evaluateTriggers should update to look at the user and see if the join date
        // field is set. If not set, it should add the set join date as a required
        // action
        final UserModel user = context.getUser();
        final String join_date = user.getFirstAttribute(USER_ATTRIBUTE);

        // Check if attribute is empty
        if (join_date == null || join_date.isEmpty()) {
            logger.debug("Join date is not set, adding required action");
            context.getUser().addRequiredAction(PROVIDER_ID);
        }
    }

    @Override
    public void requiredActionChallenge(RequiredActionContext context) {
        final UserModel user = context.getUser();

        final String join_date = user.getFirstAttribute(USER_ATTRIBUTE);

        // Don't overwrite join date if the required action gets re-run
        if (join_date == null || join_date.isEmpty()) {
            logger.debug("Setting Join Date to Now.");
            user.setSingleAttribute(USER_ATTRIBUTE, Integer.toString(Time.currentTime()));
            // Set context.success here, to prevent user interaction.
        }
        context.success();
    }

    @Override
    public void processAction(final RequiredActionContext context) {
        // NOOP
    }

    @Override
    public String getDisplayText() {
        return "Set join date";
    }

    @Override
    public String getId() {
        return PROVIDER_ID;
    }

    @Override
    public RequiredActionProvider create(KeycloakSession session) {
        return this;
    }

    @Override
    public void init(Config.Scope config) {
        // NOOP
    }

    @Override
    public void postInit(KeycloakSessionFactory factory) {
        // NOOP
    }
}
