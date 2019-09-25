package org.daisleyharrison.security.common.models.key;

import org.daisleyharrison.security.common.exceptions.KeyProviderException;
import javax.security.auth.Destroyable;

import java.security.Key;

public interface KeyProvider {

    public static interface KeyVersion extends Destroyable {
        public String getPath();

        public String getVersionPath();
      
        public Key getKey();
        
        public <T extends Key> T getKey(Class<T> type);
    }

    public boolean isSupported(KeyReference keyRef);

    public KeyProvider.KeyVersion resolveKey(KeyReference keyRef) throws KeyProviderException;

    public void close() throws Exception;
}