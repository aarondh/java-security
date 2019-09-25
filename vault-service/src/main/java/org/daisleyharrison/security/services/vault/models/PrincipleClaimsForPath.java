package org.daisleyharrison.security.services.vault.models;

import java.nio.file.Path;

import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.MalformedClaimException;

public class PrincipleClaimsForPath implements Principle {
    private PrincipleClaims claims;
    private Policy policy;
    private Path path;

    public PrincipleClaimsForPath(PrincipleClaims claims, Path path, Policy policy) {
        this.claims = claims;
        this.path = path;
        this.policy = policy;
    }

    public String getId() {
        try {
            return this.claims.getJwtId();
        } catch (MalformedClaimException exception) {
            return null;
        }
    }

    /**
     * @return Policy return the policy
     */
    public Policy getPolicy() {
        return policy;
    }

    /**
     * @return Path return the path
     */
    public Path getPath() {
        return path;
    }

    @Override
    public boolean hasClaim(String claimName) {
        return this.claims.hasClaim(claimName);
    }

    @Override
    public <T> T getClaimValue(String claimName, Class<T> type, T defaultValue) {
        try {
            if(this.claims.hasClaim(claimName)) {
                return this.claims.getClaimValue(claimName, type);
            }
            return defaultValue;
        } catch (MalformedClaimException exception) {
            return defaultValue;
        }
    }

    @Override
    public boolean hasCapability(Capability capability) {
        if (this.policy == null) {
            return false;
        }
        return this.policy.hasCapability(capability);
    }

    @Override
    public String getName() {
        try {
            return this.claims.getStringClaimValue(PrincipleClaims.ReservedClaims.PREFERRED_USERNAME);
        } catch (MalformedClaimException exception) {
            try {
                return this.claims.getSubject();
            } catch (MalformedClaimException exception2) {
                throw new IllegalArgumentException("Invalid claims", exception2);
            }
        }
    }

    @Override
    public String toString() {
        return getId() + "=>" + this.policy + " @ " + this.path;
    }

    @Override
    public JwtClaims getClaims() {
        return this.claims;
    }
}