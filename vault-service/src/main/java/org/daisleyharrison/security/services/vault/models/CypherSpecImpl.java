package org.daisleyharrison.security.services.vault.models;

import java.security.spec.AlgorithmParameterSpec;

import org.daisleyharrison.security.common.models.cypher.CypherSpecification;

public class CypherSpecImpl implements CypherSpecification {
    private String algorithm;

    private AlgorithmParameterSpec params;

    public CypherSpecImpl(String algorithm, AlgorithmParameterSpec params) {
        this.algorithm = algorithm;
        this.params = params;
    }

    public CypherSpecImpl(String algorithm) {
        this.algorithm = algorithm;
    }

    public String getAlgorithm() {
    return algorithm;
    }

    public AlgorithmParameterSpec getParams() {
        return params;
    }
}