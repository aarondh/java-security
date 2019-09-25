package org.daisleyharrison.security.utilities.typegenerators;

import org.daisleyharrison.security.utilities.TypeGenerator;
import org.daisleyharrison.security.utilities.RandomHelper;

public class RandomIntegerGenerator implements TypeGenerator<Integer> {
    private String pattern;
    private int max;
    private int min;
    public RandomIntegerGenerator(String pattern, int min, int max){
        this.pattern = pattern;
        if(min>=max){
            throw new IllegalArgumentException("min must be < max");
        }
        this.min = min;
        this.max = max;
    }
    public RandomIntegerGenerator(String pattern){
        this.pattern = pattern;
        this.min = 0;
        this.max = 0;
    }
    @Override
    public String getPattern() {
        return pattern;
    }

    @Override
    public Integer apply(Class<Integer> type, String name) {
        if(min==0 && max==0){
            return RandomHelper.nextInt();
        }
        return RandomHelper.nextInt(max-min) + min;
    }
}
