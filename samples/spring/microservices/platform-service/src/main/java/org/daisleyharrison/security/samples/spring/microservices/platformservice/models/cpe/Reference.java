package org.daisleyharrison.security.samples.spring.microservices.platformservice.models.cpe;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Reference {

    @JsonProperty("href")
    private String href;

    @JsonProperty("value")
    private String value;

    public Reference() {
        
    }

    /**
     * @return String return the href
     */
    public String getHref() {
        return href;
    }

    /**
     * @param href the href to set
     */
    public void setHref(String href) {
        this.href = href;
    }

    /**
     * @return String return the value
     */
    public String getValue() {
        return value;
    }

    /**
     * @param value the value to set
     */
    public void setValue(String value) {
        this.value = value;
    }

}