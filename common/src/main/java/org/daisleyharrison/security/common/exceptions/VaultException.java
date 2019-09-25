package org.daisleyharrison.security.common.exceptions;

/**
 * base of all exceptions thrown by the VaultServiceProvider
 */
public class VaultException extends Exception {

    private static final long serialVersionUID = -5167138123787777046L;

    public VaultException(String message, Throwable inner) {
        super(message, inner);
    }

    public VaultException(Throwable inner) {
        super(inner.getMessage(), inner);
    }

    public VaultException(String message) {
        super(message);
    }

    public VaultException() {
        super();
    }
}