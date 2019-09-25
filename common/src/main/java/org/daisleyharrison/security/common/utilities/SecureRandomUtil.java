package org.daisleyharrison.security.common.utilities;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

public class SecureRandomUtil {

    private static SecureRandom s_secureRandom;

    private static SecureRandom getSecureRandom() {
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
        return s_secureRandom;
    }

    public static String generateRandomString(int numberOfBytes) {
        if (numberOfBytes < 0) {
            throw new IllegalArgumentException("numberOfBytes is invalid");
        }

        byte[] jwtIdBytes = new byte[numberOfBytes];
        getSecureRandom().nextBytes(jwtIdBytes);

        return Base64.getEncoder().encodeToString(jwtIdBytes);
    }
}
