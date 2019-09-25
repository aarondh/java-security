package org.daisleyharrison.security.common.exceptions;

public class CypherHandleException extends CypherException {

    private static final long serialVersionUID = 3901796795031630889L;

    public CypherHandleException(String message, Throwable inner) {
        super(message, inner);
    }
    public CypherHandleException(String message) {
        super(message);
    }
    public CypherHandleException() {
        super();
    }
}