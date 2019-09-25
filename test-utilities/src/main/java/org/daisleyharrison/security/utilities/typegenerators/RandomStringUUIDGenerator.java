package org.daisleyharrison.security.utilities.typegenerators;

import java.util.UUID;

import org.daisleyharrison.security.utilities.TypeGenerator;

public class RandomStringUUIDGenerator implements TypeGenerator<String> {
    private String pattern;
    public RandomStringUUIDGenerator(String pattern){
        this.pattern = pattern;
    }
    @Override
    public String getPattern() {
        return pattern;
    }

    @Override
    public String apply(Class<String> type, String name) {
        return type.cast(UUID.randomUUID().toString());
    }
}

