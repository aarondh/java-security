package org.daisleyharrison.security.services.cypher.cypherProvider.internal;

import org.daisleyharrison.security.common.models.cypher.CypherEncryption;

public class CypherEncryptionImpl implements CypherEncryption {
    private byte[] iv;
    private byte[] authenicationTag;
    private byte[] secureData;

    public CypherEncryptionImpl(byte[] secureData, byte[] iv, byte[] authenicationTag) {
        this.secureData = secureData;
        this.iv = iv;
        this.authenicationTag = authenicationTag;
    }

    public CypherEncryptionImpl(byte[] secureData, byte[] iv) {
        this.secureData = secureData;
        this.iv = iv;
    }

    public CypherEncryptionImpl(byte[] secureData) {
        this.secureData = secureData;
    }

    @Override
    public byte[] getSecureData() {
        return secureData;
    }

    @Override
    public byte[] getIv() {
        return iv;
    }

    @Override
    public byte[] getAuthenicationTag() {
        return authenicationTag;
    }

}