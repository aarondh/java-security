package org.daisleyharrison.security.common.utilities;

import java.util.Map;

import org.daisleyharrison.security.common.exceptions.MalformedAuthClaimException;
import org.daisleyharrison.security.common.models.authorization.AuthClaims;
import org.daisleyharrison.security.common.models.authorization.OpenIdClaims;

public class AuthClaimsImpl extends OpenIdClaimsImpl implements AuthClaims {
    public AuthClaimsImpl() {
        super();
    }

    public AuthClaimsImpl(Map<String,Object> claimsMap) {
        super(claimsMap);
    }

    public String getInitialUri() throws MalformedAuthClaimException {
        return getStringClaimValue(PrivateClaims.INITIAL_URI);
    }

    public void setInitialUri(String initualUri) {
        setClaim(PrivateClaims.INITIAL_URI, initualUri);
    }

    public String getIdTokenIssuer() throws MalformedAuthClaimException {
        return getStringClaimValue(PrivateClaims.ID_TOKEN_ISSUER);
    }

    public void setIdTokenIssuer(String id_token_iss) {
        setClaim(PrivateClaims.ID_TOKEN_ISSUER, id_token_iss);
    }

    public OpenIdClaims getIdToken() throws MalformedAuthClaimException {
        return getClaimValue(PrivateClaims.ID_TOKEN, OpenIdClaims.class);
    }

    public void setIdToken(OpenIdClaims id_token) {
        setClaim(PrivateClaims.ID_TOKEN, id_token);
    }

    public static AuthClaims parse(String jsonClaims) throws MalformedAuthClaimException {
        return JwtClaimsImpl.parse(jsonClaims, AuthClaimsImpl.class);
    }

}