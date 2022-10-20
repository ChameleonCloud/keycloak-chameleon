package org.chameleoncloud;

import org.keycloak.provider.Provider;
import org.keycloak.provider.Spi;
import org.keycloak.services.resource.RealmResourceProviderFactory;

public class TermsAndConditionsPageSpi implements Spi {
    @Override
    public boolean isInternal() {
        return false;
    }

    @Override
    public String getName() {
        return "chameleon-terms-and-conditions-page";
    }

    @Override
    public Class<? extends Provider> getProviderClass() {
        return TermsAndConditionsPageProvider.class;
    }

    @Override
    public Class<? extends RealmResourceProviderFactory> getProviderFactoryClass() {
        return TermsAndConditionsPageProviderFactory.class;
    }

    @Override
    public boolean isEnabled() {
        return Spi.super.isEnabled();
    }
}
