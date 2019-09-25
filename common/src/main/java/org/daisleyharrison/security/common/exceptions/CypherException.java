package org.daisleyharrison.security.common.exceptions;

/**
 * base of all exceptions thrown by the CypherServiceProvider
 */
public class CypherException extends Exception {
    private static final long serialVersionUID = 4395382220278556657L;

    public CypherException(String message, Throwable inner) {
        super(message, inner);
    }
    public CypherException(Throwable inner) {
        super(inner.getMessage(), inner);
    }
    public CypherException(String message) {
        super(message);
    }
    public CypherException() {
        super();
    }
}