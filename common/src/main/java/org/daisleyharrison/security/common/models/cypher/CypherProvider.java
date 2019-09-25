package org.daisleyharrison.security.common.models.cypher;

import org.daisleyharrison.security.common.exceptions.CypherException;
import org.daisleyharrison.security.common.models.key.KeyReference;

public interface CypherProvider {
    Cypher getCypher(CypherSpecification cypherSpec, KeyReference keyRef) throws CypherException;
    StringCypher getStringCypher(CypherSpecification cypherSpec, KeyReference keyRef) throws CypherException;
}