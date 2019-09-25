package org.daisleyharrison.security.common.models.cypher;

import org.daisleyharrison.security.common.exceptions.CypherException;

public interface HashCypher {
    public String hash(char[] data) throws CypherException;
    public boolean verify(String hash, char[] data) throws CypherException;
    public void close() throws Exception;
}