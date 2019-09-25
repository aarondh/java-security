package org.daisleyharrison.security.services.cypher.internal;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.Signature;
import java.security.SignatureException;
import java.util.Arrays;
import java.util.Base64;

import org.daisleyharrison.security.common.exceptions.CypherException;
import org.daisleyharrison.security.common.models.Endorser;
import org.daisleyharrison.security.common.models.cypher.CypherSpecification;
import org.daisleyharrison.security.common.models.key.KeyReference;
import org.daisleyharrison.security.services.cypher.CypherService;
import org.daisleyharrison.security.services.cypher.cypherProvider.internal.KeyRefImpl;

public class EndorserImpl implements Endorser {
    public static final String DEFAULT_CHARSET_NAME = "UTF-8";
    private CypherService service;
    private CypherSpecification cypherSpec;
    private KeyReference privateKeyRef;
    private KeyReference publicKeyRef;
    private Charset charset;

    public EndorserImpl(final CypherService service, final CypherSpecification cypherSpec, final KeyReference keyRef) throws CypherException {
        this.service = service;
        if (!service.keyExists(keyRef.getPath())) {
            throw new IllegalArgumentException("keyPath not found");
        }

        this.cypherSpec = cypherSpec;
        this.privateKeyRef = keyRef;
        this.publicKeyRef = new KeyRefImpl(KeyPathHelper.join(keyRef.getPath(), "public"));

        this.charset = Charset.forName("UTF-8");

    }

    private byte[] stringToBytes(String text) {
        ByteBuffer byteBuffer = charset.encode(text);
        return Arrays.copyOf(byteBuffer.array(), byteBuffer.limit());
    }

    @Override
    public String sign(String payload) throws CypherException {
        try {
            byte[] payloadBytes = stringToBytes(payload);
            Signature rsa = Signature.getInstance(cypherSpec.getAlgorithm());
            service.processWithPrivateKey(this.privateKeyRef.getPath(), privateKey -> {
                rsa.initSign(privateKey);
            });
            rsa.update(payloadBytes);

            byte[] signature = rsa.sign();

            return Base64.getEncoder().encodeToString(signature);

        } catch (SignatureException | NoSuchAlgorithmException exception) {
            throw new CypherException(exception);
        }
    }

    @Override
    public boolean verify(String signature, String payload) throws CypherException {
        try {
            byte[] signatureBytes = Base64.getDecoder().decode(signature);
            byte[] payloadBytes = stringToBytes(payload);
            Signature rsa = Signature.getInstance(cypherSpec.getAlgorithm());
            service.processWithPublicKey(this.publicKeyRef.getPath(), key -> {
                try {
                    rsa.initVerify(key);
                } catch (InvalidKeyException exception) {
                    throw new CypherException();
                }
            });
        rsa.update(payloadBytes);

            return rsa.verify(signatureBytes);
        } catch (SignatureException | NoSuchAlgorithmException exception) {
            throw new CypherException(exception);
        }
    }
}