package org.daisleyharrison.security.samples.spring.microservices.identityservice.models;

import java.time.Duration;
import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class AccessToken {
    private long created;
    private long ttl;
    private String token;

    public AccessToken() {

    }

    public AccessToken(String token, Duration ttl) {
        this.created = Instant.now().toEpochMilli();
        this.token = token;
        this.ttl = ttl.toMillis();
    }

    @JsonIgnore
    public boolean isExpired() {
        Instant then = Instant.ofEpochMilli(created);
        return then.plus(Duration.ofMillis(ttl)).isBefore(Instant.now());
    }

    @JsonIgnore
    public boolean isValid() {
        return !isExpired();
    }

    /**
     * @return long return the created
     */
    public long getCreated() {
        return created;
    }

    /**
     * @param created the created to set
     */
    public void setCreated(long created) {
        this.created = created;
    }

    /**
     * @return long return the ttl
     */
    public long getTtl() {
        return ttl;
    }

    /**
     * @param ttl the ttl to set
     */
    public void setTtl(long ttl) {
        this.ttl = ttl;
    }

    /**
     * @return String return the token
     */
    public String getToken() {
        return token;
    }

    /**
     * @param token the token to set
     */
    public void setToken(String token) {
        this.token = token;
    }

}