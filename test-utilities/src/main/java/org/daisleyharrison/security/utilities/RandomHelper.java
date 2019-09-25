package org.daisleyharrison.security.utilities;

import java.security.SecureRandom;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class RandomHelper {
    private static SecureRandom s_random;

    private RandomHelper() {
    }

    public static SecureRandom random() {
        if (s_random == null) {
            try {
                s_random = SecureRandom.getInstanceStrong();
            } catch (NoSuchAlgorithmException exception1) {
                try {
                    s_random = SecureRandom.getInstance("SHA1PRNG");
                } catch (NoSuchAlgorithmException exception2) {
                    throw new IllegalStateException("No valid secure random algorithm found.");
                }
            }
        }
        return s_random;
    }

    public static Object generateBytes(int length) {
        byte[] bytes = new byte[length];
        random().nextBytes(bytes);

        return Base64.getEncoder().encodeToString(bytes);
    }

    public static long nextLong(long min, long max) {
        if (min == 0 && max == 0) {
            return random().nextLong();
        }
        return ((long) (random().nextDouble() * (max - min))) + min;
    }

    public static long nextLong() {
        return random().nextLong();
    }

    public static int nextInt(int min, int max) {
        return random().nextInt(max - min + 1) + min;
    }

    public static int nextInt(int max) {
        return random().nextInt(max);
    }

    public static int nextInt() {
        return random().nextInt();
    }

    public static float nextFloat(float min, float max) {
        return random().nextFloat() * (max - min) + min;
    }

    public static float nextFloat() {
        return random().nextFloat();
    }

    public static double nextDouble(float min, float max) {
        return random().nextDouble() * (max - min) + min;
    }

    public static double nextDouble() {
        return random().nextDouble();
    }

}