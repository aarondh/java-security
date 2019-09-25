package org.daisleyharrison.security.services.cypher.cypherProvider;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.daisleyharrison.security.common.exceptions.CypherException;
import org.daisleyharrison.security.common.models.cypher.Cypher;
import org.daisleyharrison.security.common.models.cypher.CypherProvider;
import org.daisleyharrison.security.common.models.cypher.CypherSpecification;
import org.daisleyharrison.security.common.models.key.KeyProvider;
import org.daisleyharrison.security.common.models.key.KeyReference;
import org.daisleyharrison.security.common.models.key.KeySpecification;
import org.daisleyharrison.security.common.models.cypher.StringCypher;
import org.daisleyharrison.security.services.cypher.cypherProvider.models.CypherAlgorithm;
import org.daisleyharrison.security.services.cypher.cypherProvider.models.CypherContext;
import org.daisleyharrison.security.services.cypher.cypherProvider.models.CypherTemplate;
import org.daisleyharrison.security.services.cypher.cypherProvider.internal.CypherImpl;
import java.util.Arrays;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.classgraph.AnnotationInfo;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ScanResult;

public class CypherProviderImpl implements CypherProvider, CypherContext {
    private static Logger LOGGER = LoggerFactory.getLogger(CypherProviderImpl.class);
    private Map<String, CypherTemplate> cypherSpecByName;
    private KeyProvider keyProvider;

    public CypherProviderImpl(String... packages) {
        this.cypherSpecByName = new HashMap<>();
        addPackage(packages);
    }

    public void setKeyProvider(KeyProvider keyProvider) {
        this.keyProvider = keyProvider;
    }

    @Override
    public KeyProvider getKeyProvider() {
        return keyProvider;
    }

    public void addCypher(String algorithm, CypherTemplate cypherTemplate) {
        this.cypherSpecByName.put(algorithm, cypherTemplate);
    }

    private void addPackage(String... packages) {
        try (ScanResult scanResult = new ClassGraph().whitelistPackages(packages).enableClassInfo()
                .enableAnnotationInfo().scan()) {
            scanResult.getClassesWithAnnotation(CypherAlgorithm.class.getName()).forEach(classInfo -> {
                AnnotationInfo annotationInfo = classInfo.getAnnotationInfo(CypherAlgorithm.class.getName());
                try {
                    CypherTemplate cypherTemplate = classInfo.loadClass(CypherTemplate.class).getDeclaredConstructor()
                            .newInstance();
                    CypherAlgorithm cypherAlgorithm = (CypherAlgorithm) annotationInfo.loadClassAndInstantiate();
                    for (String algorithm : cypherAlgorithm.value()) {
                        addCypher(algorithm, cypherTemplate);
                    }
                } catch (NoSuchMethodException | InstantiationException | IllegalAccessException
                        | InvocationTargetException exception) {
                    LOGGER.error("Failed to instantiate cypher specification {}: {}", classInfo.getName(),
                            exception.getMessage());
                }
            });
        }
    }

    public CypherTemplate getCypherTemplate(String algorithm) {
        if (algorithm == null) {
            throw new IllegalArgumentException("cypherSpec cannot be null");
        }
        CypherTemplate cypherTemplate = cypherSpecByName.get(algorithm);
        if (cypherTemplate == null) {
            throw new IllegalArgumentException("Unsupported cypher algorithm \"" + algorithm + "\"");
        }
        return cypherTemplate;
    }

    @Override
    public Cypher getCypher(CypherSpecification cypherSpec, KeyReference keyRef) throws CypherException {
        return new CypherImpl(this, getCypherTemplate(cypherSpec.getAlgorithm()), keyRef);
    }

    @Override
    public StringCypher getStringCypher(CypherSpecification cypherSpec, KeyReference keyRef) throws CypherException {
        return (StringCypher) getCypher(cypherSpec, keyRef);
    }
}