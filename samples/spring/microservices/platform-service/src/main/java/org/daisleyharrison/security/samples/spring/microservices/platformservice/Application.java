package org.daisleyharrison.security.samples.spring.microservices.platformservice;

import org.daisleyharrison.security.common.models.datastore.DatastoreCollection;
import org.daisleyharrison.security.common.spi.DatastoreServiceProvider;
import org.daisleyharrison.security.samples.spring.microservices.platformservice.models.cpe.Platform;
import org.daisleyharrison.security.samples.spring.microservices.shared.security.EnableDHSecurity;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@EnableAutoConfiguration(exclude = {org.springframework.boot.autoconfigure.gson.GsonAutoConfiguration.class})
@SpringBootApplication
@EnableDHSecurity
public class Application {

	@Autowired
	DatastoreServiceProvider datastoreService;

	@Bean
	DatastoreCollection<Platform> platformCollection() throws Exception {
		return datastoreService.openCollection(Platform.class, "name");
	}

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

}
