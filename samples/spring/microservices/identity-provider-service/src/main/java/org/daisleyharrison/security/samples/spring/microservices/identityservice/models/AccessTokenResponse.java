package org.daisleyharrison.security.samples.spring.microservices.identityservice.models;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * RE
 */
public class AccessTokenResponse {
    /**
     * REQUIRED.  The access token issued by the authorization server.
     */
    @JsonProperty("access_token")
    private String accessToken;
    /**
     * REQUIRED for OpenId.  The id token issued by the authorization server.
     */
    @JsonProperty("id_token")
    private String idToken;
    /**
     * REQUIRED.  The type of the token issued as described in
     * Section 7.1.  Value is case insensitive.
     */
    @JsonProperty("token_type")
    private String tokenType;
    /**
     * RECOMMENDED.  The lifetime in seconds of the access token.  For
     * example, the value "3600" denotes that the access token will
     * expire in one hour from the time the response was generated.
     * If omitted, the authorization server SHOULD provide the
     * expiration time via other means or document the default value.
     */
    @JsonProperty("expires_in")
    private String expiresIn;
    /**
     * OPTIONAL.  The refresh token, which can be used to obtain new
     * access tokens using the same authorization grant as described
     * in Section 6.
     */
    @JsonProperty("refresh_token")
    private String refreshToken;
    /**
     * OPTIONAL, if identical to the scope requested by the client;
     * otherwise, REQUIRED.  The scope of the access token as
     * described by Section 3.3.
     */
    @JsonProperty("scope")
    private String scope;

    public AccessTokenResponse(){}

    /**
     * @return String return the accessToken
     */
    public String getAccessToken() {
        return accessToken;
    }

    /**
     * @param accessToken the accessToken to set
     */
    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    /**
     * @return String return the tokenType
     */
    public String getTokenType() {
        return tokenType;
    }

    /**
     * @param tokenType the tokenType to set
     */
    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    /**
     * @return String return the expiresIn
     */
    public String getExpiresIn() {
        return expiresIn;
    }

    /**
     * @param expiresIn the expiresIn to set
     */
    public void setExpiresIn(String expiresIn) {
        this.expiresIn = expiresIn;
    }

    /**
     * @return String return the refreshToken
     */
    public String getRefreshToken() {
        return refreshToken;
    }

    /**
     * @param refreshToken the refreshToken to set
     */
    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    /**
     * @return String return the scope
     */
    public String getScope() {
        return scope;
    }

    /**
     * @param scope the scope to set
     */
    public void setScope(String scope) {
        this.scope = scope;
    }


    /**
     * @return String return the idToken
     */
    public String getIdToken() {
        return idToken;
    }

    /**
     * @param idToken the idToken to set
     */
    public void setIdToken(String idToken) {
        this.idToken = idToken;
    }

}