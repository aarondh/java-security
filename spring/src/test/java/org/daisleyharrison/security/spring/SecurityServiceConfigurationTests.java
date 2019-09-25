package org.daisleyharrison.security.spring;

import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.assertNotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import org.daisleyharrison.security.common.spi.ConfigurationServiceProvider;
import org.daisleyharrison.security.common.spi.DatastoreServiceProvider;
import org.daisleyharrison.security.common.spi.KeyServiceProvider;
import org.daisleyharrison.security.common.spi.TokenizerServiceProvider;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { SecurityServiceConfiguration.class })
@PropertySources({ @PropertySource(value = "classpath:application.yml", ignoreResourceNotFound = true),
		@PropertySource(value = "classpath:application.properties", ignoreResourceNotFound = true) })
public class SecurityServiceConfigurationTests {

	@Autowired
	ConfigurationServiceProvider configurationService;

	@Autowired
	KeyServiceProvider keyService;

	@Autowired
	TokenizerServiceProvider tokenizerService;

	@Autowired
	DatastoreServiceProvider datastoreService;

	@Test
	public void configurationServiceBeanLoads() throws Exception {
		assertNotNull(configurationService);
	}

	@Test
	public void keyServiceBeanLoads() throws Exception {
		assertNotNull(keyService);
	}

	@Test
	public void tokenizerServiceBeanLoads() throws Exception {
		assertNotNull(tokenizerService);
	}

	@Test
	public void datastoreServiceBeanLoads() throws Exception {
		assertNotNull(datastoreService);
	}

}
