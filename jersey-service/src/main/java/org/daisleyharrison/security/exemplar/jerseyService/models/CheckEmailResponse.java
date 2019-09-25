package org.daisleyharrison.security.samples.jerseyService.models;

import javax.json.bind.annotation.JsonbProperty;

public class CheckEmailResponse {
    public CheckEmailResponse() {

    }

    public CheckEmailResponse(String issuer) {
        this.issuer = issuer;
    }

    @JsonbProperty("issuer")
    private String issuer;

    public String getIssuer() {
        return this.issuer;
    }

    public void setIssuer(String issuer) {
        this.issuer = issuer;
    }
}