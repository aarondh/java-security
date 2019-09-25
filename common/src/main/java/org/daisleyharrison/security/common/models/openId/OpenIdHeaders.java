package org.daisleyharrison.security.common.models.openId;

public interface OpenIdHeaders {
    public Iterable<String> names();

    public String getHeader(String name);
}