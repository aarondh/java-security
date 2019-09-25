package org.daisleyharrison.security.services.vault.utilities;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import org.daisleyharrison.security.common.models.key.KeySpecification;
import org.daisleyharrison.security.services.vault.models.KeyGeneratorAlgorithm;
import org.daisleyharrison.security.services.vault.models.KeyGeneratorTemplate;
import org.daisleyharrison.security.services.vault.models.KeyGeneratorProvider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.classgraph.AnnotationInfo;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ScanResult;

public class KeyGeneratorProviderImpl implements KeyGeneratorProvider {
    private static Logger LOGGER = LoggerFactory.getLogger(KeyGeneratorProviderImpl.class);
    private Map<String, KeyGeneratorTemplate> generatorByName;

    public KeyGeneratorProviderImpl(String... packages) {
        this.generatorByName = new HashMap<>();
        addPackage(packages);
    }

    public void addKeyGeneratorTemplate(String algorithm, KeyGeneratorTemplate keyGeneratorTemplate){
        this.generatorByName.put(algorithm, keyGeneratorTemplate);
    }

    private void addPackage(String... packages){
        try (ScanResult scanResult = new ClassGraph().whitelistPackages(packages).enableClassInfo()
                .enableAnnotationInfo().scan()) {
            scanResult.getClassesWithAnnotation(KeyGeneratorAlgorithm.class.getName()).forEach(classInfo -> {
                AnnotationInfo annotationInfo = classInfo.getAnnotationInfo(KeyGeneratorAlgorithm.class.getName());
                try {
                    KeyGeneratorTemplate keyGeneratorTemplate = classInfo.loadClass(KeyGeneratorTemplate.class).getDeclaredConstructor()
                            .newInstance();
                            KeyGeneratorAlgorithm keyGeneratorAlgorithm = (KeyGeneratorAlgorithm) annotationInfo.loadClassAndInstantiate();
                    for (String algorithm : keyGeneratorAlgorithm.value()) {
                        addKeyGeneratorTemplate(algorithm, keyGeneratorTemplate);
                    }
                } catch (NoSuchMethodException | InstantiationException | IllegalAccessException
                        | InvocationTargetException exception) {
                    LOGGER.error("Failed to instantiate key generator template {}: {}", classInfo.getName(),
                            exception.getMessage());
                }
            });
        }
    }

    public KeyGeneratorTemplate getKeyGeneratorTemplate(String algorithm) {
        if (algorithm == null) {
            throw new IllegalArgumentException("cypheralgorithmSpec cannot be null");
        }
        KeyGeneratorTemplate keyGeneratorTemplate = generatorByName.get(algorithm);
        if (keyGeneratorTemplate == null) {
            throw new IllegalArgumentException("Unsupported cypher algorithm \"" + algorithm + "\"");
        }
        return keyGeneratorTemplate;
    }

    @Override
    public KeyGeneratorTemplate getKeyGenerator(KeySpecification keySpec) {
        return getKeyGeneratorTemplate(keySpec.getAlgorithm());
    }

}