package org.daisleyharrison.security.samples.spring.microservices.identityservice.models;

public interface AuthenticationClientProvider {
    public AuthenticationClient find(String clientId);
    public AuthenticationClient authenticate(String clientId, String token) throws SecurityException;
    }