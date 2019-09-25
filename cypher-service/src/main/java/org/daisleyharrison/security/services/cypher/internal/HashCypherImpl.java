package org.daisleyharrison.security.services.cypher.internal;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;
import java.util.Base64;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

import org.daisleyharrison.security.common.exceptions.CypherException;
import org.daisleyharrison.security.common.models.cypher.HashCypher;

public class HashCypherImpl implements HashCypher, HashSpecification {
    private static final int DEFAULT_ITERATIONS = 65536;
    private static final int DEFAULT_KEY_LENGTH = 512;
    private static final String DEFAULT_ALGORITHM = "PBKDF2WithHmacSHA512";
    private static final int MINIMUM_SALT_LENGTH = 10;
    private HashSpecification hashSpec;
    private byte[] salt;

    public HashCypherImpl(HashSpecification hashSpec, byte[] salt) throws CypherException {
        this.hashSpec = hashSpec;
        if (salt == null) {
            salt = this.hashSpec.getSalt();
        }
        if (salt == null) {
            throw new CypherException("salt cannot be null");
        }
        if (salt == null || salt.length < MINIMUM_SALT_LENGTH) {
            throw new CypherException("salt length too small");
        }
        this.salt = salt;
    }

    @Override
    public String hash(char[] data) throws CypherException {
        if (data == null) {
            throw new IllegalArgumentException("data cannot be null");
        }
        PBEKeySpec spec = new PBEKeySpec(data, getSalt(), getIterations(), getKeyLength());

        try {
            SecretKeyFactory fac = SecretKeyFactory.getInstance(getAlgorithm());
            byte[] hash = fac.generateSecret(spec).getEncoded();
            return Base64.getEncoder().encodeToString(hash);

        } catch (NoSuchAlgorithmException | InvalidKeySpecException ex) {
            throw new CypherException(ex.getMessage(), ex);

        } finally {
            spec.clearPassword();
        }
    }

    @Override
    public boolean verify(String hash, char[] data) throws CypherException {
        if (hash == null) {
            throw new IllegalArgumentException("hash cannot be null");
        }
        if (data == null) {
            throw new IllegalArgumentException("data cannot be null");
        }
        return hash.equals(hash(data));
    }

    @Override
    public void close() throws Exception {
        Arrays.fill(salt, (byte)0);
    }

    @Override
    public String getAlgorithm() {
        String algorithm = hashSpec.getAlgorithm();
        if (algorithm == null) {
            return DEFAULT_ALGORITHM;
        }
        return algorithm;
    }

    @Override
    public byte[] getSalt() {
        return salt;
    }

    @Override
    public int getIterations() {
        int iterations = hashSpec.getIterations();
        if (iterations <= 0) {
            return DEFAULT_ITERATIONS;
        }
        return iterations;
    }

    @Override
    public int getKeyLength() {
        int keyLength = hashSpec.getKeyLength();
        if (keyLength <= 0) {
            return DEFAULT_KEY_LENGTH;
        }
        return keyLength;
    }

}