package org.daisleyharrison.security.spring;

import java.security.NoSuchAlgorithmException;

import javax.management.ServiceNotFoundException;

import org.daisleyharrison.security.common.spi.PopTokenConsumer;
import org.daisleyharrison.security.common.spi.PopTokenProducer;
import org.daisleyharrison.security.services.poptoken.PopTokenFactory;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SecurityServiceConfiguration {

    private PopTokenFactory popTokenFactory;

    public SecurityServiceConfiguration(ApplicationContext context)
             {
        new SpringBeanServiceProvider(context);
    }
    private PopTokenFactory getPopTokenFactory() throws NoSuchAlgorithmException, ServiceNotFoundException {
        if(popTokenFactory == null){
            popTokenFactory = new PopTokenFactory();
        }
        return popTokenFactory;
    }

    @Bean
    public ConfigurationServiceFactory configurationServiceFactory() {
        return new ConfigurationServiceFactory();
    }

    @Bean
    public KeyServiceFactory keyServiceFactory() throws Exception {
        return new KeyServiceFactory();
    }

    @Bean
    public CypherServiceFactory cypherServiceFactory() throws Exception {
        return new CypherServiceFactory();
    }

    @Bean
    public TokenizerServiceFactory tokenizerServiceFactory() throws Exception {
        return new TokenizerServiceFactory();
    }

    @Bean
    public DatastoreServiceFactory datastoreServiceFactory() throws Exception {
        return new DatastoreServiceFactory();
    }

    @Bean
    public PopTokenConsumer popTokenConsumer()  throws Exception {
        return getPopTokenFactory().getConsumer();
    }

    @Bean
    public PopTokenProducer popTokenProducer()  throws Exception {
        return getPopTokenFactory().getProducer();
    }
}