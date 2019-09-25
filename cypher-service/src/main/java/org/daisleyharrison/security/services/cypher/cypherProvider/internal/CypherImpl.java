package org.daisleyharrison.security.services.cypher.cypherProvider.internal;

import java.io.UnsupportedEncodingException;
import java.util.Base64;

import org.daisleyharrison.security.common.exceptions.CypherException;
import org.daisleyharrison.security.common.models.cypher.Cypher;
import org.daisleyharrison.security.common.models.cypher.CypherEncryption;
import org.daisleyharrison.security.common.models.key.KeyReference;
import org.daisleyharrison.security.services.cypher.cypherProvider.models.CypherContext;
import org.daisleyharrison.security.services.cypher.cypherProvider.models.CypherTemplate;
import org.daisleyharrison.security.common.models.cypher.StringCypher;

public class CypherImpl implements Cypher, StringCypher {
    private static final String DEFAULT_CHARSET = "UTF-8";
    private CypherContext context;
    private KeyReference keyRef;
    private CypherTemplate cypherTemplate;

    @Override
    public String encrypt(String unsecureText) throws CypherException {
        if (unsecureText == null) {
            return null;
        }
        try {
            byte[] unsecure = unsecureText.getBytes(DEFAULT_CHARSET);
            return Base64.getEncoder().encodeToString(encrypt(unsecure));
        } catch (UnsupportedEncodingException exception) {
            throw new CypherException(exception);
        }
    }

    @Override
    public String decrypt(String secureText) throws CypherException {
        if (secureText == null) {
            return null;
        }
        try {
            byte[] secure = Base64.getDecoder().decode(secureText);
            return new String(decrypt(secure), DEFAULT_CHARSET);
        } catch (UnsupportedEncodingException exception) {
            throw new CypherException(exception);
        }
    }

    @Override
    public CypherEncryption encrypt(byte[] unsecure, byte[] iv, byte[] aad) throws CypherException {
        if (unsecure == null) {
            return null;
        }
        return cypherTemplate.encrypt(context, keyRef, unsecure, iv, aad);
    }

    @Override
    public byte[] encrypt(byte[] unsecure) throws CypherException {
        if (unsecure == null) {
            return null;
        }
        return cypherTemplate.encrypt(context, keyRef, unsecure, null, null).getSecureData();
    }

    @Override
    public byte[] decrypt(CypherEncryption encryption, byte[] aad) throws CypherException {
        return cypherTemplate.decrypt(context, keyRef, encryption, aad);
    }

    @Override
    public byte[] decrypt(byte[] secure) throws CypherException {
        return cypherTemplate.decrypt(context, keyRef, new CypherEncryptionImpl(secure), null);
    }

    public CypherImpl(CypherContext context, CypherTemplate cypherTemplate, KeyReference keyRef) {
        this.context = context;
        this.cypherTemplate = cypherTemplate;
        this.keyRef = keyRef;
    }
}
