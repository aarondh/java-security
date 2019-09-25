package org.daisleyharrison.security.samples.spring.webtalker;

import java.util.Optional;

public interface TokenManager {
    public void put(String name, String tokenType, String token, int ttlInMinutes);

    public void registerProvider(String name, String tokenType, TokenProvider tokenProvider);

    public Optional<String> get(String name, String tokenType);

}