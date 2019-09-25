package org.daisleyharrison.security.common.models.authorization;

import org.daisleyharrison.security.common.exceptions.MalformedAuthClaimException;

public interface AuthClaims extends OpenIdClaims {
    public class PrivateClaims {
        public static final String ID_TOKEN = "id_token";
        public static final String ID_TOKEN_ISSUER = "id_token_iss";
        public static final String INITIAL_URI = "initial_uri";
    }
    public String getInitialUri()  throws MalformedAuthClaimException;
    public void setInitialUri(String initualUri);
    public String getIdTokenIssuer() throws MalformedAuthClaimException;
    public void setIdTokenIssuer(String id_token_iss);
    public OpenIdClaims getIdToken() throws MalformedAuthClaimException;
    public void setIdToken(OpenIdClaims id_token);
    public String toJson();
}