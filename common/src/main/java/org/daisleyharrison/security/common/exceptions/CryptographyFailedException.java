package org.daisleyharrison.security.common.exceptions;

public class CryptographyFailedException extends CypherException {

    private static final long serialVersionUID = 2276367839604828322L;

    public CryptographyFailedException() {
        super();
    }

    public CryptographyFailedException(String message) {
        super(message);
    }

    public CryptographyFailedException(String message, Throwable throwable) {
        super(message, throwable);
    }
}