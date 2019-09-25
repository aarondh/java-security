package org.daisleyharrison.security.services.vault.models;

import org.daisleyharrison.security.common.models.key.KeySpecification;

public interface KeyGeneratorProvider {
    public KeyGeneratorTemplate getKeyGenerator(KeySpecification keySpec);
}