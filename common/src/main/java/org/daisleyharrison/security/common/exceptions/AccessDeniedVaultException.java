package org.daisleyharrison.security.common.exceptions;

public class AccessDeniedVaultException extends VaultException {

    private static final long serialVersionUID = 573666583050936243L;

    public AccessDeniedVaultException(String message, Throwable inner) {
        super(message, inner);
    }

    public AccessDeniedVaultException(Throwable inner) {
        super(inner.getMessage(), inner);
    }

    public AccessDeniedVaultException(String message) {
        super(message);
    }

    public AccessDeniedVaultException() {
        super();
    }
}