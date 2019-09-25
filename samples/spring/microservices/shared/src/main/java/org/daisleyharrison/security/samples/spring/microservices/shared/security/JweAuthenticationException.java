package org.daisleyharrison.security.samples.spring.microservices.shared.security;

import javax.servlet.ServletException;

import org.springframework.security.core.AuthenticationException;

public class JweAuthenticationException extends AuthenticationException {
    private static final long serialVersionUID = -2313327362890805963L;

    public JweAuthenticationException(String message) {
        super(message);
    }
    public JweAuthenticationException(String message, Throwable throwable) {
        super(message, throwable);
    }
}