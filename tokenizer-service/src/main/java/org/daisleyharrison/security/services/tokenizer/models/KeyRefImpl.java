package org.daisleyharrison.security.services.tokenizer.models;

import org.daisleyharrison.security.common.models.key.KeyReference;

public class KeyRefImpl implements KeyReference {
    private String path;
    private char[] password;

    public KeyRefImpl(String path, char[] password) {
        this.path = path;
        this.password = password;
    }

    public KeyRefImpl(String path) {
        this.path = path;
    }

    @Override
    public String getPath() {
        return path;
    }

    @Override
    public char[] getPassword() {
        return password;
    }
}