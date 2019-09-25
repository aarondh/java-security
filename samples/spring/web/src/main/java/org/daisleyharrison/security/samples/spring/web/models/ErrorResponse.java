package org.daisleyharrison.security.samples.spring.web.models;

public class ErrorResponse extends Response {
    private String error;
    private String description;
    public ErrorResponse(String error, String description){
        super(STATUS_ERROR);
        this.error = error;
        this.description = description;
    }

    public ErrorResponse(){
        super(STATUS_ERROR);
    }

    /**
     * @return String return the error
     */
    public String getError() {
        return error;
    }

    /**
     * @param error the error to set
     */
    public void setError(String error) {
        this.error = error;
    }

    /**
     * @return String return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * @param description the description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }

}