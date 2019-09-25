package org.daisleyharrison.security.services.key.utilities;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

import org.daisleyharrison.security.common.models.Cache;

public class MemoryCache<T> implements Cache<T> {

    public interface OnEviction<T> {
        public void evict(String key, T value);
    }

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

        public void destroy() {
            value = null;
            expiryTime = 0;
        }
    }

    private final ConcurrentHashMap<String, CacheContainer<T>> cache = new ConcurrentHashMap<>();
    private Duration ttl;
    private Timer cleanupTimer;
    private OnEviction<T> onEviction;

    public MemoryCache(Duration ttl, OnEviction<T> onEviction) {
        this.ttl = ttl;
        this.onEviction = onEviction;
        start(Duration.ofMillis(ttl.toMillis() / 2));
    }

    private void start(Duration cleanupPeriod) {
        TimerTask cleanupTask = new TimerTask() {
            public void run() {
                cache.entrySet().removeIf(entry -> {
                    CacheContainer<T> container = entry.getValue();
                    if (container.isExpired()) {
                        onEviction.evict(entry.getKey(), container.getValue());
                        container.destroy();
                        return true;
                    } else {
                        return false;
                    }
                });
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
        cache.put(key, new CacheContainer<T>(value, expiryTime));
    }

    public T get(String key) {
        return Optional.ofNullable(cache.get(key)).filter(cacheContainer -> !cacheContainer.isExpired())
                .map(CacheContainer<T>::getValue).orElse(null);
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
        return cache.entrySet().stream().filter(
                entry -> Optional.ofNullable(entry.getValue()).map(container -> !container.isExpired()).orElse(false))
                .count();
    }

    public void evict(String key) {
        Optional.ofNullable(cache.remove(key)).ifPresent(container -> {
            T value = container.getValue();
            onEviction.evict(key, value);
            container.destroy();
        });
    }

    public void clear() {
        cache.entrySet().forEach(entry -> {
            CacheContainer<T> container = entry.getValue();
            onEviction.evict(entry.getKey(), container.getValue());
            container.destroy();
        });
    }
}