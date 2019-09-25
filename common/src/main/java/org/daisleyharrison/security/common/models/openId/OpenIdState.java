package org.daisleyharrison.security.common.models.openId;

public interface OpenIdState {
    public String getIssuer();

    public String getState();
}