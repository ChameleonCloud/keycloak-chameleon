package org.chameleoncloud;

import org.keycloak.provider.Provider;
import org.keycloak.provider.Spi;
import org.keycloak.services.resource.RealmResourceProviderFactory;

public class ExtendedApiSpi implements Spi {
    @Override
    public boolean isInternal() {
        return false;
    }

    @Override
    public String getName() {
        return "chameleon-extended-api";
    }

    @Override
    public Class<? extends Provider> getProviderClass() {
        return ExtendedApiResourceProvider.class;
    }

    @Override
    public Class<? extends RealmResourceProviderFactory> getProviderFactoryClass() {
        return ExtendedApiResourceProviderFactory.class;
    }

    @Override
    public boolean isEnabled() {
        return Spi.super.isEnabled();
    }
}
