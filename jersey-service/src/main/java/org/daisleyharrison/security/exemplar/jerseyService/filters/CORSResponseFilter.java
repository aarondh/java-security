package org.daisleyharrison.security.samples.jerseyService.filters;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import java.util.Map;

import javax.management.ServiceNotFoundException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.Provider;

import org.daisleyharrison.security.common.spi.ConfigurationServiceProvider;
import org.daisleyharrison.security.samples.jerseyService.Main;

@Provider
public class CORSResponseFilter implements ContainerResponseFilter {
	private static final String HEADER_ACCESS_CONTROL_ALLOW_ORIGIN = "Access-Control-Allow-Origin";
	private String defaultOrigin;
	private Map<String, String> headers = new HashMap<>();
	private Set<String> allowedOrigins = new HashSet<>();

	public CORSResponseFilter() {
		try {
			ConfigurationServiceProvider config = Main.getConfigurationService();
			this.defaultOrigin = config.getValue("CORSResponseFilter.origin", Main.getBaseUri().toString());
			Set<String> headers = config.getNames("CORSResponseFilter.headers");
			headers.forEach(header -> {
				String value = config.getValue("CORSResponseFilter.headers." + header, null);
				if (value != null) {
					this.headers.put(header, value);
				}
			});
			String allowedOrigins = this.headers.get(HEADER_ACCESS_CONTROL_ALLOW_ORIGIN);
			if (allowedOrigins != null) {
				String[] origins = allowedOrigins.split("\\s*,\\s*");
				Arrays.stream(origins).forEach(origin -> {
					this.allowedOrigins.add(origin);
				});
			}
		} catch (ServiceNotFoundException exception) {

		}
	}

	public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext)
			throws IOException {

		String origin = requestContext.getHeaderString("Origin");
		if (origin == null) {
			origin = this.defaultOrigin;
		}
		if (allowedOrigins.contains(origin)) {
			final String allowedOrigin = origin;
			MultivaluedMap<String, Object> responseHeaders = responseContext.getHeaders();
			this.headers.forEach((name, value) -> {
				if (name.equals(HEADER_ACCESS_CONTROL_ALLOW_ORIGIN)) {
					responseHeaders.add(name, allowedOrigin );
				} else {
					responseHeaders.add(name, value);
				}
			});
		}
	}

}