package org.daisleyharrison.security.common.models.cypher;

public interface CypherAction<TArg> {
    public void action(TArg arg) throws Exception;
}