package org.daisleyharrison.security.services.datastore.jsondb.models;

import org.daisleyharrison.security.common.exceptions.CypherException;
import org.daisleyharrison.security.common.models.cypher.StringCypher;

import io.jsondb.crypto.ICipher;

public class JsonDbCypherImpl implements ICipher {
    private StringCypher cypher;

    public JsonDbCypherImpl(StringCypher cypher) {
        this.cypher = cypher;
    }

    @Override
    public String encrypt(String plainText) {
        try {
            return cypher.encrypt(plainText);
        } catch (CypherException exception) {
            throw new IllegalStateException("encryption failed", exception);
        }
    }

    @Override
    public String decrypt(String cipherText) {
        try {
            return cypher.decrypt(cipherText);
        } catch (CypherException exception) {
            throw new IllegalStateException("decryption failed", exception);
        }
    }

}