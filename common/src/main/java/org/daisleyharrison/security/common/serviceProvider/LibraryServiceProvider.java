package org.daisleyharrison.security.common.serviceProvider;

import javax.management.ServiceNotFoundException;

import org.daisleyharrison.security.common.spi.ServiceProvider;

public class LibraryServiceProvider implements ServiceProvider {
    private static LibraryServiceProvider _instance;

    public static LibraryServiceProvider getInstance() {
        if (_instance == null) {
            _instance = new LibraryServiceProvider();
        }
        return _instance;
    }

    private ServiceProvider serviceProvider;

    protected LibraryServiceProvider() {
        this.serviceProvider = new XBeanServiceProvider();
    }

    public void registerServiceProvider(ServiceProvider serviceProvider) {
        this.serviceProvider = serviceProvider;
    }

    @Override
    public <T> T provideService(Class<T> clazz) throws ServiceNotFoundException {
        return serviceProvider.provideService(clazz);
    }
}