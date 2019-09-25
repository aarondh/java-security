package org.daisleyharrison.security.common.serviceProvider;

import java.util.Map;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import javax.management.ServiceNotFoundException;

import org.daisleyharrison.security.common.spi.ServiceProvider;

import org.apache.xbean.finder.ResourceFinder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class XBeanServiceProvider implements ServiceProvider {
    private static final Logger LOGGER = LoggerFactory.getLogger(XBeanServiceProvider.class);
    private ResourceFinder finder;
    @SuppressWarnings("rawtypes")
    private Map<Class<?>, Class> serviceMap;

    public XBeanServiceProvider() {
        finder = new ResourceFinder("META-INF/services/");
        serviceMap = new HashMap<>();
    }

    @Override
    @SuppressWarnings("rawtypes")
    public <T> T provideService(Class<T> clazz) throws ServiceNotFoundException {
        try {
            Class implementation = serviceMap.get(clazz);
            if (implementation == null) {
                List<Class> implementations = finder.findAllImplementations(clazz);
                if (implementations.isEmpty()) {
                    throw new ServiceNotFoundException("service implementing " + clazz.getName() + " was not found");
                } else {
                    implementation = implementations.get(0);
                    serviceMap.put(clazz, implementation);
                }
            }

            try {
                Method method = implementation.getMethod("getInstance");
                if (Modifier.isStatic(method.getModifiers())) {
                    return clazz.cast(method.invoke(implementation, new Object[0]));
                } else {
                    LOGGER.error("Unable to load {} singlton service. getInstance() method is not static",
                            clazz.getName());
                }
                // is a singleton
            } catch (NoSuchMethodException exception) {
                // not a singleton
            }

            return clazz.cast(implementation.getDeclaredConstructor().newInstance());

        } catch (NoSuchMethodException exception) {
            LOGGER.error("Unable to load {} service. {}", clazz.getName(), exception.getMessage());
        } catch (SecurityException exception) {
            LOGGER.error("Unable to load {} service. {}", clazz.getName(), exception.getMessage());
        } catch (InvocationTargetException exception) {
            LOGGER.error("Unable to load {} service. {}", clazz.getName(), exception.getMessage());
        } catch (InstantiationException exception) {
            LOGGER.error("Unable to load {} service. {}", clazz.getName(), exception.getMessage());
        } catch (IllegalAccessException exception) {
            LOGGER.error("Unable to load {} service. {}", clazz.getName(), exception.getMessage());
        } catch (IOException exception) {
            LOGGER.error("Unable to load {} service. {}", clazz.getName(), exception.getMessage());
        } catch (ClassNotFoundException exception) {
            LOGGER.error("Unable to load {} service. {}", clazz.getName(), exception.getMessage());
        }
        throw new ServiceNotFoundException("service implementing " + clazz.getName() + " was not found");
    }
}