package org.daisleyharrison.security.common.exceptions;

public class NonceReplayException extends TokenizerException {

    private static final long serialVersionUID = 2727059556511861709L;

    public NonceReplayException(String message) {
        super(message);
    }
}