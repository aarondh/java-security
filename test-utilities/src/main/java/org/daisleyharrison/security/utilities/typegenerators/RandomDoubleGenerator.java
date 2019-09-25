package org.daisleyharrison.security.utilities.typegenerators;

import org.daisleyharrison.security.utilities.TypeGenerator;
import org.daisleyharrison.security.utilities.RandomHelper;

public class RandomDoubleGenerator implements TypeGenerator<Double> {
    private String pattern;
    private double max;
    private double min;

    public RandomDoubleGenerator(String pattern, double min, double max) {
        this.pattern = pattern;
        if (min >= max) {
            throw new IllegalArgumentException("min must be < max");
        }
        this.min = min;
        this.max = max;
    }

    public RandomDoubleGenerator(String pattern) {
        this.pattern = pattern;
        this.min = 0;
        this.max = 0;
    }

    @Override
    public String getPattern() {
        return pattern;
    }

    @Override
    public Double apply(Class<Double> type, String name) {
        if (min == 0 && max == 0) {
            return RandomHelper.nextDouble();
        }
        return RandomHelper.nextDouble() * (max - min) + min;
    }
}
