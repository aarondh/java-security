package org.daisleyharrison.security.services.key.internal;

import java.util.Iterator;
import java.util.LinkedList;

import java.util.List;

import org.daisleyharrison.security.common.exceptions.KeyProviderException;
import org.daisleyharrison.security.common.models.key.KeyProvider;
import org.daisleyharrison.security.common.models.key.KeyReference;

public class MultiplexedKeyProviderImpl implements KeyProvider {

    private List<KeyProvider> keyProviders;

    public MultiplexedKeyProviderImpl() {
        keyProviders = new LinkedList<>();
    }

    public void addKeyProvider(KeyProvider keyProvider) {
        keyProviders.add(keyProvider);
    }

    public int size() {
        return keyProviders.size();
    }

    public Iterator<KeyProvider> iterator(){
        return keyProviders.iterator();
    }

    @Override
    public boolean isSupported(KeyReference keyRef) {
        return keyProviders.stream().anyMatch(p -> p.isSupported(keyRef));
    }

    @Override
    public KeyProvider.KeyVersion resolveKey(KeyReference keyRef) throws KeyProviderException {
        return keyProviders.stream().filter(p -> p.isSupported(keyRef)).findFirst()
                .orElseThrow(KeyProviderException::new).resolveKey(keyRef);
    }

    @Override
    public void close() throws Exception {
        for (KeyProvider keyProvider : keyProviders) {
            keyProvider.close();
        }
    }

}