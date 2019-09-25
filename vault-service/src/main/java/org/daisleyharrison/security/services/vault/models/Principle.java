package org.daisleyharrison.security.services.vault.models;

import org.jose4j.jwt.JwtClaims;

public interface Principle {
    public String getId();
    public boolean hasClaim(String claimName);
    public <T> T getClaimValue(String claimName, Class<T> type, T defaultValue);
    public boolean hasCapability(Capability capability);
    public String getName();
    public JwtClaims getClaims();
}