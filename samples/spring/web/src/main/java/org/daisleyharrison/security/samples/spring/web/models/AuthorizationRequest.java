package org.daisleyharrison.security.samples.spring.web.models;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AuthorizationRequest {
    public enum Prompt {
        @JsonProperty("none")
        NONE, @JsonProperty("prompt")
        PROMPT
    }

    /**
     * REQUIRED. OpenID Connect requests MUST contain the openid scope value. If the
     * openid scope value is not present, the behavior is entirely unspecified.
     * Other scope values MAY be present. Scope values used that are not understood
     * by an implementation SHOULD be ignored. See Sections 5.4 and 11 for
     * additional scope values defined by this specification.
     */
    @JsonProperty("scope")
    private String scope;
    /**
     * REQUIRED. OAuth 2.0 Response Type value that determines the authorization
     * processing flow to be used, including what parameters are returned from the
     * endpoints used. When using the Authorization Code Flow, this value is code.
     */
    @JsonProperty("response_type")
    private String responseType;
    /**
     * REQUIRED. OAuth 2.0 Client Identifier valid at the Authorization Server.
     */
    @JsonProperty("client_id")
    private String clientId;
    /**
     * REQUIRED. Redirection URI to which the response will be sent. This URI MUST
     * exactly match one of the Redirection URI values for the Client pre-registered
     * at the OpenID Provider, with the matching performed as described in Section
     * 6.2.1 of [RFC3986] (Simple String Comparison). When using this flow, the
     * Redirection URI SHOULD use the https scheme; however, it MAY use the http
     * scheme, provided that the Client Type is confidential, as defined in Section
     * 2.1 of OAuth 2.0, and provided the OP allows the use of http Redirection URIs
     * in this case. The Redirection URI MAY use an alternate scheme, such as one
     * that is intended to identify a callback into a native application.
     */
    @JsonProperty("redirect_uri")
    private String redirectUri;
    /**
     * RECOMMENDED. Opaque value used to maintain state between the request and the
     * callback. Typically, Cross-Site Request Forgery (CSRF, XSRF) mitigation is
     * done by cryptographically binding the value of this parameter with a browser
     * cookie.
     */
    @JsonProperty("state")
    private String state;
    /**
     * OPTIONAL. Informs the Authorization Server of the mechanism to be used for
     * returning parameters from the Authorization Endpoint. This use of this
     * parameter is NOT RECOMMENDED when the Response Mode that would be requested
     * is the default mode specified for the Response Type.
     */
    @JsonProperty("response_mode")
    private String responseMode;
    /**
     * OPTIONAL. String value used to associate a Client session with an ID Token,
     * and to mitigate replay attacks. The value is passed through unmodified from
     * the Authentication Request to the ID Token. Sufficient entropy MUST be
     * present in the nonce values used to prevent attackers from guessing values.
     * For implementation notes, see Section 15.5.2.
     */
    @JsonProperty("nonce")
    private String nonce;
    /**
     * REQUIRED. Code challenge. (rfc7636)
     */
    @JsonProperty("code_challenge")
    private String codeChallenge;
    /**
     * OPTIONAL, defaults to "plain" if not present in the request. Code verifier
     * transformation method is "S256" or "plain". (rfc7636)
     */
    @JsonProperty("code_challenge_method")
    private String codeChallengeMethod;
    /**
     * The public key of the client
     */
    @JsonProperty("confirmation")
    private String confirmation;


    @JsonProperty("login_hint")
    private String loginHint;

    public AuthorizationRequest() {
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
     * @param responseType the responseType to set
     */
    public void setResponseType(String responseType) {
        this.responseType = responseType;
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
     * @return String return the responseMode
     */
    public String getResponseMode() {
        return responseMode;
    }

    /**
     * @param responseMode the responseMode to set
     */
    public void setResponseMode(String responseMode) {
        this.responseMode = responseMode;
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
     * @return String return the codeChallenge
     */
    public String getCodeChallenge() {
        return codeChallenge;
    }

    /**
     * @param codeChallenge the codeChallenge to set
     */
    public void setCodeChallenge(String codeChallenge) {
        this.codeChallenge = codeChallenge;
    }

    /**
     * @return String return the codeChallengeMethod
     */
    public String getCodeChallengeMethod() {
        return codeChallengeMethod;
    }

    /**
     * @param codeChallengeMethod the codeChallengeMethod to set
     */
    public void setCodeChallengeMethod(String codeChallengeMethod) {
        this.codeChallengeMethod = codeChallengeMethod;
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
     * @return String return the responseType
     */
    public String getResponseType() {
        return responseType;
    }

    /**
     * @return String return the loginHint
     */
    public String getLoginHint() {
        return loginHint;
    }

    /**
     * @param loginHint the loginHint to set
     */
    public void setLoginHint(String loginHint) {
        this.loginHint = loginHint;
    }

}