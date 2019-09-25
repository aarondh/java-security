
package org.daisleyharrison.security.services.vault.models;

import com.fasterxml.jackson.annotation.JsonValue;

public enum Capability {
    CREATE("create"), 
    READ("read"), 
    UPDATE("update"), 
    DELETE("delete"), 
    LIST("list"), 
    MANAGE("manage"), 
    DENY("deny");

    private String value;

    Capability(String value){
        this.value = value;
    }
    @JsonValue
    public String toValue() {
        return value;
    }
    @Override
    public String toString() {
        return value;
    }
}