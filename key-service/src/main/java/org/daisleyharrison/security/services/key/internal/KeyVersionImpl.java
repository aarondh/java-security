package org.daisleyharrison.security.services.key.internal;

import java.security.Key;

import org.daisleyharrison.security.common.models.key.KeyPathComponents;
import org.daisleyharrison.security.common.models.key.KeyProvider;

public class KeyVersionImpl implements KeyProvider.KeyVersion {
    private Key key;
    private String versionPath;

    public KeyVersionImpl(String versionPath, Key key) {
        this.versionPath = versionPath;
        this.key = key;
    }

    @Override
    public String getPath() {
        return KeyPathComponents.getComponents(null, versionPath).getPath();
    }

    @Override
    public String getVersionPath() {
        return versionPath;
    }

    @Override
    public Key getKey() {
        return key;
    }

    @Override
    public <T extends Key> T getKey(Class<T> type) {
        return type.cast(key);
    }

}