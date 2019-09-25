package org.daisleyharrison.security.services.vault.models;

import java.security.Key;
import java.security.KeyStore;

import org.daisleyharrison.security.common.exceptions.CypherException;
import org.daisleyharrison.security.common.models.key.KeySpecification;

public interface KeyGeneratorTemplate {
    public <T extends Key> T generateKey(KeySpecification keySpec, Class<T> type) throws CypherException;
    public void generateKeys(KeyStore keyStore, KeySpecification keySpec) throws CypherException;
}