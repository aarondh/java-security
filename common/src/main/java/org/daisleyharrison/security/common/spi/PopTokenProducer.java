package org.daisleyharrison.security.common.spi;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface PopTokenProducer {
    public Optional<String> producePopToken(String method, Map<String,List<String>> headers, URI uri, String body);
}