package org.daisleyharrison.security.common.models.key;

public interface CachedKeyProvider extends KeyProvider {
    public void clear();
    public void evict(String path);
}