package org.daisleyharrison.security.samples.spring.microservices.identityservice.models;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ValidateRequest {

    public enum ReturnTypes {
        @JsonProperty("claims")
        CLAIMS,
        @JsonProperty("jws")
        JWS
        
    }
    @JsonProperty("client_id")
    private String clientId;
    @JsonProperty("secret")
    private String secret;
    @JsonProperty("return_type")
    private ReturnTypes returnType;
    @JsonProperty("token")
    private String token;

    public ValidateRequest() {}

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
     * @return String return the secret
     */
    public String getSecret() {
        return secret;
    }

    /**
     * @param secret the secret to set
     */
    public void setSecret(String secret) {
        this.secret = secret;
    }

    /**
     * @return ReturnTypes return the returnType
     */
    public ReturnTypes getReturnType() {
        return returnType;
    }

    /**
     * @param returnType the returnType to set
     */
    public void setReturnType(ReturnTypes returnType) {
        this.returnType = returnType;
    }

    /**
     * @return String return the token
     */
    public String getToken() {
        return token;
    }

    /**
     * @param token the token to set
     */
    public void setToken(String token) {
        this.token = token;
    }

}