package org.daisleyharrison.security.spring;

import org.daisleyharrison.security.common.spi.KeyServiceProvider;
import org.daisleyharrison.security.services.key.KeyService;

public class KeyServiceFactory extends SecurityServiceFactory<KeyServiceProvider,KeyService> {

    public KeyServiceFactory() {
        super(KeyServiceProvider.class, KeyService.class);
    }

}