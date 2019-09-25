package org.daisleyharrison.security.common.exceptions;

/**
 * base of all exceptions thrown by the OpenIdServiceProvider
 */
public class OpenIdException extends Exception {

    private static final long serialVersionUID = -421963667658070422L;

    public OpenIdException(String message, Throwable inner) {
        super(message, inner);
    }
    public OpenIdException(Throwable inner) {
        super(inner.getMessage(), inner);
    }
    public OpenIdException(String message) {
        super(message);
    }
    public OpenIdException() {
        super();
    }
}