package org.daisleyharrison.security.samples.spring.microservices.weaknessservice;

import org.daisleyharrison.security.common.models.datastore.DatastoreCollection;
import org.daisleyharrison.security.common.spi.DatastoreServiceProvider;
import org.daisleyharrison.security.samples.spring.microservices.shared.security.EnableDHSecurity;
import org.daisleyharrison.security.samples.spring.microservices.weaknessservice.models.cwe.Weakness;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import io.jsondb.JsonDBTemplate;

@SpringBootApplication
@EnableAutoConfiguration(exclude = {org.springframework.boot.autoconfigure.gson.GsonAutoConfiguration.class})
@EnableDHSecurity
public class Application {

	@Autowired
	DatastoreServiceProvider datastoreService;

	@Bean
	DatastoreCollection<Weakness> weaknessCollection() throws Exception {
		return datastoreService.openCollection(Weakness.class, "name");
	}


	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

}
