package org.daisleyharrison.security.services.key.internal;

import java.time.Duration;

import javax.security.auth.DestroyFailedException;
import org.daisleyharrison.security.common.exceptions.KeyProviderException;
import org.daisleyharrison.security.common.models.key.CachedKeyProvider;
import org.daisleyharrison.security.common.models.key.KeyProvider;
import org.daisleyharrison.security.common.models.key.KeyReference;
import org.daisleyharrison.security.services.key.utilities.MemoryCache;

public class CachedKeyProviderImpl implements CachedKeyProvider {
    private KeyProvider keyProvider;
    private MemoryCache<KeyVersion> cache;

    public CachedKeyProviderImpl(KeyProvider keyProvider, Duration ttl) {
        this.keyProvider = keyProvider;
        this.cache = new MemoryCache<KeyVersion>(ttl, this::evict);
    }

    private void evict(String path, KeyVersion keyVersion) {
        try {
            keyVersion.destroy();
        } catch (DestroyFailedException exception) {
            // eat this exception
        }
    }

    @Override
    public void clear() {
        cache.clear();
    }

    @Override
    public void evict(String path) {
        cache.evict(path);
    }

    @Override
    public boolean isSupported(KeyReference keyRef) {
        return this.keyProvider.isSupported(keyRef);
    }

    @Override
    public KeyProvider.KeyVersion resolveKey(KeyReference keyRef) throws KeyProviderException {
        String path = keyRef.getPath();
        KeyVersion keyVersion = cache.get(path);
        if (keyVersion == null) {
            keyVersion = this.keyProvider.resolveKey(keyRef);
            cache.add(path, keyVersion);
        }
        return keyVersion;
    }

    @Override
    public void close() throws Exception {
        this.clear();
        this.keyProvider.close();
    }
}