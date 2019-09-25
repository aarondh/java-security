package org.daisleyharrison.security.common.spi;

import org.daisleyharrison.security.common.models.openId.OpenIdServiceConfig;
import org.daisleyharrison.security.common.models.openId.OpenIdState;
import org.daisleyharrison.security.common.models.openId.CompletableFutureBuilder;
import org.daisleyharrison.security.common.models.authorization.AuthClaims;
import org.daisleyharrison.security.common.models.openId.OpenIdHeaders;
import org.daisleyharrison.security.common.models.openId.OpenIdResponse;
import org.daisleyharrison.security.common.exceptions.OpenIdException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.concurrent.CompletableFuture;

public interface OpenIdServiceProvider extends SecurityServiceProvider {

        public void setConfiguration(OpenIdServiceConfig configuration);

        public void setDomain(String domain);

        public String getDomain();

        public void setDebug(boolean debug);

        public boolean isDebug();

        public void setClientCredentials(String issuer, String clientId, char[] clientSecret);

        public CompletableFuture<Boolean> supportsEndSession(String issuer) throws OpenIdException;

        public CompletableFutureBuilder<Boolean> discover(String issuer) throws OpenIdException;

        public CompletableFutureBuilder<URI> getAuthenticationURI(String issuer)
                        throws OpenIdException, URISyntaxException;

        public CompletableFutureBuilder<URI> getEndSessionURI(String issuerName)
                        throws OpenIdException, URISyntaxException;

        public CompletableFutureBuilder<AuthClaims> requestId(String stateToken, String authenicationCode)
                        throws OpenIdException;

        public CompletableFutureBuilder<Boolean> revoke(String issuerName) throws OpenIdException;

        public CompletableFutureBuilder<Boolean> endSession(String issuerName) throws OpenIdException;

        public CompletableFuture<OpenIdState> validateStateToken(String stateToken) throws OpenIdException;

        public CompletableFutureBuilder<String> createStateToken(String issuerName) throws OpenIdException;

        public CompletableFutureBuilder<String> createBearerToken(int notBeforeInMinutes, int expiryInMinutes)
                        throws OpenIdException;

        public CompletableFuture<AuthClaims> validateBearerToken(String bearerToken) throws OpenIdException;

        public OpenIdResponse validateOpenIdResponse(OpenIdHeaders headers, URL url) throws OpenIdException;
}
