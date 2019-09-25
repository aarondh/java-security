package org.daisleyharrison.security.utilities.typegenerators;

import org.daisleyharrison.security.utilities.TypeGenerator;
import org.daisleyharrison.security.utilities.RandomHelper;

public class RandomFloatGenerator implements TypeGenerator<Float> {
    private String pattern;
    private float max;
    private float min;
    public RandomFloatGenerator(String pattern, float min, float max){
        this.pattern = pattern;
        if(min >= max){
            throw new IllegalArgumentException("min must be < max");
        }
        this.min = min;
        this.max = max;
    }
    public RandomFloatGenerator(String pattern){
        this.pattern = pattern;
        this.min = 0;
        this.max = 0;
    }
    @Override
    public String getPattern() {
        return pattern;
    }

    @Override
    public Float apply(Class<Float> type, String name) {
        if(min==0 && max==0){
            return RandomHelper.nextFloat();
        }
        return RandomHelper.nextFloat() * (max-min) + min;
    }
}