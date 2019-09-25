package org.daisleyharrison.security.samples.spring.web.conversations;

import org.daisleyharrison.security.samples.spring.webtalker.*;
import org.daisleyharrison.security.samples.spring.web.models.AuthorizationRequest;
import org.daisleyharrison.security.samples.spring.web.models.AuthorizationResponse;

import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;

@Component
public class IdentityAuthenticate extends TokenizedWebTalker<AuthorizationRequest,AuthorizationResponse> {
    public IdentityAuthenticate(){
        super(AuthorizationResponse.class, "identity-provider-service", "api/v1/identity/authenticate", HttpMethod.POST );
    }
}