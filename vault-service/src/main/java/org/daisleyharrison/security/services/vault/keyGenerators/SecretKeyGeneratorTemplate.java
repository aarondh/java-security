package org.daisleyharrison.security.services.vault.keyGenerators;

import java.security.NoSuchAlgorithmException;

import javax.crypto.SecretKey;

import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;

import org.daisleyharrison.security.common.exceptions.CypherException;
import org.daisleyharrison.security.services.vault.models.KeyGeneratorAlgorithm;
import org.daisleyharrison.security.common.models.key.KeySpecification;
import org.daisleyharrison.security.services.vault.models.KeyGeneratorTemplate;

public class SecretKeyGeneratorTemplate implements KeyGeneratorTemplate {
    private String algorithm;
    public SecretKeyGeneratorTemplate(String algorithm){
        this.algorithm = algorithm;
    }
    @Override
    public <T extends Key> T generateKey(KeySpecification keySpec, Class<T> type) throws CypherException {
        if (keySpec == null) {
            throw new IllegalArgumentException("keyRef cannot be null");
        }
        try {
            javax.crypto.KeyGenerator keyGenerator = javax.crypto.KeyGenerator.getInstance(algorithm);
            keyGenerator.init(keySpec.getKeySize());
            return type.cast(keyGenerator.generateKey());
        } catch (NoSuchAlgorithmException exception) {
            throw new CypherException(exception);
        }
    }

    @Override
    public void generateKeys(KeyStore keystore, KeySpecification keySpec) throws CypherException {
        if (keystore == null) {
            throw new IllegalArgumentException("keystore cannot be null");
        }
        if (keySpec == null) {
            throw new IllegalArgumentException("keyRef cannot be null");
        }
        try {
            SecretKey secretKey = generateKey(keySpec, SecretKey.class);

            KeyStore.ProtectionParameter protParam = new KeyStore.PasswordProtection(keySpec.getPassword());

            KeyStore.SecretKeyEntry secretKeyEntry = new KeyStore.SecretKeyEntry(secretKey);

            keystore.setEntry(keySpec.getKeyPath(), secretKeyEntry, protParam);
        } catch (KeyStoreException exception) {
            throw new CypherException(exception);
        }

    }
    @KeyGeneratorAlgorithm({ "AES" })
    public static class AesKey extends SecretKeyGeneratorTemplate {
        public AesKey(){
            super("AES");
        }
    }
    @KeyGeneratorAlgorithm({ "ChaCha20" })
    public static class ChaCha20Key extends SecretKeyGeneratorTemplate {
        public ChaCha20Key(){
            super("ChaCha20");
        }
    }
}