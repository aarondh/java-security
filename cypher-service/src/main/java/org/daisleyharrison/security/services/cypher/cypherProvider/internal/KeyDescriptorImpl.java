package org.daisleyharrison.security.services.cypher.cypherProvider.internal;

import org.daisleyharrison.security.services.cypher.cypherProvider.models.KeyDescriptor;

public class KeyDescriptorImpl implements KeyDescriptor {
    private String keyAlgorithm;
    private int keySize;

    public KeyDescriptorImpl() {
    }

    /**
     * @param algorithm the algorithm to set
     */
    public void setKeyAlgorithm(String keyAlgorithm) {
        this.keyAlgorithm = keyAlgorithm;
    }

    /**
     * @param keySize the keySize to set
     */
    public void setKeySize(int keySize) {
        this.keySize = keySize;
    }

    @Override
    public String getKeyAlgorithm() {
        return keyAlgorithm;
    }

    @Override
    public int getKeySize() {
        return keySize;
    }
}