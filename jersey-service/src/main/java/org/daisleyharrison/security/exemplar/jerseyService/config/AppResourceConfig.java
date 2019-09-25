package org.daisleyharrison.security.samples.jerseyService.config;

import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.management.ServiceNotFoundException;
import javax.ws.rs.ApplicationPath;

import org.daisleyharrison.security.samples.jerseyService.Main;
import org.daisleyharrison.security.samples.jerseyService.filters.CORSResponseFilter;
import org.glassfish.jersey.logging.LoggingFeature;
import org.glassfish.jersey.server.ResourceConfig;

@ApplicationPath("resources")
public class AppResourceConfig extends ResourceConfig {
	public AppResourceConfig() {
		try {
			Set<String> packageNames = Main.getConfigurationService().getNames("server.packages");
			packages(packageNames.toArray(String[]::new));
		} catch (ServiceNotFoundException exception) {

		}
		register(CORSResponseFilter.class);
		// register(CorsFilter.class);
		register(new LoggingFeature(Logger.getLogger(LoggingFeature.DEFAULT_LOGGER_NAME), Level.FINEST,
				LoggingFeature.Verbosity.PAYLOAD_ANY, 10000));
	}
}