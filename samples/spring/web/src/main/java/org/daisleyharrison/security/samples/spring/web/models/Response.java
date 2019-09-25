package org.daisleyharrison.security.samples.spring.web.models;

public class Response {
    public static final String STATUS_OK = "ok";
    public static final String STATUS_ERROR = "error";
    public static final String STATUS_UNDEFINED = "undefined";
    private String status;

    public Response(String status) {
        this.status = status;
    }

    public Response() {
        this(STATUS_UNDEFINED);
    }

    /**
     * @return String return the status
     */
    public String getStatus() {
        return status;
    }

    /**
     * @param status the status to set
     */
    public void setStatus(String status) {
        this.status = status;
    }

}