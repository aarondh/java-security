package org.daisleyharrison.security.services.tokenizer.models;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

public class RandomTokenGeneratorImpl implements TokenGenerator {
    private static SecureRandom s_secureRandom;
    private String namespace;
    private int numberOfBytes;
    public static final int MINIMUM_NUMBER_OF_BYTES = 4;
    public static final int MAXIMUM_NUMBER_OF_BYTES = 256;

    public RandomTokenGeneratorImpl(String namespace, int numberOfBytes) {
        if (namespace == null) {
            throw new IllegalArgumentException("namespace cannot be null");
        }
        if (numberOfBytes < MINIMUM_NUMBER_OF_BYTES || numberOfBytes > MAXIMUM_NUMBER_OF_BYTES) {
            throw new IllegalArgumentException("Illegal numberOfBytes");
        }
        this.namespace = namespace;
        this.numberOfBytes = numberOfBytes;
        if (s_secureRandom == null) {
            try {
                s_secureRandom = SecureRandom.getInstanceStrong();
            } catch (NoSuchAlgorithmException exception1) {
                try {
                    s_secureRandom = SecureRandom.getInstance("SHA1PRNG");
                } catch (NoSuchAlgorithmException exception2) {
                    throw new IllegalStateException("No valid secure random algorithm found.");
                }
            }
        }
    }

    @Override
    public String produceToken() {
        byte[] tokenBytes = new byte[this.numberOfBytes];
        s_secureRandom.nextBytes(tokenBytes);

        return this.namespace + Base64.getEncoder().encodeToString(tokenBytes);
    }

}