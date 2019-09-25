package org.daisleyharrison.security.services.cypher.cypherProvider.models;

import org.daisleyharrison.security.common.exceptions.CypherException;
import org.daisleyharrison.security.common.models.cypher.CypherEncryption;
import org.daisleyharrison.security.common.models.key.KeyReference;

public interface CypherTemplate {
    public KeyDescriptor getKeyDescriptor();
    public CypherDescriptor getCypherDescriptor();
    public CypherEncryption encrypt(CypherContext context, KeyReference keyRef, byte[] unsecure, byte[] iv, byte[] aad) throws CypherException;
    public byte[] decrypt(CypherContext context, KeyReference keyRef, CypherEncryption encryption, byte[] aad) throws CypherException;
}