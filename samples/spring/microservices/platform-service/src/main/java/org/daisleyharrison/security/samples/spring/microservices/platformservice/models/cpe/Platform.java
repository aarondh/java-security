package org.daisleyharrison.security.samples.spring.microservices.platformservice.models.cpe;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Platform {

    @JsonProperty("name")
    private String name;

    @JsonProperty("title")
    private String title;

    @JsonProperty("references")
    private List<Reference> references;

    @JsonProperty("cpe23-item-name")
    private String cpe23ItemName;

    public Platform() {

    }

    public String getCpe23ItemName() {
        return cpe23ItemName;
    }

    public void setCpe23ItemName(String cpe23ItemName) {
        this.cpe23ItemName = cpe23ItemName;
    }

    public String getTitle() {
        return title;
    }

    public List<Reference> getReferences() {
        return references;
    }

    public void setReferences(List<Reference> references) {
        this.references = references;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}