package org.daisleyharrison.security.spring;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Autowired;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import javax.management.ServiceNotFoundException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.daisleyharrison.security.common.spi.ConfigurationServiceProvider;
import org.daisleyharrison.security.common.spi.SecurityServiceProvider;
import org.daisleyharrison.security.common.models.Stage;

public class SecurityServiceFactory<T extends SecurityServiceProvider, I extends T> implements FactoryBean<T> {
    private static final Logger LOGGER = LoggerFactory.getLogger(SecurityServiceFactory.class);

    private Class<T> beanType;
    private Class<I> implementationType;

    public SecurityServiceFactory(Class<T> beanType, Class<I> implmentationType) {
        this.beanType = beanType;
        this.implementationType = implmentationType;
    }

    private Method getSingletonMethod() {
        try {
            Method method = implementationType.getMethod("getInstance");
            if (method != null && Modifier.isStatic(method.getModifiers())) {
                return method;
            }
        } catch (NoSuchMethodException ex) {
        }
        return null;
    }

    private T getSingleton() throws IllegalAccessException, InvocationTargetException {
        Method getInstanceMethod = getSingletonMethod();
        if (getInstanceMethod != null) {
            return implementationType.cast(getInstanceMethod.invoke(implementationType, new Object[0]));
        }
        return null;
    }

    @Override
    public T getObject() throws Exception {
        T service = getSingleton();
        if (service == null) {
            service = implementationType.getDeclaredConstructor().newInstance();
        }
        LOGGER.info("SecurityServiceFactory: Got {} for bean {}", service.getClass().getName(),
                beanType.getSimpleName());
        if (!service.isInitialized()) {
            try (Stage stage = service.beginInitialize()) {
                service.configure();
            } catch (Exception exception) {
                LOGGER.error("{} bean failed to initialize", beanType.getName(), exception);
                throw new ServiceNotFoundException(
                        beanType.getName() + " bean failed to initialize: " + exception.getMessage());
            }
            LOGGER.info("{} bean initialized.", beanType.getName());
        }
        return service;
    }

    @Override
    public Class<?> getObjectType() {
        return beanType;
    }

    @Override
    public boolean isSingleton() {
        return getSingletonMethod() != null;
    }
}