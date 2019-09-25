package org.daisleyharrison.security.common.models.cypher;

public interface CypherEncryption {
    public byte[] getIv();
    public byte[] getAuthenicationTag();
    public byte[] getSecureData();
}