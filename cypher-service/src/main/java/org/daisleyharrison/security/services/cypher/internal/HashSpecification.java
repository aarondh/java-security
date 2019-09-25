package org.daisleyharrison.security.services.cypher.internal;

public interface HashSpecification {
    public String getAlgorithm();
    public byte[] getSalt();
    public int getIterations();
    public int getKeyLength();
}