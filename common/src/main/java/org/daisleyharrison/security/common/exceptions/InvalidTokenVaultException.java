package org.daisleyharrison.security.common.exceptions;

public class InvalidTokenVaultException extends VaultException {

    private static final long serialVersionUID = -7992206487298411285L;

    public InvalidTokenVaultException(String message, Throwable inner) {
        super(message, inner);
    }

    public InvalidTokenVaultException(Throwable inner) {
        super(inner.getMessage(), inner);
    }

    public InvalidTokenVaultException(String message) {
        super(message);
    }

    public InvalidTokenVaultException() {
        super();
    }
}