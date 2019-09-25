package org.daisleyharrison.security.common.models.key;

import org.daisleyharrison.security.common.exceptions.KeyProviderException;

public interface FramedKeyProvider extends KeyProvider {
    public boolean hasFrame();
    public KeyFrame openFrame() throws KeyProviderException; 
}