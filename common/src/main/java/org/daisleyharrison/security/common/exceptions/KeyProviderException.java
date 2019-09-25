package org.daisleyharrison.security.common.exceptions;

public class KeyProviderException extends CypherException {
    private static final long serialVersionUID = 8113002280135972876L;

    public KeyProviderException() {
        super();
    }
    public KeyProviderException(String message){
        super(message);
    }
    public KeyProviderException(String message, Throwable throwable){
        super(message, throwable);
    }
    public KeyProviderException(Throwable throwable){
        super(throwable.getMessage(), throwable);
    }
}