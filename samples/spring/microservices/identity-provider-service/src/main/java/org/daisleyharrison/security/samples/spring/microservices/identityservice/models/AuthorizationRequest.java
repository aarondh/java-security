package org.daisleyharrison.security.samples.spring.microservices.identityservice.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
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

    /**
     * OPTIONAL. ASCII string value that specifies how the Authorization Server displays the authentication and consent user interface pages to the End-User. The defined values are:
     * page
     *      The Authorization Server SHOULD display the authentication and consent UI consistent with a full User Agent page view. If the display parameter is not specified, this is the default display mode.
     * popup
     *      The Authorization Server SHOULD display the authentication and consent UI consistent with a popup User Agent window. The popup User Agent window should be of an appropriate size for a login-focused dialog and should not obscure the entire window that it is popping up over.
     * touch
     *      The Authorization Server SHOULD display the authentication and consent UI consistent with a device that leverages a touch interface.
     * wap
     *      The Authorization Server SHOULD display the authentication and consent UI consistent with a "feature phone" type display.
     *
     * The Authorization Server MAY also attempt to detect the capabilities of the User Agent and present an appropriate display.
     */
    @JsonProperty("display")
    private String display;
    /**
     * OPTIONAL. Space delimited, case sensitive list of ASCII string values that specifies whether the Authorization Server prompts the End-User for reauthentication and consent. The defined values are:
     * none
     *     The Authorization Server MUST NOT display any authentication or consent user interface pages. An error is returned if an End-User is not already authenticated or the Client does not have pre-configured consent for the requested Claims or does not fulfill other conditions for processing the request. The error code will typically be login_required, interaction_required, or another code defined in Section 3.1.2.6. This can be used as a method to check for existing authentication and/or consent.
     * login
     *     The Authorization Server SHOULD prompt the End-User for reauthentication. If it cannot reauthenticate the End-User, it MUST return an error, typically login_required.
     * consent
     *     The Authorization Server SHOULD prompt the End-User for consent before returning information to the Client. If it cannot obtain consent, it MUST return an error, typically consent_required.
     * select_account
     *     The Authorization Server SHOULD prompt the End-User to select a user account. This enables an End-User who has multiple accounts at the Authorization Server to select amongst the multiple accounts that they might have current sessions for. If it cannot obtain an account selection choice made by the End-User, it MUST return an error, typically account_selection_required.
     * The prompt parameter can be used by the Client to make sure that the End-User is still present for the current session or to bring attention to the request. If this parameter contains none with any other value, an error is returned.
     */
    @JsonProperty("prompt")
    private String prompt;

    /**
     * OPTIONAL. Maximum Authentication Age. 
     * Specifies the allowable elapsed time in seconds since the last time the End-User was actively authenticated by the OP. 
     * If the elapsed time is greater than this value, the OP MUST attempt to actively re-authenticate the End-User. 
     * (The max_age request parameter corresponds to the OpenID 2.0 PAPE [OpenID.PAPE] max_auth_age request parameter.) 
     * When max_age is used, the ID Token returned MUST include an auth_time Claim Value.
     */
    @JsonProperty("max_age")
    private String maxAge;
    /**
     * OPTIONAL. End-User's preferred languages and scripts for the user interface, 
     * represented as a space-separated list of BCP47 [RFC5646] language tag values, ordered by preference.
     *  For instance, the value "fr-CA fr en" represents a preference for French as spoken in Canada, then French (without a region designation), 
     * followed by English (without a region designation). 
     * An error SHOULD NOT result if some or all of the requested locales are not supported by the OpenID Provider.
     */
    @JsonProperty("ui_locales")
    private String uiLocales;
    /**
     * OPTIONAL. ID Token previously issued by the Authorization Server being passed as a hint about the End-User's current or past authenticated session with the Client. 
     * If the End-User identified by the ID Token is logged in or is logged in by the request, then the Authorization Server returns a positive response; otherwise, 
     * it SHOULD return an error, such as login_required. When possible, an id_token_hint SHOULD be present when prompt=none is used and an invalid_request error
     *  MAY be returned if it is not; however, the server SHOULD respond successfully when possible, even if it is not present. 
     * The Authorization Server need not be listed as an audience of the ID Token when it is used as an id_token_hint value.
     * If the ID Token received by the RP from the OP is encrypted, to use it as an id_token_hint,
     *  the Client MUST decrypt the signed ID Token contained within the encrypted ID Token. 
     * The Client MAY re-encrypt the signed ID token to the Authentication Servre 
     * using a key that enables the server to decrypt the ID Token, and use the re-encrypted ID token as the id_token_hint value.
     */
    @JsonProperty("id_token_hint")
    private String idTokenHint;
    /**
     * OPTIONAL. Hint to the Authorization Server about the login identifier the End-User might use to log in (if necessary). 
     * This hint can be used by an RP if it first asks the End-User for their e-mail address (or other identifier) and then wants 
     * to pass that value as a hint to the discovered authorization service. It is RECOMMENDED that the hint value match the value used for discovery. 
     * This value MAY also be a phone number in the format specified for the phone_number Claim. The use of this parameter is left to the OP's discretion.
     */
    @JsonProperty("login_hint")
    private String loginHint;
    /**
     * OPTIONAL. Requested Authentication Context Class Reference values. 
     * Space-separated string that specifies the acr values that the Authorization Server is being requested to use for processing this Authentication Request, 
     * with the values appearing in order of preference. 
     * The Authentication Context Class satisfied by the authentication performed is returned as the acr Claim Value, 
     * as specified in Section 2. 
     * The acr Claim is requested as a Voluntary Claim by this parameter.
     */
    @JsonProperty("acr_values")
    private String acrValues;

    public AuthorizationRequest() {
    }

    @JsonIgnore
    public String[] getScopes() {
        if (scope == null) {
            return new String[0];
        }
        return scope.split("\\s+");
    }

    @JsonIgnore
    public String[] getResponseTypes() {
        if (responseType == null) {
            return new String[0];
        }
        return responseType.split("\\s+");
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
     * @return String return the display
     */
    public String getDisplay() {
        return display;
    }

    /**
     * @param display the display to set
     */
    public void setDisplay(String display) {
        this.display = display;
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
     * @return String return the maxAge
     */
    public String getMaxAge() {
        return maxAge;
    }

    /**
     * @param maxAge the maxAge to set
     */
    public void setMaxAge(String maxAge) {
        this.maxAge = maxAge;
    }

    /**
     * @return String return the uiLocales
     */
    public String getUiLocales() {
        return uiLocales;
    }

    /**
     * @param uiLocales the uiLocales to set
     */
    public void setUiLocales(String uiLocales) {
        this.uiLocales = uiLocales;
    }

    /**
     * @return String return the idTokenHint
     */
    public String getIdTokenHint() {
        return idTokenHint;
    }

    /**
     * @param idTokenHint the idTokenHint to set
     */
    public void setIdTokenHint(String idTokenHint) {
        this.idTokenHint = idTokenHint;
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

    /**
     * @return String return the acrValues
     */
    public String getAcrValues() {
        return acrValues;
    }

    /**
     * @param acrValues the acrValues to set
     */
    public void setAcrValues(String acrValues) {
        this.acrValues = acrValues;
    }

}