package org.daisleyharrison.security.services.cypher.keyProvider;

import org.daisleyharrison.security.common.models.Stage;
import org.daisleyharrison.security.common.models.key.KeyFrame;

import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;

import org.daisleyharrison.security.common.exceptions.KeyProviderException;

public class KeyFrameImpl implements KeyFrame, Stage {
    private String key;
    private List<CloseListener> closeListeners = new ArrayList<>();
    private Set<String> keyPaths = new HashSet<>();
    private static ThreadLocal<String> frameKey = new ThreadLocal<>();

    public KeyFrameImpl(String key) throws KeyProviderException {
        synchronized (this) {
            if (frameKey.get() != null) {
                throw new KeyProviderException("A KeyFrame already exists in the current thread");
            }
            this.key = key;
            frameKey.set(key);
        }
    }

    public String getKey() {
        return this.key;
    }

    public static String getFrameKey() {
        return frameKey.get();
    }

    public static boolean hasFrame() {
        return frameKey.get() != null;
    }

    /**
     * Put a keyPath into the frame
     * 
     * @param handle
     */
    public void put(String keyPath) {
        this.keyPaths.add(keyPath);
    }

    protected Set<String> getKeyPaths() {
        return this.keyPaths;
    }

    /**
     * removes a keyPath from the frame
     * 
     * @param keyPath
     * @return
     */
    public boolean remove(String keyPath) {
        return this.keyPaths.remove(keyPath);
    }

    public void addCloseListener(CloseListener listener) {
        this.closeListeners.add(listener);
    }

    private void signalClosed() throws KeyProviderException {
        for (CloseListener closeListener : closeListeners) {
            closeListener.closed();
        }
    }

    /**
     * implements closable close() calls destroy() to release all handles in the
     * frame
     */
    @Override
    public void close() throws Exception {
        frameKey.set(null);
        signalClosed();
    }

    @Override
    public boolean equals(Object src) {
        if (src instanceof KeyFrameImpl) {
            KeyFrameImpl srcFrame = (KeyFrameImpl) src;
            return this.key.equals(srcFrame.key);
        } else if (src instanceof String) {
            String srcKey = (String) src;
            return this.key.equals(srcKey);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return this.key.hashCode();
    }
}
