package org.daisleyharrison.security.common.exceptions;

public class MalformedAuthClaimException extends AuthorizationException {

    private static final long serialVersionUID = -5832592899767280508L;

    public MalformedAuthClaimException(String message, Throwable exception) {
        super(message, exception);
    }
}