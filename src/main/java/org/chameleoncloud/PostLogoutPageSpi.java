package org.chameleoncloud;

import org.keycloak.provider.Provider;
import org.keycloak.provider.Spi;
import org.keycloak.services.resource.RealmResourceProviderFactory;

public class PostLogoutPageSpi implements Spi {
    @Override
    public boolean isInternal() {
        return false;
    }

    @Override
    public String getName() {
        return "chameleon-post-logout-page";
    }

    @Override
    public Class<? extends Provider> getProviderClass() {
        return PostLogoutPageProvider.class;
    }

    @Override
    public Class<? extends RealmResourceProviderFactory> getProviderFactoryClass() {
        return PostLogoutPageProviderFactory.class;
    }

    @Override
    public boolean isEnabled() {
        return Spi.super.isEnabled();
    }
}
