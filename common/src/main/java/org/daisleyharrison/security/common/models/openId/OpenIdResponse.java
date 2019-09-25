package org.daisleyharrison.security.common.models.openId;

public interface OpenIdResponse {
    String getIssuer();

    String getStateToken();

    String getCode();
}