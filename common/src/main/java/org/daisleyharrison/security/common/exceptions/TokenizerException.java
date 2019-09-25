package org.daisleyharrison.security.common.exceptions;

/**
 * base of all exceptions thrown by the TokenizerServiceProvider
 */
public class TokenizerException extends Exception {

    private static final long serialVersionUID = -5200133128245197914L;

    public TokenizerException(String message, Throwable inner) {
        super(message, inner);
    }

    public TokenizerException(Throwable inner) {
        super(inner.getMessage(), inner);
    }

    public TokenizerException(String message) {
        super(message);
    }

    public TokenizerException() {
        super();
    }
}