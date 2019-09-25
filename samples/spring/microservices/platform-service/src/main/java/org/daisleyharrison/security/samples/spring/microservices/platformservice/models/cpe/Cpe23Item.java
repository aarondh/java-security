package org.daisleyharrison.security.samples.spring.microservices.platformservice.models.cpe;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Cpe23Item {

    @JsonProperty("namy")
    private String name;

    public Cpe23Item() {

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}