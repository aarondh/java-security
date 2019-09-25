package org.daisleyharrison.security.samples.spring.microservices.shared.security;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

public class JweAuthenticationEntryPoint implements AuthenticationEntryPoint {

	@Override
	public void commence(HttpServletRequest request, HttpServletResponse response,
			AuthenticationException authException) throws IOException, ServletException {
		// log.debug("Jwe authentication failed:" + authException);

		response.sendError(HttpServletResponse.SC_UNAUTHORIZED	, "Jwe authentication failed");

	}

}