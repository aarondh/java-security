package org.daisleyharrison.security.common.exceptions;

public class AuthorizationFailedException extends AuthorizationException {

    private static final long serialVersionUID = 4227198755544584061L;

    public AuthorizationFailedException() {

    }

    public AuthorizationFailedException(String message) {
        super(message);
    }

}