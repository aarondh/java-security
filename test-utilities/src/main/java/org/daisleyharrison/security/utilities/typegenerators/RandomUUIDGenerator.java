package org.daisleyharrison.security.utilities.typegenerators;

import java.util.UUID;

import org.daisleyharrison.security.utilities.TypeGenerator;

public class RandomUUIDGenerator implements TypeGenerator<UUID> {
    private String pattern;

    public RandomUUIDGenerator(String pattern) {
        this.pattern = pattern;
    }

    @Override
    public String getPattern() {
        return pattern;
    }

    @Override
    public UUID apply(Class<UUID> type, String name) {
        return type.cast(UUID.randomUUID());
    }
}