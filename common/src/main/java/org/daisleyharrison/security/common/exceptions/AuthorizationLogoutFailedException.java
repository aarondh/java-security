package org.daisleyharrison.security.common.exceptions;

public class AuthorizationLogoutFailedException extends AuthorizationException {

    private static final long serialVersionUID = 4227198755544584361L;

    public AuthorizationLogoutFailedException() {

    }

    public AuthorizationLogoutFailedException(String message) {
        super(message);
    }

}