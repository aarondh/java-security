package org.daisleyharrison.security.services.cypher.cypherProvider.models;

public interface KeyDescriptor {
    public String getKeyAlgorithm();
    public int getKeySize();
}