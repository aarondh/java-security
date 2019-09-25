package org.daisleyharrison.security.samples.spring.microservices.identityservice.models;

import com.fasterxml.jackson.annotation.JsonProperty;

public class OAuth2ErrorResponse {
    @JsonProperty("error")
    private OAuth2Error error;
    @JsonProperty("error_description")
    private String errorDescription;
    @JsonProperty("error_uri")
    private String errorUri;
    @JsonProperty("state")
    private String state;

    public OAuth2ErrorResponse() {

    }

    public OAuth2ErrorResponse(OAuth2Error error, String errorDescription, String state, String errorUri) {
        this.error = error;
        this.errorDescription = errorDescription;
        this.state = state;
        this.errorUri = errorUri;
    }

    public OAuth2ErrorResponse(OAuth2Error error, String errorDescription) {
        this.error = error;
        this.errorDescription = errorDescription;
    }

    public OAuth2ErrorResponse(OAuth2Exception exception) {
        this.error = exception.getError();
        this.errorDescription = exception.getErrorDescription();
        this.state = exception.getState();
        this.errorUri = exception.getErrorUri();
    }

    public OAuth2ErrorResponse(OAuth2Error error, Throwable throwable) {
        this.error = error;
        this.errorDescription = throwable.getLocalizedMessage();
    }

}