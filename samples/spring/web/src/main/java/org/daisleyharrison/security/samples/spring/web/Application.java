package org.daisleyharrison.security.samples.spring.web;

import org.daisleyharrison.security.common.spi.PopTokenProducer;
import org.daisleyharrison.security.samples.spring.web.beans.MicroserviceAuthenticator;
import org.daisleyharrison.security.samples.spring.webtalker.MicroserviceUrlProvider;
import org.daisleyharrison.security.samples.spring.webtalker.TokenManager;
import org.daisleyharrison.security.samples.spring.webtalker.TokenManagerImpl;
import org.daisleyharrison.security.services.poptoken.PopTokenFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

import java.security.NoSuchAlgorithmException;
import java.util.Optional;

import javax.management.ServiceNotFoundException;

import com.netflix.appinfo.InstanceInfo;
import com.netflix.discovery.EurekaClient;


@SpringBootApplication
@ComponentScan(basePackages = "org.daisleyharrison.security.samples.spring")
public class Application extends SpringBootServletInitializer {

    @Autowired
    private EurekaClient discoveryClient;

    @Autowired
    private MicroserviceAuthenticator authenticator;

    @Bean
    public TokenManager tokenManager() {
        return new TokenManagerImpl();
    }

    @Bean
    public PopTokenProducer popTokenProducer() throws NoSuchAlgorithmException, ServiceNotFoundException {
        return new PopTokenFactory().getProducer();
    }

    @Bean
    public MicroserviceUrlProvider microserviceUrlProvider() {
        return new MicroserviceUrlProvider(){
        
            @Override
            public Optional<String> lookupMicroservice(String microserviceName, boolean secure) {
                InstanceInfo microserviceHost = discoveryClient.getNextServerFromEureka(microserviceName, secure);
                return microserviceHost == null ? Optional.empty() : Optional.of( microserviceHost.getHomePageUrl());
            }
        };
    }

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(Application.class);
    }

    public static void main(String[] args) throws Exception {
        SpringApplication.run(Application.class, args);
    }
    
}