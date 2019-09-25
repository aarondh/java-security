package org.daisleyharrison.security.services.key.internal;

import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.util.Arrays;
import java.util.Enumeration;

import java.security.cert.Certificate;
import java.security.Key;

import org.daisleyharrison.security.common.exceptions.KeyProviderException;
import org.daisleyharrison.security.common.models.key.KeyPathComponents;
import org.daisleyharrison.security.common.models.key.KeyProvider;
import org.daisleyharrison.security.common.models.key.KeyReference;

public class KeyStoreKeyProviderImpl implements KeyProvider {
    private String keyPathRoot;
    private KeyStore keyStore;
    private char[] defaultPassword;

    public KeyStoreKeyProviderImpl(String keyPathRoot, KeyStore keyStore, char[] defaultPassword) {
        this.keyPathRoot = keyPathRoot;
        this.keyStore = keyStore;
        this.defaultPassword = defaultPassword;
    }

    private String toAlias(KeyPathComponents components) throws KeyStoreException {
        String alias = components.getAlias();
        int version = components.getVersion();
        if (version == 0) {
            // no version specified... need to find the latest version
            String currentAlias = null;
            int currentVersion = 0;
            Enumeration<String> ksAliases = keyStore.aliases();
            while (ksAliases.hasMoreElements()) {
                String ksAlias = ksAliases.nextElement();
                if (ksAlias.startsWith(alias)) {
                    KeyPathComponents aliasComponents = KeyPathComponents.getComponents(null, ksAlias);
                    if (aliasComponents.getVersion() > currentVersion) {
                        currentAlias = ksAlias;
                        currentVersion = aliasComponents.getVersion();
                    }
                }
            }
            return currentAlias == null ? components.getVersionAlias() : currentAlias;
        } else {
            return components.getVersionAlias();
        }
    }

    private char[] toPassword(KeyReference keyRef) {
        char[] password = keyRef.getPassword();
        if (password == null) {
            return this.defaultPassword;
        }
        return password;
    }

    @Override
    public boolean isSupported(KeyReference keyRef) {
        if (keyRef == null) {
            throw new IllegalArgumentException("keyRef cannot be null");
        }

        String keyPath = keyRef.getPath();

        if (keyPath == null) {
            throw new IllegalArgumentException("keyRef.getPath() cannot be null");
        }

        return keyPath.startsWith(keyPathRoot);
    }

    @Override
    public KeyVersion resolveKey(KeyReference keyRef) throws KeyProviderException {
        try {
            if (isSupported(keyRef)) {
                KeyPathComponents pathComponents = KeyPathComponents.getComponents(keyPathRoot, keyRef.getPath());
                String alias = toAlias(pathComponents);
                Key key = null;
                if (pathComponents.isPublic()) {
                    Certificate cert = keyStore.getCertificate(alias);
                    key = cert == null ? null : cert.getPublicKey();
                    alias = KeyPathComponents.addSuffix(alias, KeyPathComponents.PUBLIC_KEY_PATH_SUFFIX);
                } else {
                    key = keyStore.getKey(alias, toPassword(keyRef));
                }
                if (key == null) {
                    throw new KeyProviderException("key path " + pathComponents.getVersionPath() + " not found");
                }
                return new KeyVersionImpl(KeyPathComponents.join(keyPathRoot, alias), key);
            }
            throw new KeyProviderException("key path " + keyRef.getPath() + " is not supported");
        } catch (UnrecoverableKeyException | NoSuchAlgorithmException | KeyStoreException exception) {
            throw new KeyProviderException(exception);
        }
    }

    @Override
    public void close() throws Exception {
        this.keyStore = null;
        this.keyPathRoot = null;
        Arrays.fill(this.defaultPassword, Character.MAX_SURROGATE);
        this.defaultPassword = null;
    }
}