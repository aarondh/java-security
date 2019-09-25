package org.daisleyharrison.security.utilities;

public interface TypeGenerator<T> {
    public static final String PATTERN_MATCH_ALL = ".*";
    public String getPattern();
    public T apply(Class<T> type, String name) throws Exception;
}

