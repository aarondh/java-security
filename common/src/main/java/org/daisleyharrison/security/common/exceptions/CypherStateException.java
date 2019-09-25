package org.daisleyharrison.security.common.exceptions;

public class CypherStateException extends CypherException {

    private static final long serialVersionUID = 1088994839198766803L;

    public CypherStateException(String message, Throwable inner) {
        super(message, inner);
    }
    public CypherStateException(String message) {
        super(message);
    }
    public CypherStateException() {
        super();
    }
}