package org.daisleyharrison.security.common.models.openId;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.daisleyharrison.security.common.exceptions.OpenIdException;

public interface CompletableFutureBuilder<T> {
    public CompletableFutureBuilder<T> setProperty(String propertyName, String propertyValue);

    public String getProperty(String propertyName);

    public CompletableFutureBuilder<T> setProperties(Map<String, String> properties);

    public Map<String, String> getProperties();

    public CompletableFutureBuilder<T> setState(String state);

    public String getState();

    public CompletableFuture<T> build() throws OpenIdException;
}
