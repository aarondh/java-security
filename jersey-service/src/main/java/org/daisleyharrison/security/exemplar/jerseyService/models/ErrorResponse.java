package org.daisleyharrison.security.samples.jerseyService.models;

public class ErrorResponse{
    private String error;
    private String error_description;
    public ErrorResponse(){

    }
    public ErrorResponse(String error, String error_description){
        this.error = error;
        this.error_description = error_description;
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
     * @return String return the error_description
     */
    public String getError_description() {
        return error_description;
    }

    /**
     * @param error_description the error_description to set
     */
    public void setError_description(String error_description) {
        this.error_description = error_description;
    }

}