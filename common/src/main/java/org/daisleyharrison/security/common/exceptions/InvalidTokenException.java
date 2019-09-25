package org.daisleyharrison.security.common.exceptions;

public class InvalidTokenException extends TokenizerException {

    private static final long serialVersionUID = 1531310717271871214L;

    public InvalidTokenException(String message, Throwable inner) {
        super(message, inner);
    }

    public InvalidTokenException(Throwable inner) {
        super(inner.getMessage(), inner);
    }

    public InvalidTokenException(String message) {
        super(message);
    }

    public InvalidTokenException() {
        super();
    }
}