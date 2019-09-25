package org.daisleyharrison.security.services.key.utilities;

import java.lang.ref.SoftReference;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

import org.daisleyharrison.security.common.models.Cache;

public class SoftMemoryCache<T> implements Cache<T> {

    private static class CacheContainer<T> {

        private T value;
        private long expiryTime;

        public CacheContainer(T value, long expiryTime) {
            this.value = value;
            this.expiryTime = expiryTime;
        }

        public boolean isExpired() {
            return System.currentTimeMillis() > expiryTime;
        }

        public T getValue() {
            return value;
        }
    }

    private final ConcurrentHashMap<String, SoftReference<CacheContainer<T>>> cache = new ConcurrentHashMap<>();
    private Duration ttl;
    private Timer cleanupTimer;

    public SoftMemoryCache(Duration ttl, Duration cleanupPeriod) {
        this.ttl = ttl;
        start(Duration.ofMillis(ttl.toMillis() / 2));
    }

    private void start(Duration cleanupPeriod) {
        TimerTask cleanupTask = new TimerTask() {
            public void run() {
                cache.entrySet().removeIf(entry -> Optional.ofNullable(entry.getValue()).map(SoftReference::get)
                        .map(CacheContainer<T>::isExpired).orElse(false));
            }
        };
        cleanupTimer = new Timer("cleanUpTimer");
        cleanupTimer.scheduleAtFixedRate(cleanupTask, 0, cleanupPeriod.toMillis());
    }

    public void add(String key, T value) {
        if (key == null) {
            throw new IllegalArgumentException("key cannot be null");
        }
        if (value == null) {
            throw new IllegalArgumentException("key cannot be null");
        }
        long expiryTime = Instant.now().plus(ttl).toEpochMilli();
        cache.put(key, new SoftReference<>(new CacheContainer<T>(value, expiryTime)));
    }

    public T get(String key) {
        return Optional.ofNullable(cache.get(key)).map(SoftReference::get)
                .filter(cacheContainer -> !cacheContainer.isExpired()).map(CacheContainer<T>::getValue).orElse(null);
    }

    public T orElse(String key, OrElse<T> orElse) {
        T value = get(key);
        if (value == null) {
            value = orElse.orElse(key);
            add(key, value);
        }
        return value;
    }

    public long size() {
        return cache.entrySet().stream().filter(entry -> Optional.ofNullable(entry.getValue()).map(SoftReference::get)
                .map(cacheContainer -> !cacheContainer.isExpired()).orElse(false)).count();
    }

    public void evict(String key) {
        cache.remove(key);
    }

    public void clear() {
        cache.clear();
    }
}