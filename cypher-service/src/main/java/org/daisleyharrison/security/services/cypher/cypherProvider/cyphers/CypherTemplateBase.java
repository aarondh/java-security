package org.daisleyharrison.security.services.cypher.cypherProvider.cyphers;

import org.daisleyharrison.security.common.exceptions.CypherException;
import org.daisleyharrison.security.common.models.cypher.CypherEncryption;
import org.daisleyharrison.security.common.models.key.KeyReference;
import org.daisleyharrison.security.services.cypher.cypherProvider.internal.KeyDescriptorImpl;
import org.daisleyharrison.security.services.cypher.cypherProvider.models.CypherContext;
import org.daisleyharrison.security.services.cypher.cypherProvider.models.CypherDescriptor;
import org.daisleyharrison.security.services.cypher.cypherProvider.models.CypherTemplate;
import org.daisleyharrison.security.services.cypher.cypherProvider.models.KeyDescriptor;

public abstract class CypherTemplateBase extends KeyDescriptorImpl implements CypherTemplate, CypherDescriptor {
    private String algorithm;

    public CypherTemplateBase() {
    }

    /**
     * @param algorithm the java algorithm name
     */
    public void setAlgorithm(String algorithm) {
        this.algorithm = algorithm;
    }

    /**
     * @return String the java algorithm name
     */
    @Override
    public String getAlgorithm() {
        return algorithm;
    }

    @Override
    public KeyDescriptor getKeyDescriptor() {
        return this;
    }

    @Override
    public CypherDescriptor getCypherDescriptor() {
        return this;
    }

    @Override
    public abstract CypherEncryption encrypt(CypherContext context, KeyReference keyRef, byte[] iv,
            byte[] aad, byte[] unsecure) throws CypherException;

    @Override
    public abstract byte[] decrypt(CypherContext context, KeyReference keyRef, CypherEncryption encrption, byte[] aad)
            throws CypherException;
}