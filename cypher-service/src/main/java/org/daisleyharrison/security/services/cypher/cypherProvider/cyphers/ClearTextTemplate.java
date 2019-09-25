package org.daisleyharrison.security.services.cypher.cypherProvider.cyphers;

import org.daisleyharrison.security.common.exceptions.CypherException;
import org.daisleyharrison.security.common.models.cypher.CypherEncryption;
import org.daisleyharrison.security.common.models.key.KeyReference;
import org.daisleyharrison.security.services.cypher.cypherProvider.internal.CypherEncryptionImpl;
import org.daisleyharrison.security.services.cypher.cypherProvider.models.CypherAlgorithm;
import org.daisleyharrison.security.services.cypher.cypherProvider.models.CypherContext;
import org.daisleyharrison.security.services.cypher.cypherProvider.models.CypherDescriptor;
import org.daisleyharrison.security.services.cypher.cypherProvider.models.CypherTemplate;
import org.daisleyharrison.security.services.cypher.cypherProvider.models.KeyDescriptor;

@CypherAlgorithm("ClearText")
public class ClearTextTemplate implements CypherTemplate {

    public ClearTextTemplate() {

    }

    @Override
    public KeyDescriptor getKeyDescriptor() {
        return null;
    }

    @Override
    public CypherDescriptor getCypherDescriptor() {
        return new CypherDescriptor() {

            @Override
            public String getAlgorithm() {
                return "ClearText";
            }
        };
    }

    @Override
    public CypherEncryption encrypt(CypherContext context, KeyReference keyRef, byte[] iv, byte[] aad, byte[] unsecure)
            throws CypherException {
        return new CypherEncryptionImpl(unsecure, iv, aad);
    }

    @Override
    public byte[] decrypt(CypherContext context, KeyReference keyRef, CypherEncryption encryption, byte[] aad)
            throws CypherException {
        return encryption.getSecureData();
    }
}