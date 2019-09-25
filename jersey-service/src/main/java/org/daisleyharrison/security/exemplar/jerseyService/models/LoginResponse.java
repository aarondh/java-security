package org.daisleyharrison.security.samples.jerseyService.models;

import org.daisleyharrison.security.common.exceptions.MalformedAuthClaimException;
import org.daisleyharrison.security.common.models.authorization.AuthClaims;

import javax.json.bind.annotation.JsonbProperty;

public class LoginResponse {

    private String givenName;
    private String familyName;
    private String preferredUsername;
    private String token;
    private String cookie;
    private String redirectUri;

    public LoginResponse() {

    }

    public LoginResponse(AuthClaims userClaims, String token, String cookie, String redirectUri) {
        try {
            setGivenName(userClaims.getGivenName());
        } catch (MalformedAuthClaimException exception) {

        }

        try {
            setFamilyName(userClaims.getFamilyName());
        } catch (MalformedAuthClaimException exception) {

        }

        try {
            setPreferredUsername(userClaims.getPreferredUsername());
        } catch (MalformedAuthClaimException exception) {

        }
        setToken(token);
        setCookie(cookie);
        setRedirectUri(redirectUri);
    }

    @JsonbProperty("givenName")
    public String getGivenName() {
        return this.givenName;
    }

    public void setGivenName(String givenName) {
        this.givenName = givenName;
    }

    @JsonbProperty("familyName")
    public String getFamilyName() {
        return this.familyName;
    }

    public void setFamilyName(String familyName) {
        this.familyName = familyName;
    }


    /**
     * @return String return the preferredUsername
     */
    @JsonbProperty("preferredUsername")
    public String getPreferredUsername() {
        return preferredUsername;
    }

    /**
     * @param preferredUsername the preferredUsername to set
     */
    public void setPreferredUsername(String preferredUsername) {
        this.preferredUsername = preferredUsername;
    }

    @JsonbProperty("token")
    public String getToken() {
        return this.token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    @JsonbProperty("cookie")
    public String getCookie() {
        return this.cookie;
    }

    public void setCookie(String cookie) {
        this.cookie = cookie;
    }

    @JsonbProperty("redirectUri")
    public String getRedirectUri() {
        return this.redirectUri;
    }

    public void setRedirectUri(String redirectUri) {
        this.redirectUri = redirectUri;
    }

}