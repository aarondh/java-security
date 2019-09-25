package org.daisleyharrison.security.services.poptoken;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.Base64.Encoder;
import java.util.stream.Collectors;

import javax.management.ServiceNotFoundException;

import org.daisleyharrison.security.common.exceptions.InvalidPopTokenException;
import org.daisleyharrison.security.common.exceptions.MalformedAuthClaimException;
import org.daisleyharrison.security.common.exceptions.TokenizerException;
import org.daisleyharrison.security.common.models.authorization.AuthClaims;
import org.daisleyharrison.security.common.serviceProvider.LibraryServiceProvider;
import org.daisleyharrison.security.common.spi.PopTokenConsumer;
import org.daisleyharrison.security.common.spi.PopTokenProducer;
import org.daisleyharrison.security.common.spi.TokenizerServiceProvider;
import org.daisleyharrison.security.common.utilities.AuthClaimsImpl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PopTokenFactory {
    public static final Logger LOGGER = LoggerFactory.getLogger(PopTokenFactory.class);
    public static final LibraryServiceProvider _serviceProvider = LibraryServiceProvider.getInstance();
    public static final String POP_TOKEN_TYPE = "PoP";
    private static final Encoder encoder = Base64.getEncoder();
    public static final String AUTHORIZATION_COMPONENT = "Authorization";
    public static final String X_AUTH_ORIGINATOR_COMPONENT = "X-Auth-Originator";
    public static final String URI_COMPONENT = "Uri";
    public static final String BODY_COMPONENT = "Body";
    public static final List<String> DEFAULT_HASH_COMPONENTS = Collections.unmodifiableList(
            Arrays.asList(AUTHORIZATION_COMPONENT, X_AUTH_ORIGINATOR_COMPONENT, URI_COMPONENT, BODY_COMPONENT));

    public static class Taap {
        public static class ClaimNames {
            public static final String EXTERNAL_DATA_TO_SIGN = "edts";
            public static final String EXTERNAL_HEADERS_TO_SIGN = "ehts";
            public static final String VERSION = "v";
        }

    }

    private MessageDigest digest;
    private List<String> components;
    private TokenizerServiceProvider tokenizerService;

    public PopTokenFactory(String... components) throws NoSuchAlgorithmException, ServiceNotFoundException {
        this();

        if (components == null) {
            throw new IllegalArgumentException("You must supply at least one component");
        }

        this.components = Arrays.asList(components);

    }

    public PopTokenFactory() throws NoSuchAlgorithmException, ServiceNotFoundException {
        this.components = DEFAULT_HASH_COMPONENTS;
        this.digest = MessageDigest.getInstance("SHA-256");
        this.tokenizerService = _serviceProvider.provideService(TokenizerServiceProvider.class);
    }

    private Map<String, List<String>> splitQuery(URI uri) {
        return Arrays.stream(uri.getQuery().split("&")).map(this::splitQueryParameter)
                .collect(Collectors.groupingBy(SimpleImmutableEntry::getKey, LinkedHashMap::new,
                        Collectors.mapping(Map.Entry::getValue, Collectors.toList())));
    }

    private SimpleImmutableEntry<String, String> splitQueryParameter(String pair) {
        try {
            final int idx = pair.indexOf("=");
            final String key = idx > 0 ? pair.substring(0, idx) : pair;
            final String value = idx > 0 && pair.length() > idx + 1
                    ? URLDecoder.decode(pair.substring(idx + 1), "UTF-8")
                    : null;
            return new SimpleImmutableEntry<>(key, value);
        } catch (UnsupportedEncodingException ex) {
            throw new IllegalArgumentException("bad encoding");
        }
    }

    private String toCononicalUri(URI uri) {
        return uri.getQuery() == null ? uri.getPath() : uri.getPath() + "?";
    }

    private String toCononicalQueryString(URI uri) {
        Map<String, List<String>> paramMap = splitQuery(uri);
        String cononicalQueryString = paramMap.entrySet().stream().sorted(Map.Entry.comparingByKey()).map(
                e -> e.getValue().stream().sorted().map(v -> e.getKey() + "=" + v).collect(Collectors.joining("&")))
                .collect(Collectors.joining("&"));
        return cononicalQueryString;
    }

    private String base64url(byte[] bytes) {
        StringBuilder base64url = new StringBuilder();
        String base64Encoded = encoder.encodeToString(bytes);
        for (char c : base64Encoded.toCharArray()) {
            switch (c) {
            case '+':
                base64url.append('-');
                break;
            case '/':
                base64url.append('_');
                break;
            default:
                base64url.append(c);
                break;
            }
        }

        return base64url.toString();
    }

    private byte[] sha256(String text) {
        return digest.digest(text.getBytes(StandardCharsets.UTF_8));
    }

    private interface PopClaims {
        String getEdts();

        String getEhts();
    }

    private List<String> toEhtsList(String ehts) {
        if (ehts == null) {
            return DEFAULT_HASH_COMPONENTS;
        } else {
            return Arrays.asList(ehts.split("; ?"));
        }
    }

    private PopClaims buildHash(List<String> components, String method, Map<String, List<String>> headers, URI uri,
            String body) {
        List<String> used = new ArrayList<>();
        StringBuilder hashSource = new StringBuilder();
        for (String component : components) {
            boolean componentUsed = false;
            switch (component) {
            case URI_COMPONENT:
                hashSource.append(toCononicalUri(uri));
                componentUsed = true;
                break;
            case BODY_COMPONENT:
                if (uri.getQuery() != null) {
                    hashSource.append(toCononicalQueryString(uri));
                    componentUsed = true;
                }
                if (body != null) {
                    hashSource.append(base64url(sha256(body)));
                    componentUsed = true;
                }
                break;
            default:
                List<String> values = headers.get(component);
                if (values != null && !values.isEmpty()) {
                    hashSource.append(values.get(0));
                    componentUsed = true;
                }
                break;
            }
            if (componentUsed) {
                used.add(component);
            }
        }

        return new PopClaims() {

            @Override
            public String getEdts() {
                return base64url(sha256(hashSource.toString()));
            }

            @Override
            public String getEhts() {
                return String.join("; ", used);
            }
        };

    }

    public PopTokenConsumer getConsumer() {
        return new PopTokenConsumer() {

            @Override
            public void consumePopToken(String method, Map<String, List<String>> headers, URI uri, String body,
                    String token, String jwk) throws InvalidPopTokenException {
                try {
                    AuthClaims claims;
                    try {
                        claims = tokenizerService.consumeWebTokenWithJwk(POP_TOKEN_TYPE, token, jwk);
                    } catch (TokenizerException ex) {
                        LOGGER.error("Unable to consume PoP token", ex);
                        throw new InvalidPopTokenException();
                    }

                    String version = claims.getStringClaimValue(Taap.ClaimNames.VERSION);
                    if (!"1".equals(version)) {
                        LOGGER.error("Unable to consume PoP token (Pop token version \"{}\" not supported)", version);
                        throw new InvalidPopTokenException();
                    }

                    String ehts = claims.getStringClaimValue(Taap.ClaimNames.EXTERNAL_HEADERS_TO_SIGN);

                    String edts = claims.getStringClaimValue(Taap.ClaimNames.EXTERNAL_DATA_TO_SIGN);

                    PopClaims popClaims = buildHash(toEhtsList(ehts), method, headers, uri, body);

                    if (!popClaims.getEdts().equals(edts)) {
                        throw new InvalidPopTokenException();
                    }

                } catch (MalformedAuthClaimException ex) {
                    LOGGER.error("Unable to consume PoP token", ex);
                    throw new InvalidPopTokenException();
                }
            }
        };
    }

    public PopTokenProducer getProducer() {
        return new PopTokenProducer() {

            @Override
            public Optional<String> producePopToken(String method, Map<String, List<String>> headers, URI uri,
                    String body) {
                try {
                    PopClaims popClaims = buildHash(components, method, headers, uri, body);

                    AuthClaims claims = new AuthClaimsImpl();
                    claims.setClaim(Taap.ClaimNames.EXTERNAL_HEADERS_TO_SIGN, popClaims.getEhts());
                    claims.setClaim(Taap.ClaimNames.EXTERNAL_DATA_TO_SIGN, popClaims.getEdts());
                    claims.setClaim(Taap.ClaimNames.VERSION, "1");

                    String token = tokenizerService.produceWebToken(POP_TOKEN_TYPE, claims.getClaimsMap());

                    return Optional.of(token);
                } catch (TokenizerException ex) {
                    LOGGER.error("Unable to produce PoP token", ex);
                    return Optional.empty();
                }
            }
        };

    }
}