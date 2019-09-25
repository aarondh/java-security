package org.daisleyharrison.security.samples.spring.web.conversations;

import org.daisleyharrison.security.samples.spring.webtalker.*;
import org.daisleyharrison.security.samples.spring.web.models.AccessTokenRequest;
import org.daisleyharrison.security.samples.spring.web.models.AccessTokenResponse;

import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;

@Component
public class IdentityToken extends TokenizedWebTalker<AccessTokenRequest, AccessTokenResponse> {
    public IdentityToken() {
        super(AccessTokenResponse.class, "identity-provider-service", "api/v1/identity/token", HttpMethod.POST);
    }
}