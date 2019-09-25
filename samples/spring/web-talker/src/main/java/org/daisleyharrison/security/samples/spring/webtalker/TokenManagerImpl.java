package org.daisleyharrison.security.samples.spring.webtalker;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class TokenManagerImpl implements TokenManager {
    private static final String ALL_MICROSERVICES = "*";
    private static final Map<TokenEntry.Key, TokenEntry> s_cache = new HashMap<>();

    public static class TokenEntry {

        public static class Key {
            private String name;
            private String tokenType;

            public Key(String name, String tokenType) {
                this.name = name;
                this.tokenType = tokenType;
            }

            @Override
            public String toString() {
                return name + ": " + tokenType;
            }

            @Override
            public int hashCode() {
                return name.hashCode() | tokenType.hashCode();
            }

            @Override
            public boolean equals(Object obj) {
                if (obj instanceof Key) {
                    Key key = (Key) obj;
                    return this.name.equals(key.name) && this.tokenType.equals(key.tokenType);
                }
                return false;
            }
        }

        private String name;
        private String tokenType;
        private String token;
        private int ttlInMinutes;
        private long created;
        private TokenProvider tokenProvider;

        TokenEntry(String name, String tokenType, String token, int ttlInMinutes) {
            this.name = name;
            this.tokenType = tokenType;
            this.token = token;
            this.ttlInMinutes = ttlInMinutes;
            this.created = new Date().getTime();
        }

        TokenEntry(String name, String tokenType, TokenProvider tokenProvider) {
            this.name = name;
            this.tokenType = tokenType;
            this.tokenProvider = tokenProvider;
            clearToken();
        }

        TokenEntry(String name, String tokenType) {
            this.name = name;
            this.tokenType = tokenType;
        }

        public TokenProvider getTokenProvider() {
            return tokenProvider;
        }

        public void setTokeProvider(TokenProvider tokenProvider) {
            this.tokenProvider = tokenProvider;
        }

        public String getName() {
            return name;
        }

        public void setTtlInMinutes(int ttlInMinutes) {
            this.ttlInMinutes = ttlInMinutes;
        }

        public int getTtlInMinutes() {
            return ttlInMinutes;
        }

        public void setToken(String token) {
            if (token == null) {
                throw new IllegalArgumentException("token cannot be null");
            }
            this.created = new Date().getTime();
            this.token = token;
        }

        public Optional<String> getToken() {
            return Optional.ofNullable(token);
        }

        public String getTokenType() {
            return tokenType;
        }

        public void clearToken() {
            this.created = 0;
            this.ttlInMinutes = 0;
            this.token = null;
        }

        public boolean isExpired() {
            if (token == null) {
                return true;
            }

            Instant now = Instant.now();

            Instant expires = Instant.ofEpochMilli(created).plus(Duration.ofMinutes(ttlInMinutes));

            return expires.isBefore(now);
        }

        Key getKey() {
            return new Key(name, tokenType);
        }

    }

    public void put(String name, String tokenType, String token, int ttlInMinutes) {
        TokenEntry entry = new TokenEntry(name, tokenType);
        TokenEntry original = s_cache.get(entry.getKey());
        if (original == null) {
            entry.setToken(token);
            entry.setTtlInMinutes(ttlInMinutes);
            s_cache.put(entry.getKey(), entry);
        } else {
            original.setToken(token);
            original.setTtlInMinutes(ttlInMinutes);
        }
    }

    public void registerProvider(String name, String tokenType, TokenProvider tokenProvider) {
        TokenEntry entry = new TokenEntry(name, tokenType, tokenProvider);
        s_cache.put(entry.getKey(), entry);
    }

    public Optional<String> get(String name, String tokenType) {
        TokenEntry.Key key = new TokenEntry.Key(name, tokenType);
        TokenEntry entry = s_cache.get(key);
        if (entry != null) {
            if(!entry.isExpired()){
                return entry.getToken();
            } else {
                entry.clearToken();
                TokenProvider tokenProvider = entry.getTokenProvider();
                if(tokenProvider != null){
                    Optional<TokenProvider.TokenInfo> tokenInfo = tokenProvider.requestToken(entry.getName(), entry.getTokenType());
                    if(tokenInfo.isPresent()){
                        entry.setToken(tokenInfo.get().getToken());
                        entry.setTtlInMinutes(tokenInfo.get().getTtlInMinutes());
                        return entry.getToken();
                    }
                }
            }
        }
        if(!ALL_MICROSERVICES.equals(name)){
            return get(ALL_MICROSERVICES, tokenType);
        }
        return Optional.empty();
    }
}