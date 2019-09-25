package org.daisleyharrison.security.services.cypher.internal;

import org.daisleyharrison.security.common.exceptions.CypherFrameException;
import org.daisleyharrison.security.common.exceptions.CypherStateException;
import org.daisleyharrison.security.common.exceptions.CypherHandleException;

public interface CloseListener {
    public void closed() throws CypherFrameException, CypherStateException, CypherHandleException;
}
