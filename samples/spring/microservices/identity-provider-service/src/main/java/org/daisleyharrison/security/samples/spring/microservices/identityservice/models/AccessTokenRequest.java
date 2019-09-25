package org.daisleyharrison.security.samples.spring.microservices.identityservice.models;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AccessTokenRequest {
    public static class GrantTypes {
        public static final String AUTHROIZATION_CODE = "authorization_code";
        public static final String PASSWORD = "password";
        public static final String CLIENT_CREDENTIALS = "client_credentials";
    }

    /**
     * The client makes a request to the token endpoint by sending the following
     * parameters using the "application/x-www-form-urlencoded" format per Appendix
     * B with a character encoding of UTF-8 in the HTTP request entity-body:
     * 
     * /** REQUIRED. Value MUST be set to "authorization_code".
     */
    @JsonProperty("grant_type")
    private String grantType;

    /**
     * REQUIRED. The authorization code received from the authorization server.
     */
    @JsonProperty("code")
    private String code;

    /**
     * REQUIRED, if the "redirect_uri" parameter was included in the authorization
     * request as described in Section 4.1.1, and their values MUST be identical.
     */
    @JsonProperty("redirect_uri")
    private String redirectUri;

    /**
     * REQUIRED, if the client is not authenticating with the authorization server
     * as described in Section 3.2.1.
     */
    @JsonProperty("client_id")
    private String clientId;

    /**

     */
    @JsonProperty("client_secret")
    private String clientSecret;

    /**
     * REQUIRED for grant_type == password. The resource owner username.
     */
    @JsonProperty("username")
    private String username;
    /**
     * REQUIRED for grant_type == password. The resource owner password.
     */
    @JsonProperty("password")
    private String password;
    /**
     * REQUIRED for grant_type == password. The request scope
     */
    @JsonProperty("scope")
    private String scope;
    /**
     * REQUIRED for grant_type == password. The request scope
     */
    @JsonProperty("confirmation")
    private String confirmation;

    /**
     * @return GrantType return the grantType
     */
    public String getGrantType() {
        return grantType;
    }

    /**
     * @param grantType the grantType to set
     */
    public void setGrantType(String grantType) {
        this.grantType = grantType;
    }

    /**
     * @return String return the code
     */
    public String getCode() {
        return code;
    }

    /**
     * @param code the code to set
     */
    public void setCode(String code) {
        this.code = code;
    }

    /**
     * @return String return the redirectUri
     */
    public String getRedirectUri() {
        return redirectUri;
    }

    /**
     * @param redirectUri the redirectUri to set
     */
    public void setRedirectUri(String redirectUri) {
        this.redirectUri = redirectUri;
    }

    /**
     * @return String return the clientId
     */
    public String getClientId() {
        return clientId;
    }

    /**
     * @param clientId the clientId to set
     */
    public void setClientId(String clientId) {
        this.clientId = clientId;
    }


    /**
     * @return String return the username
     */
    public String getUsername() {
        return username;
    }

    /**
     * @param username the username to set
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * @return String return the password
     */
    public String getPassword() {
        return password;
    }

    /**
     * @param password the password to set
     */
    public void setPassword(String password) {
        this.password = password;
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
     * @return String return the confirmation
     */
    public String getConfirmation() {
        return confirmation;
    }

    /**
     * @param confirmation the confirmation to set
     */
    public void setConfirmation(String confirmation) {
        this.confirmation = confirmation;
    }


    /**
     * @return String return the clientSecret
     */
    public String getClientSecret() {
        return clientSecret;
    }

    /**
     * @param clientSecret the clientSecret to set
     */
    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }

}