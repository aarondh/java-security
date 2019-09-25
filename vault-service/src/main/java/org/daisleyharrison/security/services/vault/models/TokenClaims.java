package org.daisleyharrison.security.services.vault.models;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.daisleyharrison.security.common.models.authorization.OpenIdClaims.ReservedClaims;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.MalformedClaimException;
import org.jose4j.jwt.NumericDate;
import org.jose4j.jwt.consumer.InvalidJwtException;

public class TokenClaims extends JwtClaims implements Token {
    private static final String OPAQUE_TOKEN_PREFIX = "t.";

    public class PrivateClaims {
        public static final String USES = "uses";
        public static final String RENEWABLE = "ren";
        public static final String SCOPE = "scope";
    }

    public TokenClaims() {
        setIssuedAtToNow();
        setNotBeforeMinutesInThePast(2);
    }

    public TokenClaims(JwtClaims claims) {
        setClaimMap(claims.getClaimsMap());
    }

    private void setClaimMap(Map<String, Object> map) {
        map.entrySet().forEach(entry -> {
            setClaim(entry.getKey(), entry.getValue());
        });
    }

    public boolean isRenewable() {
        try {
            return getClaimValue(PrivateClaims.RENEWABLE, Boolean.class);
        } catch (MalformedClaimException exception) {
            return false;
        }
    }

    public void setRenewable(boolean renewable) {
        setClaim(PrivateClaims.RENEWABLE, renewable);
    }

    public boolean hasUses() {
        return hasClaim(PrivateClaims.USES);
    }

    private int getIntegerClaimValue(String claimName) throws MalformedClaimException {
        Number value = getClaimValue(claimName, Number.class);
        return value.intValue();
    }

    public int getUses() throws MalformedClaimException {
        return getIntegerClaimValue(PrivateClaims.USES);
    }

    public void setUses(int uses) {
        setClaim(PrivateClaims.USES, uses);
    }

    public boolean hasScope() {
        return hasClaim(ReservedClaims.SCOPE);
    }

    public List<String> getScope() throws MalformedClaimException {
        return getStringListClaimValue(ReservedClaims.SCOPE);
    }

    public void setScope(List<String> scope) {
        setClaim(ReservedClaims.SCOPE, scope);
    }

    public void setScope(String... scope) {
        setClaim(ReservedClaims.SCOPE, Arrays.asList(scope));
    }

    public void addScope(String scope) throws MalformedClaimException {
        List<String> scopes;
        if (hasScope()) {
            scopes = getScope();
        } else {
            scopes = new ArrayList<>();
        }
        scopes.add(scope);
        setScope(scopes);
    }

    public String toOpaqueToken() throws MalformedClaimException {
        return OPAQUE_TOKEN_PREFIX + getJwtId();
    }

    public static boolean isOpaqueToken(String token) {
        return token == null ? false : token.startsWith(OPAQUE_TOKEN_PREFIX);
    }

    public boolean isExpired() throws MalformedClaimException {
        NumericDate now = NumericDate.now();

        if (now.isAfter(getExpirationTime())) {
            return true;
        }

        if (now.isBefore(getNotBefore())) {
            return true;
        }

        if (hasUses()) {
            if (getUses() <= 0) {
                return true;
            }
        }

        return false;
    }

    public static TokenClaims parse(String content) throws InvalidJwtException {
        return new TokenClaims(JwtClaims.parse(content));
    }

}
