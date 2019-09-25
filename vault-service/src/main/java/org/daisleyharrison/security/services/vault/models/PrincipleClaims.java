package org.daisleyharrison.security.services.vault.models;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.MalformedClaimException;
import org.jose4j.jwt.consumer.InvalidJwtException;

public class PrincipleClaims extends JwtClaims implements Principle {
    public class PrivateClaims {
        public static final String POLICY = "pol";
        public static final String PASSWORD = "pass";
    }

    public class ReservedClaims {
        public static final String PREFERRED_USERNAME = "preferred_username";
    }

    public PrincipleClaims() {
        setIssuedAtToNow();
        setNotBeforeMinutesInThePast(2);
    }

    private PrincipleClaims(JwtClaims claims) {
        setClaimMap(claims.getClaimsMap());
    }

    public String getId() {
        try {
            return getJwtId();
        } catch (MalformedClaimException exception) {
            return null;
        }
    }

    public List<String> getPolicies() throws MalformedClaimException {
        return getStringListClaimValue(PrivateClaims.POLICY);
    }

    public void setPolicies(List<String> policies) {
        setClaim(PrivateClaims.POLICY, policies);
    }

    public void setPolicies(String... policies) {
        setClaim(PrivateClaims.POLICY, Arrays.asList(policies));
    }

    public void addPolicy(String policy) throws MalformedClaimException {
        List<String> policies;
        if (hasPolicies()) {
            policies = getPolicies();
        } else {
            policies = new ArrayList<>();
        }
        policies.add(policy);
        setPolicies(policies);
    }

    public boolean hasPolicies() {
        return hasClaim(PrivateClaims.POLICY);
    }

    public boolean hasPassword(char[] password) throws MalformedClaimException {
        String value = getStringClaimValue(PrivateClaims.PASSWORD);
        if (value == null) {
            return password == null;
        } else {
            return Arrays.equals(value.toCharArray(), password);
        }
    }

    public void setPassword(char[] password) {
        setClaim(PrivateClaims.PASSWORD, new String(password));
    }

    private void setClaimMap(Map<String, Object> map) {
        map.entrySet().forEach(entry -> {
            setClaim(entry.getKey(), entry.getValue());
        });
    }

    public static PrincipleClaims parse(String content) throws InvalidJwtException {
        return new PrincipleClaims(JwtClaims.parse(content));
    }

    @Override
    public <T> T getClaimValue(String claimName, Class<T> type, T defaultValue) {
        try {
            if (hasClaim(claimName)) {
                return getClaimValue(claimName, type);
            }
            return defaultValue;
        } catch (MalformedClaimException exception) {
            return defaultValue;
        }
    }

    @Override
    public boolean hasCapability(Capability capability) {
        return false;
    }

    @Override
    public String getName() {
        try {
            return getStringClaimValue(PrincipleClaims.ReservedClaims.PREFERRED_USERNAME);
        } catch (MalformedClaimException exception) {
            try {
                return getSubject();
            } catch (MalformedClaimException exception2) {
                throw new IllegalArgumentException("Invalid claims", exception2);
            }
        }
    }

    @Override
    public JwtClaims getClaims() {
        return this;
    }

}