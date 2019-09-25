package org.daisleyharrison.security.services.vault.models;

import java.security.spec.AlgorithmParameterSpec;

import org.daisleyharrison.security.common.models.key.KeyReference;

public class KeyRefImpl implements KeyReference {
    private String path;
    private int keySize;
    private char[] password;
    private AlgorithmParameterSpec[] parameters;

    public KeyRefImpl(String path, int keySize, char[] password, AlgorithmParameterSpec... parameters) {
        this.path = path;
        this.keySize = keySize;
        this.password = password;
        this.parameters = parameters;
    }

    public String getPath() {
        return path;
    }

    public int getKeySize() {
        return keySize;
    }

    public AlgorithmParameterSpec[] getParameters() {
        return parameters;
    }

    public char[] getPassword() {
        return password;
    }
}