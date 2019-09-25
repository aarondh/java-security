package org.daisleyharrison.security.samples.spring.webtalker;

import java.util.Optional;

public interface TokenProvider {
    public static interface TokenInfo {
        public String getToken();
        public int getTtlInMinutes();
    }
    public Optional<TokenInfo> requestToken(String name, String tokenType);
}
