package org.daisleyharrison.security.services.cypher.keyProvider;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

import org.daisleyharrison.security.common.exceptions.KeyProviderException;
import org.daisleyharrison.security.common.models.key.CachedKeyProvider;
import org.daisleyharrison.security.common.models.key.FramedKeyProvider;
import org.daisleyharrison.security.common.models.key.KeyFrame;
import org.daisleyharrison.security.common.models.key.KeyReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FramedKeyProviderImpl implements FramedKeyProvider, AutoCloseable {
    private static Logger LOGGER = LoggerFactory.getLogger(FramedKeyProviderImpl.class);
    private final Map<String, KeyFrameImpl> frames;
    private Map<String, Integer> countByKeyPath;
    private boolean requireFrame;
    private ReentrantLock lock;

    private CachedKeyProvider cachedKeyProvider;

    public FramedKeyProviderImpl(CachedKeyProvider cachedKeyProvider, boolean requireFrame) {
        this.cachedKeyProvider = cachedKeyProvider;
        this.requireFrame = requireFrame;
        this.lock = new ReentrantLock();
        this.frames = new ConcurrentHashMap<>();
        this.countByKeyPath = new ConcurrentHashMap<>();
    }

    /**
     * open a new frame not only one active frame is allowed per thread
     * 
     * @return the newly opened frame
     */
    @Override
    public KeyFrame openFrame() throws KeyProviderException {
        this.lock.lock();
        try {
            String key = UUID.randomUUID().toString();
            KeyFrameImpl frame = new KeyFrameImpl(key);
            frame.addCloseListener(() -> {
                closeFrame(frame);
            });
            this.frames.put(key, frame);
            return frame;
        } finally {
            this.lock.unlock();
        }
    }

    /**
     * Test if the current thread has an open frame
     * 
     * @return boolean true if has open frame
     */
    @Override
    public boolean hasFrame() {
        return KeyFrameImpl.hasFrame();
    }

    private void closeFrame(KeyFrameImpl frame) {
        this.lock.lock();
        try {
            frame.getKeyPaths().forEach(keyPath -> releaseKeyPath(keyPath));
            this.frames.remove(frame.getKey());
        } finally {
            this.lock.unlock();
        }
    }

    /**
     * get the current active frame on the current thread
     * 
     * @return
     * @throws CypherFrameException
     */
    private KeyFrameImpl getFrame() throws KeyProviderException {
        String frameKey = KeyFrameImpl.getFrameKey();
        if (frameKey == null) {
            throw new KeyProviderException("No open frame on this thread");
        } else {
            return this.frames.get(frameKey);
        }
    }

    private void releaseKeyPath(String keyPath) {
        try {
            Integer count = countByKeyPath.get(keyPath);
            if (count != null) {
                count = count - 1;
                if (count <= 0) {
                    this.cachedKeyProvider.evict(keyPath);
                    countByKeyPath.remove(keyPath);
                } else {
                    countByKeyPath.put(keyPath, count);
                }
            }
        } catch (Exception exception) {
            LOGGER.error("Error evicting key path {}: {}", keyPath, exception.getMessage());
        }
    }

    @Override
    public boolean isSupported(KeyReference keyRef) {
        return this.cachedKeyProvider.isSupported(keyRef);
    }

    @Override
    public KeyVersion resolveKey(KeyReference keyRef) throws KeyProviderException {
        this.lock.lock();
        try {
            String keyPath = keyRef.getPath();
            KeyVersion keyVersion = cachedKeyProvider.resolveKey(keyRef);
            if (hasFrame()) {
                getFrame().put(keyPath);
                Integer count = countByKeyPath.get(keyPath);
                if (count == null) {
                    countByKeyPath.put(keyPath, 1);
                } else {
                    countByKeyPath.put(keyPath, count + 1);
                }
            } else if (requireFrame) {
                throw new KeyProviderException("No open frame on this thread");
            }
            return keyVersion;
        } finally {
            this.lock.unlock();
        }
    }

    public void close() throws Exception {
        this.lock.lock();
        try {
            this.frames.values().forEach(frame -> {
                closeFrame(frame);
            });
        } finally {
            this.lock.unlock();
        }
    }

}
