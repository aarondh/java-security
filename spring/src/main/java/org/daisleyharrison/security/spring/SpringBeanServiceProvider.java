package org.daisleyharrison.security.spring;

import javax.management.ServiceNotFoundException;

import org.daisleyharrison.security.common.serviceProvider.LibraryServiceProvider;
import org.daisleyharrison.security.common.spi.ServiceProvider;

import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;

public class SpringBeanServiceProvider implements ServiceProvider {
    private ApplicationContext context;
    public SpringBeanServiceProvider( ApplicationContext context) {
        this.context = context;
        LibraryServiceProvider.getInstance().registerServiceProvider(this);
    }

    @Override
    public <T> T provideService(Class<T> clazz) throws ServiceNotFoundException {
        try {
            return context.getBean(clazz);
        } catch (NoSuchBeanDefinitionException ex) {
            throw new ServiceNotFoundException(ex.getLocalizedMessage());
        }
    }   
}