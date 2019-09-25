package org.daisleyharrison.security.services.cypher.cypherProvider.cyphers;

import java.security.GeneralSecurityException;
import java.security.Key;
import java.security.PrivateKey;
import java.security.PublicKey;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;

import org.daisleyharrison.security.common.exceptions.CypherException;
import org.daisleyharrison.security.common.models.cypher.CypherEncryption;
import org.daisleyharrison.security.common.models.key.KeyReference;
import org.daisleyharrison.security.common.models.key.KeyProvider.KeyVersion;
import org.daisleyharrison.security.services.cypher.cypherProvider.internal.CypherEncryptionImpl;
import org.daisleyharrison.security.services.cypher.cypherProvider.models.CypherAlgorithm;
import org.daisleyharrison.security.services.cypher.cypherProvider.models.CypherContext;

public class SimpleCypherTemplate<E extends Key, D extends Key> extends CypherTemplateBase {
    private Class<E> encryptionKeyType;
    private Class<D> decryptionKeyType;

    public SimpleCypherTemplate(String algorithm, String keyAlgorithm, int keySize, Class<E> encryptionKeyType,
            Class<D> decryptionKeyType) {
        setAlgorithm(algorithm);
        setKeyAlgorithm(keyAlgorithm);
        setKeySize(keySize);
        this.encryptionKeyType = encryptionKeyType;
        this.decryptionKeyType = decryptionKeyType;
    }

    @Override
    public CypherEncryption encrypt(CypherContext context, KeyReference keyRef, byte[] unsecure, byte[] iv, byte[] aad)
            throws CypherException {
        try {
            KeyVersion keyVersion = context.getKeyProvider().resolveKey(keyRef);
            Key key = keyVersion.getKey(encryptionKeyType);
            Cipher cipher = Cipher.getInstance(getAlgorithm());
            cipher.init(Cipher.ENCRYPT_MODE, key);
            return new CypherEncryptionImpl(cipher.doFinal(unsecure), iv, aad);
        } catch (GeneralSecurityException exception) {
            throw new CypherException(exception);
        }
    }

    @Override
    public byte[] decrypt(CypherContext context, KeyReference keyRef, CypherEncryption encryption, byte[] aad)
            throws CypherException {
        try {
            KeyVersion keyVersion = context.getKeyProvider().resolveKey(keyRef);
            Key key = keyVersion.getKey(decryptionKeyType);
            Cipher cipher = Cipher.getInstance(getAlgorithm());
            cipher.init(Cipher.DECRYPT_MODE, key);
            return cipher.doFinal(encryption.getSecureData());
        } catch (GeneralSecurityException exception) {
            throw new CypherException(exception);
        }
    }

    @CypherAlgorithm({"RSA/ECB/PKCS1Padding"})
    public static class Rsa256 extends SimpleCypherTemplate<PublicKey, PrivateKey> {
        public Rsa256() {
            super("RSA/ECB/PKCS1Padding", "RSA", 256, PublicKey.class, PrivateKey.class);
        }
    }

    @CypherAlgorithm({ "RSA", "RS256" })
    public static class Rsa extends SimpleCypherTemplate<PublicKey, PrivateKey> {
        public Rsa() {
            super("RSA", "RSA", 2048, PublicKey.class, PrivateKey.class);
        }
    }

    @CypherAlgorithm({ "AES", "AES/ECB/PKCS5Padding" })
    public static class Aes256 extends SimpleCypherTemplate<SecretKey, SecretKey> {
        public Aes256() {
            super("AES/ECB/PKCS5Padding", "AES", 256, SecretKey.class, SecretKey.class);
        }
    }

}