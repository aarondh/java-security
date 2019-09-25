package org.daisleyharrison.security.common.models.key;

public interface KeyReference {
    public String getPath();
    default public char[] getPassword() {
        return null;
    }
}