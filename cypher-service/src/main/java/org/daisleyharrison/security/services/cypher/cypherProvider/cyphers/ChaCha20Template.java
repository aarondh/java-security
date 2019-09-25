package org.daisleyharrison.security.services.cypher.cypherProvider.cyphers;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import java.security.GeneralSecurityException;

import org.daisleyharrison.security.common.exceptions.CypherException;
import org.daisleyharrison.security.common.models.cypher.CypherEncryption;
import org.daisleyharrison.security.common.models.key.KeyReference;
import org.daisleyharrison.security.common.models.key.KeyProvider.KeyVersion;
import org.daisleyharrison.security.services.cypher.cypherProvider.internal.CypherEncryptionImpl;
import org.daisleyharrison.security.services.cypher.cypherProvider.models.CypherAlgorithm;
import org.daisleyharrison.security.services.cypher.cypherProvider.models.CypherContext;

public class ChaCha20Template extends CypherTemplateBase {
    public ChaCha20Template(String algorithm, String keyAlgoritm, int keySize) {
        setAlgorithm(algorithm);
        setKeyAlgorithm(keyAlgoritm);
        setKeySize(keySize);
    }

    @Override
    public CypherEncryption encrypt(CypherContext context, KeyReference keyRef, byte[] unsecure, byte[] iv, byte[] aad)
            throws CypherException {

        try {

            Cipher cipher = Cipher.getInstance("ChaCha20-Poly1305/None/NoPadding");

            if (iv == null) {
                iv = new byte[12];
            }

            IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);
            KeyVersion keyVersion = context.getKeyProvider().resolveKey(keyRef);
            SecretKey key = keyVersion.getKey(SecretKey.class);

            SecretKeySpec secretKeySpec = new SecretKeySpec(key.getEncoded(), "ChaCha20");

            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, ivParameterSpec);

            return new CypherEncryptionImpl(cipher.doFinal(unsecure), iv, aad);
        } catch (GeneralSecurityException exception) {
            throw new CypherException(exception);
        }
    }

    @Override
    public byte[] decrypt(CypherContext context, KeyReference keyRef, CypherEncryption encryption, byte[] aad)
            throws CypherException {
        try {

            Cipher cipher = Cipher.getInstance("ChaCha20-Poly1305/None/NoPadding");

            byte[] iv = encryption.getIv();
            if (iv == null) {
                iv = new byte[12];
            }

            IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);

            KeyVersion keyVersion = context.getKeyProvider().resolveKey(keyRef);
            SecretKey key = keyVersion.getKey(SecretKey.class);

            SecretKeySpec secretKeySpec = new SecretKeySpec(key.getEncoded(), "ChaCha20");

            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, ivParameterSpec);

            return cipher.doFinal(encryption.getSecureData());
        } catch (GeneralSecurityException exception) {
            throw new CypherException(exception);
        }

    }

    @CypherAlgorithm({ "ChaCha20-128", "ChaCha20-Poly1305-128" })
    public static class ChaCha20Poly1305_128 extends ChaCha20Template {
        public ChaCha20Poly1305_128() {
            super("ChaCha20-Poly1305/None/NoPadding", "ChaCha20", 128);
        }
    }

    @CypherAlgorithm({ "ChaCha20", "ChaCha20-Poly1305" })
    public static class ChaCha20Poly1305_256 extends ChaCha20Template {
        public ChaCha20Poly1305_256() {
            super("ChaCha20-Poly1305/None/NoPadding", "ChaCha20", 128);
        }
    }
}