package org.daisleyharrison.security.samples.spring.web.models;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AuthorizationResponse {
    @JsonProperty("state")
    private String state;
    @JsonProperty("code")
    private String code;
    @JsonProperty("nonce")
    private String nonce;

    public AuthorizationResponse() {
    }

    /**
     * @return String return the state
     */
    public String getState() {
        return state;
    }

    /**
     * @param state the state to set
     */
    public void setState(String state) {
        this.state = state;
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
     * @return String return the nonce
     */
    public String getNonce() {
        return nonce;
    }

    /**
     * @param nonce the nonce to set
     */
    public void setNonce(String nonce) {
        this.nonce = nonce;
    }
}