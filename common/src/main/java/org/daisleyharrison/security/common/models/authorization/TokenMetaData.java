package org.daisleyharrison.security.common.models.authorization;

public interface TokenMetaData {
    public enum Type {
        JWT,
        JWE,
        JWS,
        OPAQUE
    }

    public String getName();
    public Type getType();
    public String getAlgHeader();
    public String getContentEncryptionAlg();
    public String getKey();
    public String getIssuer();
    public String getAudience();
    public int getExpires();
    public int getNotBefore();
}