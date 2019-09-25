package org.daisleyharrison.security.common.exceptions;

/**
 * Thrown by the TokenizerServiceProvider if a token is valid bu expired
 */
public class TokenExpiredException extends TokenizerException {


    private static final long serialVersionUID = 8988086870730317353L;

    public TokenExpiredException(String message, Throwable inner) {
        super(message, inner);
    }

    public TokenExpiredException(Throwable inner) {
        super(inner.getMessage(), inner);
    }

    public TokenExpiredException(String message) {
        super(message);
    }

    public TokenExpiredException() {
        super();
    }
}