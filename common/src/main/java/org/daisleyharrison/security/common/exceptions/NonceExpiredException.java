package org.daisleyharrison.security.common.exceptions;

public class NonceExpiredException extends TokenizerException {
    private static final long serialVersionUID = -3014701670767768757L;

    public NonceExpiredException(String message) {
        super(message);
    }
}