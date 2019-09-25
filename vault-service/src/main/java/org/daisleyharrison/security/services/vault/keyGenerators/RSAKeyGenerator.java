package org.daisleyharrison.security.services.vault.keyGenerators;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Security;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.time.Duration;
import java.util.Date;

import org.daisleyharrison.security.common.exceptions.CypherException;
import org.daisleyharrison.security.common.models.key.KeySpecification;
import org.daisleyharrison.security.services.vault.models.KeyGeneratorAlgorithm;
import org.daisleyharrison.security.services.vault.models.KeyGeneratorTemplate;

import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

@KeyGeneratorAlgorithm({ "RSA", "RS256" })
public class RSAKeyGenerator implements KeyGeneratorTemplate {

    static {
        Security.addProvider(new BouncyCastleProvider());
    }


    private static X509Certificate createCertificate(String issuerDn, String subjectDn, Duration ttl, KeyPair keyPair)
            throws GeneralSecurityException, IOException, OperatorCreationException {
        if (issuerDn == null) {
            throw new IllegalArgumentException("issuerDn cannot be null");
        }
        if (subjectDn == null) {
            throw new IllegalArgumentException("subjectDn cannot be null");
        }
        if (ttl == null) {
            throw new IllegalArgumentException("ttl cannot be null");
        }
        if (keyPair == null) {
            throw new IllegalArgumentException("keyPair cannot be null");
        }
        PrivateKey privateKey = keyPair.getPrivate();
        PublicKey publicKey = keyPair.getPublic();
        Date startDate = new Date();
        Date expiryDate = Date.from(startDate.toInstant().plus(ttl));

        X500Name issuer = new X500Name(issuerDn);
        X500Name subject = new X500Name(subjectDn);
        BigInteger serialNumber = BigInteger.valueOf(Math.abs(new SecureRandom().nextLong()));
        X509v3CertificateBuilder certBuilder = new X509v3CertificateBuilder(issuer, serialNumber, startDate, expiryDate,
                subject, SubjectPublicKeyInfo.getInstance(publicKey.getEncoded()));
        JcaContentSignerBuilder builder = new JcaContentSignerBuilder("SHA256withRSA");
        ContentSigner signer = builder.build(privateKey);

        byte[] certBytes = certBuilder.build(signer).getEncoded();
        CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
        return (X509Certificate) certificateFactory.generateCertificate(new ByteArrayInputStream(certBytes));
    }

    @Override
    public <T extends Key> T generateKey(KeySpecification keySpec, Class<T> type) throws CypherException {
        throw new UnsupportedOperationException("generateKey for the RSAKeyGenerator is not supported");
    }

    @Override
    public void generateKeys(KeyStore keystore, KeySpecification keySpec) throws CypherException {
        if (keystore == null) {
            throw new IllegalArgumentException("keystore cannot be null");
        }
        if (keySpec == null) {
            throw new IllegalArgumentException("keySpec cannot be null");
        }
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(keySpec.getKeySize());
            KeyPair keyPair = keyPairGenerator.generateKeyPair();
            Certificate certificate = createCertificate(keySpec.getIssuer(), keySpec.getSubject(),
                    keySpec.getTTLDuration(), keyPair);
            java.security.cert.Certificate[] certificateChain = { certificate };

            keystore.setKeyEntry(keySpec.getKeyPath(), keyPair.getPrivate(), keySpec.getPassword(), certificateChain);
        } catch (OperatorCreationException | GeneralSecurityException | IOException exception) {
            throw new CypherException(exception);
        }
    }
}