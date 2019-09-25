package org.daisleyharrison.security.utilities.typegenerators;
import org.daisleyharrison.security.utilities.TypeGenerator;
import org.daisleyharrison.security.utilities.RandomHelper;

public class RandomEnumGenerator implements TypeGenerator<Object> {
    private static RandomHelper RANDOM;
    private static RandomEnumGenerator s_instance = new RandomEnumGenerator();

    public static RandomEnumGenerator getInstance() {
        return s_instance;
    }

    private RandomEnumGenerator() {
    }

    @Override
    public String getPattern() {
        return PATTERN_MATCH_ALL;
    }

    @Override
    public Object apply(Class<Object> type, String name) throws Exception {
        Object[] enumValues = type.getEnumConstants();
        return enumValues[RANDOM.nextInt(enumValues.length)];
    }
}
