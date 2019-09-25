package org.daisleyharrison.security.samples.spring.microservices.identityservice;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;


@Component
public class Bootstrapper implements CommandLineRunner {
    private static final Logger LOGGER = LoggerFactory.getLogger(Bootstrapper.class);

    public Bootstrapper() {
    }


    @Override
    public void run(String... strings) throws Exception {

    }
}