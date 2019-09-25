package org.daisleyharrison.security.utilities.typegenerators;

import org.daisleyharrison.security.utilities.TypeGenerator;
import org.daisleyharrison.security.utilities.RandomHelper;

public class RandomStringGenerator implements TypeGenerator<String> {
    public enum Mode {
        ALPHA_NUMERIC_MIXED, ALPHA_NUMERIC_LOWER, ALPHA_NUMERIC_UPPER, ALPHA_MIXED, ALPHA_LOWER, ALPHA_UPPER, PRINTABLE
    }

    private String pattern;
    private int minLength;
    private int maxLength;
    private Mode mode;

    public RandomStringGenerator(String pattern, int minLength, int maxLength, Mode mode) {
        this.pattern = pattern;
        this.minLength = minLength;
        this.maxLength = maxLength;
        if (minLength >= maxLength) {
            throw new IllegalArgumentException("minLengh must be < maxLength");
        }
        this.mode = mode;
    }

    public RandomStringGenerator(String pattern, int minLength, int maxLength) {
        this(pattern, minLength, maxLength, Mode.ALPHA_NUMERIC_MIXED);
    }

    @Override
    public String getPattern() {
        return pattern;
    }

    private static final String ALPHA_LOWER = "abcdefghijklmnopqrstuvwxyz";
    private static final String ALPHA_UPPER = ALPHA_LOWER.toUpperCase();
    private static final String ALPHA_MIXED = ALPHA_LOWER + ALPHA_UPPER;
    private static final String ALPHA_NUMERIC_MIXED = ALPHA_LOWER + ALPHA_UPPER + "0123456789";
    private static final String ALPHA_NUMERIC_LOWER = ALPHA_LOWER + "0123456789";
    private static final String ALPHA_NUMERIC_UPPER = ALPHA_UPPER + "0123456789";
    private static final String PRINTABLE = "!@#$%^&*()_-+={[}]|\\:;\"'<,>.?/'" + ALPHA_NUMERIC_MIXED;

    private String gen(String source, int length) {
        StringBuilder alpha = new StringBuilder();
        while (length-- > 0) {
            alpha.append(source.charAt(RandomHelper.nextInt(source.length())));
        }
        return alpha.toString();
    }

    @Override
    public String apply(Class<String> type, String name) {
        int length = RandomHelper.nextInt(maxLength - minLength) + minLength;
        if (length < 0) {
            return null;
        } else if (length == 0) {
            return "";
        }
        switch (mode) {
        case ALPHA_NUMERIC_MIXED:
            return gen(ALPHA_NUMERIC_MIXED, length);
        case ALPHA_NUMERIC_LOWER:
            return gen(ALPHA_NUMERIC_LOWER, length);
        case ALPHA_NUMERIC_UPPER:
            return gen(ALPHA_NUMERIC_UPPER, length);
        case ALPHA_MIXED:
            return gen(ALPHA_MIXED, length);
        case ALPHA_LOWER:
            return gen(ALPHA_LOWER, length);
        case ALPHA_UPPER:
            return gen(ALPHA_UPPER, length);
        case PRINTABLE:
            return gen(PRINTABLE, length);
        }
        return null;
    }

}
