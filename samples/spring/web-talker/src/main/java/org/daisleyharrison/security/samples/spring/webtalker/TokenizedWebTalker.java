package org.daisleyharrison.security.samples.spring.webtalker;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.daisleyharrison.security.common.spi.PopTokenProducer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.web.util.DefaultUriBuilderFactory;
import org.springframework.web.util.UriBuilder;

public class TokenizedWebTalker<D, R> extends WebTalker<D, R> {
    private static final Logger LOGGER = LoggerFactory.getLogger(WebTalker.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static final String POP_TOKEN_TYPE = "PoP";

    public static final String AUTH_TOKEN_TYPE = "Auth";

    private static final String BEARER_TOKEN_PREFIX = "Bearer ";

    private static final String HEADERS_X_AUTHORIZATION = "X-" + HttpHeaders.AUTHORIZATION;

    private boolean authorizationRequired;
    private boolean popRequired;

    @Autowired
    TokenManager tokenManager;

    @Autowired
    PopTokenProducer popTokenProducer;

    public TokenizedWebTalker(Class<R> resultType) {
        super(resultType);
    }

    public TokenizedWebTalker(Class<R> resultType, String microserviceName, String serviceEndPoint, HttpMethod method) {
        super(resultType, microserviceName, serviceEndPoint, method);
    }

    @Override
    protected void buildHeaders(HttpHeaders headers, Function<UriBuilder, URI> produceUri, D data) {
        super.buildHeaders(headers, produceUri, data);
        if (isPopRequired()) {
            boolean successful = false;
            Optional<String> token = Optional.empty();
            UriBuilder uriBuilder = new DefaultUriBuilderFactory(getMicroserviceUrl().get()).builder();
            URI uri = produceUri.apply(uriBuilder);
            String method = getMethod().toString();
            String body = null;

            if (data == null) {
                successful = true;
            } else {
                try {
                    body = objectMapper.writeValueAsString(data);
                    successful = true;
                } catch (JsonProcessingException ex) {
                    LOGGER.error("Failed to serialize {} for PoP token creation: {}", data.getClass().getName(),
                            ex.getMessage());
                }
            }

            if (successful) {
                successful = false;
                token = popTokenProducer.producePopToken(method, headers, uri, body);
                successful = token.isPresent();
            }

            if (successful) {
                headers.add(HEADERS_X_AUTHORIZATION, BEARER_TOKEN_PREFIX + token.get());
            } else {
                String message = "Failed to create PoP token for microservice " + getMicroserviceName();
                LOGGER.error(message);
                throw new SecurityException(message);
            }
        }
    }

    public boolean isPopRequired() {
        return popRequired;
    }

    public void setPopRequired(boolean popRequired) {
        this.popRequired = popRequired;
    }

    public boolean isAuthenticationRequired() {
        return authorizationRequired;
    }

    public void setAuthenticationRequired(boolean authenticationRequired) {
        this.authorizationRequired = authenticationRequired;
    }

    @WebTalkerParameter(value = HttpHeaders.AUTHORIZATION, header = true)
    public String getAuthorizationHeader() throws SecurityException {
        if (authorizationRequired) {
            Optional<String> token = tokenManager.get(getMicroserviceName(), AUTH_TOKEN_TYPE);
            if (token.isPresent()) {
                return BEARER_TOKEN_PREFIX + token.get();
            }
            String message = "Authentication token not found for microservice \"" + getMicroserviceName() + "\"";
            LOGGER.error(message);
            throw new SecurityException(message);
        }
        return null;
    }

}