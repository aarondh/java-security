package org.daisleyharrison.security.samples.spring.microservices.identityservice.models;

public class OAuth2Exception extends Exception {
    /**
     *
     */
    private static final long serialVersionUID = -5924937940053875331L;
    private OAuth2Error error;
    private String errorDescription;
    private String errorUri;
    private String state;

    public OAuth2Exception(OAuth2Error error, String errorDescription) {
        super(error.toString() + ": " + errorDescription);
        this.error = error;
        this.errorDescription = errorDescription;
    }

    public OAuth2Exception(OAuth2Error error, String errorDescription, String state) {
        super(error.toString() + ": " + errorDescription);
        this.error = error;
        this.errorDescription = errorDescription;
        this.state = state;
    }

    public OAuth2Exception(OAuth2Error error, Throwable throwable, String state) {
        super(error.toString() + ": " + throwable.getLocalizedMessage());
        this.error = error;
        this.errorDescription = throwable.getLocalizedMessage();
        this.state = state;
    }

    public OAuth2Exception(OAuth2Error error, String errorDescription, String state, String errorUri) {
        super(error.toString() + ": " + errorDescription);
        this.error = error;
        this.state = state;
        this.errorUri = errorUri;
    }

    public OAuth2Exception(String errorDescription) {
        super(OAuth2Error.SERVER_ERROR.toString() + ": " + errorDescription);
        this.error = OAuth2Error.SERVER_ERROR;
        this.errorDescription = errorDescription;
    }

    public OAuth2Exception(String errorDescription, Throwable throwable) {
        super(OAuth2Error.SERVER_ERROR.toString() + ": " + errorDescription, throwable);
        this.error = OAuth2Error.SERVER_ERROR;
        this.errorDescription = errorDescription;
    }

    public OAuth2Exception(Throwable throwable) {
        super(OAuth2Error.SERVER_ERROR.toString() + ": " + throwable.getLocalizedMessage(), throwable);
        this.error = OAuth2Error.SERVER_ERROR;
        this.errorDescription = throwable.getLocalizedMessage();
    }

    /**
     * @return OAuth2Error return the error
     */
    public OAuth2Error getError() {
        return error;
    }

    /**
     * @param error the error to set
     */
    public void setError(OAuth2Error error) {
        this.error = error;
    }

    /**
     * @return String return the errorDescription
     */
    public String getErrorDescription() {
        return errorDescription;
    }

    /**
     * @param errorDescription the errorDescription to set
     */
    public void setErrorDescription(String errorDescription) {
        this.errorDescription = errorDescription;
    }

    /**
     * @return String return the errorUri
     */
    public String getErrorUri() {
        return errorUri;
    }

    /**
     * @param errorUri the errorUri to set
     */
    public void setErrorUri(String errorUri) {
        this.errorUri = errorUri;
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

}