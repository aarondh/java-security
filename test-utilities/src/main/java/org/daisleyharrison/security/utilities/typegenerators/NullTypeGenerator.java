package org.daisleyharrison.security.utilities.typegenerators;

import org.daisleyharrison.security.utilities.TypeGenerator;

public class NullTypeGenerator<T> implements TypeGenerator<T> {
    private String pattern;
    public NullTypeGenerator(String pattern){
        this.pattern = pattern;
    }
    @Override
    public String getPattern() {
        return pattern;
    }

    @Override
    public T apply(Class<T> type, String name) {
        return null;
    }
}

