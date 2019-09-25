package org.daisleyharrison.security.common.models.cypher;

import org.daisleyharrison.security.common.exceptions.CypherException;

public interface Cypher {
    CypherEncryption encrypt(byte[] unsecure, byte[] iv, byte[] aad) throws CypherException;
    byte[] decrypt(CypherEncryption cypherEncryption, byte[] aad) throws CypherException;
    byte[] encrypt(byte[] unsecure) throws CypherException;
    byte[] decrypt(byte[] secure) throws CypherException;
}