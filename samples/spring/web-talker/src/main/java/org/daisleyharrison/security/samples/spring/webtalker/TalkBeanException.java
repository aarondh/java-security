package org.daisleyharrison.security.samples.spring.webtalker;

import org.springframework.http.HttpStatus;

public class TalkBeanException extends Exception {
    /**
     *
     */
    private static final long serialVersionUID = 1092535235733144138L;
    private HttpStatus status;
    private String body;

    public TalkBeanException(HttpStatus status, String body){
        super(status.toString());
        this.status = status;
        this.body = body;
    }

    /**
     * @return HttpStatus return the status
     */
    public HttpStatus getStatus() {
        return status;
    }

    /**
     * @param status the status to set
     */
    public void setStatus(HttpStatus status) {
        this.status = status;
    }

    /**
     * @return String return the body
     */
    public String getBody() {
        return body;
    }

    /**
     * @param body the body to set
     */
    public void setBody(String body) {
        this.body = body;
    }

}