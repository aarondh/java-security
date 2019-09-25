package org.daisleyharrison.security.samples.spring.web.config;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import java.io.InputStream;

import javax.management.ServiceNotFoundException;

import org.daisleyharrison.security.common.models.Stage;
import org.daisleyharrison.security.common.serviceProvider.LibraryServiceProvider;
import org.daisleyharrison.security.common.spi.ConfigurationServiceProvider;
import org.daisleyharrison.security.common.spi.DatastoreServiceProvider;
import org.daisleyharrison.security.common.spi.KeyServiceProvider;
import org.daisleyharrison.security.common.spi.ServiceProvider;
import org.daisleyharrison.security.common.spi.TokenizerServiceProvider;
import org.daisleyharrison.security.services.configuration.ConfigurationService;
import org.daisleyharrison.security.services.datastore.mongodb.DatastoreServiceMongoDb;
import org.daisleyharrison.security.services.key.KeyService;
import org.daisleyharrison.security.services.tokenizer.TokenizerService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;

@Configuration
public class DsoSecurityConfig implements ServiceProvider {
    private static final Logger LOGGER = LoggerFactory.getLogger(DsoSecurityConfig.class);
    private ApplicationContext context;

    @Value("classpath:dsoSecurity.yaml")
    Resource dsoSecurityResource;

    public DsoSecurityConfig(ApplicationContext context) {
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

    @Bean
    @Scope("singleton")
    public TokenizerServiceProvider tokenizerService() {
        TokenizerServiceProvider tokenizerService = TokenizerService.getInstance();
        if (!tokenizerService.isInitialized()) {
            try (Stage stage = tokenizerService.beginInitialize()) {
                tokenizerService.configure();
            } catch (Exception exception) {
                LOGGER.error("Error initializing tokenizer-service", exception);
            }
        }
        return tokenizerService;
    }

    @Bean
    @Scope("singleton")
    public ConfigurationServiceProvider configurationService() {
        ConfigurationServiceProvider configurationService = ConfigurationService.getInstance();
        if (!configurationService.isInitialized()) {
            try (Stage stage = configurationService.beginInitialize()) {
                try (InputStream inputStream = dsoSecurityResource.getInputStream()) {
                    configurationService.setSource(inputStream);
                    configurationService.configure();
                }
            } catch (Exception exception) {
                LOGGER.error("Error initializing configuration-service", exception);
            }
        }
        return configurationService;
    }

    @Bean
    @Scope("singleton")
    public KeyServiceProvider keyService() {
        KeyServiceProvider keyService = KeyService.getInstance();
        if (!keyService.isInitialized()) {
            try (Stage stage = keyService.beginInitialize()) {
                keyService.configure();
            } catch (Exception exception) {
                LOGGER.error("Error initializing key-service", exception);
            }
        }
        return keyService;
    }

    @Bean
    @Scope("singleton")
    public DatastoreServiceProvider datastoreService() {
        DatastoreServiceProvider datastoreService = DatastoreServiceMongoDb.getInstance();
        if (!datastoreService.isInitialized()) {
            try (Stage stage = datastoreService.beginInitialize()) {
                datastoreService.configure();
            } catch (Exception exception) {
                LOGGER.error("Error initializing datastore-service", exception);
            }
        }
        return datastoreService;
    }

}