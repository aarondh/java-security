package org.daisleyharrison.security.samples.spring.microservices.identityservice.models;

import java.util.Collections;
import java.util.List;

public class Scope {
    private String name;
    private String description;
    private List<Scope> scopes;

    public Scope() {
    }

    public Scope(String name, String description, List<Scope> scopes) {
        this.name = name;
        this.description = description;
        this.scopes = scopes;
    }

    public Scope(String name, String description) {
        this.name = name;
        this.description = description;
        this.scopes = Collections.emptyList();
    }

    /**
     * @return String return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
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

    @Override
    public String toString() {
        return name;
    }

}