
package org.daisleyharrison.security.services.vault.models;

import com.fasterxml.jackson.annotation.JsonValue;

public enum PolicyPathType {
    GLOB("glob"), REGEX("regex");

    private String value;

    PolicyPathType(String value){
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