package org.daisleyharrison.security.utilities.typegenerators;

import org.daisleyharrison.security.utilities.TypeGenerator;
import org.daisleyharrison.security.utilities.RandomHelper;

public class RandomBooleanGenerator implements TypeGenerator<Boolean> {
    private String pattern;

    public RandomBooleanGenerator(String pattern) {
        this.pattern = pattern;
    }

    @Override
    public String getPattern() {
        return pattern;
    }

    @Override
    public Boolean apply(Class<Boolean> type, String name) {
        return RandomHelper.nextInt(2) == 0 ? false : true;
    }
}