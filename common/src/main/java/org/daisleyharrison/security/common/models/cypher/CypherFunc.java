package org.daisleyharrison.security.common.models.cypher;

public interface CypherFunc<TArg> {
    public Object action(TArg arg) throws Exception;
}