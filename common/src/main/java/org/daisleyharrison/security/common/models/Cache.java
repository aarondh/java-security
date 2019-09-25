package org.daisleyharrison.security.common.models;

public interface Cache<T> {
    public interface OrElse<T> {
        public T orElse(String key);
    }
    public void add(String key, T value);
    public T get(String key);
    public T orElse(String key, OrElse<T> readThru);
    public void evict(String key);
}