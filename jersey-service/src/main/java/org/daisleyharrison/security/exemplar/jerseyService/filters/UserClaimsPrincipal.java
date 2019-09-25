package org.daisleyharrison.security.samples.jerseyService.filters;

import java.security.Principal;

import org.daisleyharrison.security.common.models.authorization.AuthClaims;

public interface UserClaimsPrincipal extends Principal {
    public AuthClaims getUserClaims();
}