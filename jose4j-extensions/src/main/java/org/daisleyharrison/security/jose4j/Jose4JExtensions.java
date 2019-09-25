package org.daisleyharrison.security.jose4j;

import java.security.Security;

import org.daisleyharrison.security.jose4j.jwe.ChaCha20ContentEncryptionAlgorithm;
import org.jose4j.jwa.AlgorithmFactory;
import org.jose4j.jwa.AlgorithmFactoryFactory;
import org.jose4j.jwe.ContentEncryptionAlgorithm;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

public class Jose4JExtensions {
    public static void extend() {
        Security.addProvider(new BouncyCastleProvider());
        AlgorithmFactoryFactory factoryFactory = AlgorithmFactoryFactory.getInstance();
        AlgorithmFactory<ContentEncryptionAlgorithm> factory = factoryFactory.getJweContentEncryptionAlgorithmFactory();
        factory.registerAlgorithm(new ChaCha20ContentEncryptionAlgorithm.ChaCha20Poly1305());
        factory.registerAlgorithm(new ChaCha20ContentEncryptionAlgorithm.XChaCha20Poly1305());
    }
}