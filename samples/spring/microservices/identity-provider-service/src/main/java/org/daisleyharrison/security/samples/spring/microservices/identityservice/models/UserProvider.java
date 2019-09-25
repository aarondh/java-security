package org.daisleyharrison.security.samples.spring.microservices.identityservice.models;

import org.daisleyharrison.security.common.models.authorization.OpenIdClaims;

public interface UserProvider {
    OpenIdClaims find(String userId);
    OpenIdClaims authenticate(String userId, String token) throws SecurityException;
}