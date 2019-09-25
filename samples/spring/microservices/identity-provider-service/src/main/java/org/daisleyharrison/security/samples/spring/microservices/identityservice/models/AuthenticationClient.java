package org.daisleyharrison.security.samples.spring.microservices.identityservice.models;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AuthenticationClient {

    public enum ClientType {
        @JsonProperty("public")
        PUBLIC, @JsonProperty("confidential")
        CONFIDENTIAL
    }
    private String _id;
    private String clientId;
    private ClientType clientType;
    private String domain;
    private String email;
    private boolean allowsGetMethod;
    private List<String> returnUris;
    private List<Scope> scopes;
    private List<AccessToken> accessTokens;

    public AuthenticationClient() {
        this._id = UUID.randomUUID().toString();
        this.accessTokens = new ArrayList<>();
        this.scopes = new ArrayList<>();
        this.returnUris = new ArrayList<>();
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
     * @return String return the domain
     */
    public String getDomain() {
        return domain;
    }

    /**
     * @param domain the domain to set
     */
    public void setDomain(String domain) {
        this.domain = domain;
    }

    /**
     * @return List<String> return the returnUris
     */
    public List<String> getReturnUris() {
        return returnUris;
    }

    /**
     * @param returnUris the returnUris to set
     */
    public void setReturnUris(List<String> returnUris) {
        this.returnUris = returnUris;
    }

    /**
     * @return List<Scope> return the scopes
     */
    public List<Scope> getScopes() {
        return scopes;
    }

    /**
     * @param scopes the scopes to set
     */
    public void setScopes(List<Scope> scopes) {
        this.scopes = scopes;
    }

    public void addAccessToken(String token, Duration ttl) {
        this.accessTokens.add(new AccessToken(token, ttl));
    }

    public boolean hasScope(String scopeName) {
        for (Scope scope : this.scopes) {
            if (scope.getName().equals(scopeName)) {
                return true;
            }
        }
        return false;
    }

    public boolean hasRedirectUri(String redirectUri) {
        for (String uri : this.returnUris) {
            if (uri.equals(redirectUri)) {
                return true;
            }
        }
        return false;
    }

    public boolean hasAccessToken(String token) {
        return accessTokens.stream().filter(AccessToken::isValid).anyMatch(ac->ac.getToken().equals(token));
    }

    public Optional<String> getFirstValidAccessToken() {
        return accessTokens.stream().filter(AccessToken::isValid).map(AccessToken::getToken).findFirst();
    }

    /**
     * @return List<AccessToken> return the accessTokens
     */
    public List<AccessToken> getAccessTokens() {
        return accessTokens;
    }

    /**
     * @param accessTokens the accessTokens to set
     */
    public void setAcessTokens(List<AccessToken> accessTokens) {
        this.accessTokens = accessTokens;
    }

    /**
     * @return ClientType return the clientType
     */
    public ClientType getClientType() {
        return clientType;
    }

    /**
     * @param clientType the clientType to set
     */
    public void setClientType(ClientType clientType) {
        this.clientType = clientType;
    }

    /**
     * @return boolean return the allowsGetMethod
     */
    public boolean isAllowsGetMethod() {
        return allowsGetMethod;
    }

    /**
     * @param allowsGetMethod the allowsGetMethod to set
     */
    public void setAllowsGetMethod(boolean allowsGetMethod) {
        this.allowsGetMethod = allowsGetMethod;
    }


    /**
     * @return String return the _id
     */
    public String get_id() {
        return _id;
    }

    /**
     * @param _id the _id to set
     */
    public void set_id(String _id) {
        this._id = _id;
    }

    /**
     * @param accessTokens the accessTokens to set
     */
    public void setAccessTokens(List<AccessToken> accessTokens) {
        this.accessTokens = accessTokens;
    }


    /**
     * @return String return the email
     */
    public String getEmail() {
        return email;
    }

    /**
     * @param email the email to set
     */
    public void setEmail(String email) {
        this.email = email;
    }


    @Override 
    public String toString() {
        return clientId + ": " + scopes.stream().map(Object::toString).collect(Collectors.joining(", "));
    }

}