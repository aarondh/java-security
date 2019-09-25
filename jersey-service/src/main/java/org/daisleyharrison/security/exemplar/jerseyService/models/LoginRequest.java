package org.daisleyharrison.security.samples.jerseyService.models;
import org.daisleyharrison.security.samples.jerseyService.utilities.PasswordAdapter;

import java.util.Arrays;

import javax.json.bind.annotation.JsonbProperty;
import javax.json.bind.annotation.JsonbTypeAdapter;
import javax.security.auth.Destroyable;

public class LoginRequest implements Destroyable {

    private String email;

    /**
     * NOTE that the password is ALWAYS stored in a char array.
     * This ensures that the passowrd is not interned as a String 
     * and therefore kept around longer than necessary
     * 
     * The PasswordAdapter class also makes sure that no String are 
     * unitentionaly created during the json parsing process.
     */
    @JsonbTypeAdapter(PasswordAdapter.class)
    private char[] password;

    /**
     * The uri the user will be redirected to after a successful login
     */
    private String redirectUri;
    /** 
     * If true the server will response with a redirect
     * otherwise the server will response with a JSON login response structure
     */
    private boolean redirect;

    @JsonbProperty("email")
    public String getEmail() {
        return this.email;
    }
    public void setEmail(String email) {
        this.email = email;
    }

    @JsonbProperty("password")
    public char[] getPassword() {
        return this.password;
    }
    public void setPassword(char[] password) {
        this.password = password;
    }


    @JsonbProperty("redirectUri")
    public String getRedirectUri() {
        return this.redirectUri;
    }
    public void setRedirectUri(String redirectUri) {
        this.redirectUri = redirectUri;
    }

    @JsonbProperty("redirect")
    public boolean isRedirect() {
        return this.redirect;
    }
    public void setRedirect(boolean redirect) {
        this.redirect = redirect;
    }

    @Override 
    public void destroy() {
        this.email = null;
        Arrays.fill(this.password, '*');
        this.password = null;
    }
}