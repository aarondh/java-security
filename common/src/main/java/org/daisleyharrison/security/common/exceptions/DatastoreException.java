package org.daisleyharrison.security.common.exceptions;

/**
 * base of all exceptions thrown by the DatastoreServiceProvider
 */
public class DatastoreException extends Exception {

    private static final long serialVersionUID = 585036536188576095L;

    public DatastoreException(String message, Throwable inner) {
        super(message, inner);
    }

    public DatastoreException(Throwable inner) {
        super(inner.getMessage(), inner);
    }

    public DatastoreException(String message) {
        super(message);
    }

    public DatastoreException() {
        super();
    }
}