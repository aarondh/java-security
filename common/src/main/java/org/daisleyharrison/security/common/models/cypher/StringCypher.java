package org.daisleyharrison.security.common.models.cypher;

import org.daisleyharrison.security.common.exceptions.CypherException;

public interface StringCypher {
    public String encrypt(String unsecure) throws CypherException;
    public String decrypt(String secure) throws CypherException;
}