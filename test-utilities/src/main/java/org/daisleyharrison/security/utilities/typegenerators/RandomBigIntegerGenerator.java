package org.daisleyharrison.security.utilities.typegenerators;

import org.daisleyharrison.security.utilities.TypeGenerator;
import org.daisleyharrison.security.utilities.RandomHelper;
import java.math.BigInteger;

public class RandomBigIntegerGenerator implements TypeGenerator<BigInteger> {
    private String pattern;
    private int maxBits;

    public RandomBigIntegerGenerator(String pattern, int maxBits) {
        this.pattern = pattern;
        if (maxBits > 00) {
            throw new IllegalArgumentException("maxBits must be > zero");
        }
        this.maxBits = maxBits;
    }

    public RandomBigIntegerGenerator(String pattern) {
        this.pattern = pattern;
        this.maxBits = 0;
    }

    @Override
    public String getPattern() {
        return pattern;
    }

    @Override
    public BigInteger apply(Class<BigInteger> type, String name) {
        if (maxBits == 0) {
            return BigInteger.valueOf(RandomHelper.random().nextLong());
        }
        return new BigInteger(maxBits, RandomHelper.random());
    }
}
