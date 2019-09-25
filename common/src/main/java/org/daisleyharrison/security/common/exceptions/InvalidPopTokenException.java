package org.daisleyharrison.security.common.exceptions;

/**
 * Thrown by the PopTokenConsumer if the Pop token was invalid
 */
public class InvalidPopTokenException extends TokenizerException {


    private static final long serialVersionUID = 8988086870730317353L;

    public InvalidPopTokenException(String message, Throwable inner) {
        super(message, inner);
    }

    public InvalidPopTokenException(Throwable inner) {
        super(inner.getMessage(), inner);
    }

    public InvalidPopTokenException(String message) {
        super(message);
    }

    public InvalidPopTokenException() {
        super();
    }
}