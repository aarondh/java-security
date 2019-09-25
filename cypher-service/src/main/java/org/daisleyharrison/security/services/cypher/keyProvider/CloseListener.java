package org.daisleyharrison.security.services.cypher.keyProvider;

import org.daisleyharrison.security.common.exceptions.KeyProviderException;

public interface CloseListener {
    public void closed() throws KeyProviderException;
}
