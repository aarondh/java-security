package org.daisleyharrison.security.common.exceptions;

/**
 * base of all exceptions thrown by the AuthenticationServiceProvider
 */
public class AuthorizationException extends Exception {

    private static final long serialVersionUID = -4191354693644753424L;

    public AuthorizationException(String message, Throwable inner) {
        super(message, inner);
    }

    public AuthorizationException(Throwable inner) {
        super(inner.getMessage(), inner);
    }

    public AuthorizationException(String message) {
        super(message);
    }

    public AuthorizationException() {
        super();
    }
}