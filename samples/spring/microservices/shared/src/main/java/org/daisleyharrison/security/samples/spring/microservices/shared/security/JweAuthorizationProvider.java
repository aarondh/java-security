package org.daisleyharrison.security.samples.spring.microservices.shared.security;

import org.daisleyharrison.security.common.exceptions.MalformedAuthClaimException;
import org.daisleyharrison.security.common.models.authorization.AuthClaims;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

@Component
public class JweAuthorizationProvider {

    @Autowired
    private UserDetailsService userDetailsService;

    public Authentication getAuthentication(AuthClaims claims) throws MalformedAuthClaimException {
        UserDetails userDetails = this.userDetailsService.loadUserByUsername(claims.getSubject());
        return new JwtAuthenticationToken<UserDetails>(claims, userDetails, claims.getSubject());
    }
}