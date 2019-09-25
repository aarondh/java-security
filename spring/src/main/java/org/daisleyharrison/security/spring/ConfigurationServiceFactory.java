package org.daisleyharrison.security.spring;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import java.io.InputStream;
import java.security.SignatureException;

import javax.management.ServiceNotFoundException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.daisleyharrison.security.common.spi.ConfigurationServiceProvider;
import org.daisleyharrison.security.common.models.Stage;
import org.daisleyharrison.security.services.configuration.ConfigurationService;

public class ConfigurationServiceFactory implements FactoryBean<ConfigurationServiceProvider> {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigurationServiceFactory.class);

    @Value("${configuration-service.path:classpath:application.yml}")
    private String configurationServicePath;

    @Autowired
    private ResourceLoader resourceLoader;

    @Override
    public ConfigurationServiceProvider getObject() throws Exception {
        ConfigurationServiceProvider service = ConfigurationService.getInstance();
        if (!service.isInitialized()) {
            Resource configurationServicePathResource = resourceLoader.getResource(configurationServicePath);
            try (InputStream inputStream = configurationServicePathResource.getInputStream()) {
                try (Stage stage = service.beginInitialize()) {
                    service.setSource(inputStream);
                    service.configure();
                }
                LOGGER.info("configuration-service initialized using configuration path: {}", configurationServicePath);
            } catch (SignatureException exception) {
                LOGGER.error("configuration-service bean configuration file has an invalid signature.", exception);
                throw new ServiceNotFoundException(
                        "configuration-service bean configuration file has an invalid signature.");
            } catch (Exception exception) {
                LOGGER.error("configiration-service bean failed to initialize", exception);
                throw new ServiceNotFoundException("configiration-service bean failed to initialize: " + exception.getMessage());
            }
        }
        return service;
    }

    @Override
    public Class<?> getObjectType() {
        return ConfigurationServiceProvider.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }
}