package org.daisleyharrison.security.samples.spring.web.beans;

import java.util.Optional;

import javax.annotation.PostConstruct;

import org.daisleyharrison.security.common.exceptions.TokenizerException;
import org.daisleyharrison.security.common.spi.TokenizerServiceProvider;
import org.daisleyharrison.security.samples.spring.web.conversations.IdentityToken;
import org.daisleyharrison.security.samples.spring.web.models.AccessTokenRequest;
import org.daisleyharrison.security.samples.spring.web.models.AccessTokenResponse;
import org.daisleyharrison.security.samples.spring.webtalker.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClientException;

@Component
public class MicroserviceAuthenticator {
    private static final Logger LOGGER = LoggerFactory.getLogger(MicroserviceAuthenticator.class);
    private static final int DEFAULT_TTL_IN_MINUTES = 10;

    @Value("${identity.client.id}")
    private String client_id;

    @Value("${identity.client.secret}")
    private String client_secret;

    @Value("${identity.user.name}")
    private String user_name;

    @Value("${identity.user.password}")
    private String user_password;

    @Autowired
    TokenizerServiceProvider tokenizerService;

    @Autowired
    IdentityToken identityToken;

    @Autowired
    TokenManager tokenManager;

    @PostConstruct
    public void init() {
        TokenProvider tokenProvider = new TokenProvider() {

            @Override
            public Optional<TokenInfo> requestToken(String name, String tokenType) {

                try {
                    String jwk = tokenizerService.getPublicJwk("PoP");

                    AccessTokenRequest accessTokenRequest = new AccessTokenRequest();

                    accessTokenRequest.setClientId(client_id);
                    accessTokenRequest.setClientSecret(client_secret);
                    accessTokenRequest.setUsername(user_name);
                    accessTokenRequest.setPassword(user_password);
                    accessTokenRequest.setGrantType("password");
                    accessTokenRequest.setConfirmation(jwk);
                    accessTokenRequest.setScope("platform/read weakness/read");

                    AccessTokenResponse response = identityToken.talk(accessTokenRequest).block();
                    TokenInfo tokenInfo = new TokenInfo() {
                        @Override
                        public String getToken() {
                            return response.getAccessToken();
                        }

                        @Override
                        public int getTtlInMinutes() {
                            String expiresIn = response.getExpiresIn();
                            if (expiresIn == null) {
                                return DEFAULT_TTL_IN_MINUTES;
                            }
                            return Integer.parseInt(expiresIn) / 60;
                        }
                    };
                    return Optional.of(tokenInfo);
                } catch (TokenizerException | WebClientException ex) {
                    LOGGER.error("Access token request failed.", ex);
                    return Optional.empty();
                }
            }
        };
        tokenManager.registerProvider("platform-service", TokenizedWebTalker.AUTH_TOKEN_TYPE, tokenProvider);
    }

}