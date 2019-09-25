package org.daisleyharrison.security.samples.spring.microservices.shared.security;

import java.util.ArrayList;
import java.util.Collection;

import org.daisleyharrison.security.common.exceptions.MalformedAuthClaimException;
import org.daisleyharrison.security.common.models.authorization.AuthClaims;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

public class JwtAuthenticationToken<U> extends AbstractAuthenticationToken {

    private static final long serialVersionUID = -1949976839306453197L;
    private AuthClaims authClaims;
    private U authenticatedUser;
    private String uid;

    public JwtAuthenticationToken(AuthClaims authClaims, U authenticatedUser, String uid) {
        super(getAuthorities(authClaims));
        this.authClaims = authClaims;
        this.uid = uid;
        this.authenticatedUser = authenticatedUser;
        this.setAuthenticated(true);
    }

    private static Collection<? extends GrantedAuthority> getAuthorities(AuthClaims authClaims) {
        ArrayList<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>();
        try {
            authClaims.getScopes().forEach(scope -> authorities.add(new SimpleGrantedAuthority(scope)));
        } catch (MalformedAuthClaimException e) {
            // an invalid claim here just results in no authorities
        }
        return authorities;
    }

    @Override
    public Object getCredentials() {
        return authClaims;
    }

    @Override
    public Object getPrincipal() {
        return authenticatedUser;
    }

    public AuthClaims getClaims() {
        return authClaims;
    }

    public String getUid() {
        return uid;
    }
    
}