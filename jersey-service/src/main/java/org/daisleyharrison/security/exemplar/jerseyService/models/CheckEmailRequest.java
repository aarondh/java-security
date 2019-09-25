package org.daisleyharrison.security.samples.jerseyService.models;

import javax.json.bind.annotation.JsonbProperty;

public class CheckEmailRequest {

    @JsonbProperty("email")
    private String email;

    public String getEmail() {
        return this.email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}