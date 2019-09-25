package org.daisleyharrison.security.common.spi;

import java.net.URI;
import java.util.List;
import java.util.Map;

import org.daisleyharrison.security.common.exceptions.InvalidPopTokenException;

public interface PopTokenConsumer {
    public void consumePopToken(String method, Map<String, List<String>> headers, URI uri, String body, String token,
            String jwk) throws InvalidPopTokenException;
}