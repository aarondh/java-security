package org.daisleyharrison.security.common.spi;

import javax.management.ServiceNotFoundException;

public interface ServiceProvider {
    public <T> T provideService(Class<T> clazz) throws ServiceNotFoundException;
}