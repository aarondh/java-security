package org.daisleyharrison.security.samples.jerseyService.models;

import javax.json.bind.annotation.JsonbProperty;

public class OpenIdLoginRequest {

    private String email;
   /**
     * The uri the user will be redirected to after a successful login
     */
    private String redirectUri;
    /** 
     * The OpenId issuer
     */
    private String issuer;

    @JsonbProperty("email")
    public String getEmail() {
        return this.email;
    }
    public void setEmail(String email) {
        this.email = email;
    }

    @JsonbProperty("issuer")
    public String getIssuer() {
        return this.issuer;
    }
    public void setIssuer(String issuer) {
        this.issuer = issuer;
    }


    @JsonbProperty("redirectUri")
    public String getRedirectUri() {
        return this.redirectUri;
    }
    public void setRedirectUri(String redirectUri) {
        this.redirectUri = redirectUri;
    }
}