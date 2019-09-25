package org.daisleyharrison.security.common.spi;

import org.daisleyharrison.security.common.models.Stage;

public interface SecurityServiceProvider extends AutoCloseable {
    
    public boolean isInitialized();

    public void configure() throws Exception;
    
    public Stage beginInitialize();

    public boolean isReady();

}