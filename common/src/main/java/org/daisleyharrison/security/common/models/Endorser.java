package org.daisleyharrison.security.common.models;

import org.daisleyharrison.security.common.exceptions.CypherException;

public interface Endorser {
    String sign(String payload) throws CypherException;
    boolean verify(String signature, String payload) throws CypherException;
}