package org.daisleyharrison.security.common.exceptions;

public class CypherFrameException extends CypherException {
    private static final long serialVersionUID = 4395382220278556657L;

    public CypherFrameException(String message, Throwable inner) {
        super(message, inner);
    }
    public CypherFrameException(String message) {
        super(message);
    }
    public CypherFrameException() {
        super();
    }
}