package org.daisleyharrison.security.samples.jerseyService.models;

import javax.json.bind.annotation.JsonbProperty;

public class OpenIdRequest {
    @JsonbProperty("client_id")
    private String client_id;
    @JsonbProperty("login_hint")
    private String login_hint;
    @JsonbProperty("prompt")
    private String prompt;
    @JsonbProperty("nonce")
    private String nonce;
    @JsonbProperty("redirect_uri")
    private String redirect_uri;
    @JsonbProperty("state")
    private String state;
    @JsonbProperty("scope")
    private String scope;
    @JsonbProperty("response_mode")
    private String response_mode;
    @JsonbProperty("response_type")
    private String response_type;
    @JsonbProperty("access_type")
    private String access_type;

    /**
     * @return String return the client_id
     */
    public String getClient_id() {
        return client_id;
    }

    /**
     * @param client_id the client_id to set
     */
    public void setClient_id(String client_id) {
        this.client_id = client_id;
    }

    /**
     * @return String return the login_hint
     */
    public String getLogin_hint() {
        return login_hint;
    }

    /**
     * @param login_hint the login_hint to set
     */
    public void setLogin_hint(String login_hint) {
        this.login_hint = login_hint;
    }

    /**
     * @return String return the prompt
     */
    public String getPrompt() {
        return prompt;
    }

    /**
     * @param prompt the prompt to set
     */
    public void setPrompt(String prompt) {
        this.prompt = prompt;
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

    /**
     * @return String return the redirect_uri
     */
    public String getRedirect_uri() {
        return redirect_uri;
    }

    /**
     * @param redirect_uri the redirect_uri to set
     */
    public void setRedirect_uri(String redirect_uri) {
        this.redirect_uri = redirect_uri;
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
     * @return String return the response_mode
     */
    public String getResponse_mode() {
        return response_mode;
    }

    /**
     * @param response_mode the response_mode to set
     */
    public void setResponse_mode(String response_mode) {
        this.response_mode = response_mode;
    }

    /**
     * @return String return the response_type
     */
    public String getResponse_type() {
        return response_type;
    }

    /**
     * @param response_type the response_type to set
     */
    public void setResponse_type(String response_type) {
        this.response_type = response_type;
    }

    /**
     * @return String return the access_type
     */
    public String getAccess_type() {
        return access_type;
    }

    /**
     * @param access_type the access_type to set
     */
    public void setAccess_type(String access_type) {
        this.access_type = access_type;
    }

}