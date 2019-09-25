package org.daisleyharrison.security.utilities.typegenerators;

import org.daisleyharrison.security.utilities.TypeGenerator;
import org.daisleyharrison.security.utilities.RandomHelper;

public class RandomLongGenerator implements TypeGenerator<Long> {
    private String pattern;
    private long max;
    private long min;

    public RandomLongGenerator(String pattern, long min, long max) {
        this.pattern = pattern;
        if (min >= max) {
            throw new IllegalArgumentException("min must be < max");
        }
        this.min = min;
        this.max = max;
    }

    public RandomLongGenerator(String pattern) {
        this.pattern = pattern;
        this.min = 0;
        this.max = 0;
    }

    @Override
    public String getPattern() {
        return pattern;
    }

    @Override
    public Long apply(Class<Long> type, String name) {
        if (min == 0 && max == 0) {
            return RandomHelper.nextLong();
        }
        return ((long) (RandomHelper.nextDouble() * (max - min))) + min;
    }
}
