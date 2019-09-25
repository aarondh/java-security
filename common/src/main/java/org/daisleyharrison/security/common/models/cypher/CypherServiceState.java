package org.daisleyharrison.security.common.models.cypher;

public enum CypherServiceState {
    CREATED("Created"),
    INITIALIZING("Initializing"),
    INITIALIZED("Initialized"),
    COMPROMISED("Compromised"),
    CLOSED("Closed");

    private String label;
    CypherServiceState(String label){
        this.label = label;
    }
    public String toString(){
        return this.label;
    }
}