package org.daisleyharrison.security.common.models.cypher;

import java.security.spec.AlgorithmParameterSpec;

public interface CypherSpecification {
    public String getAlgorithm();
    public AlgorithmParameterSpec getParams();
}