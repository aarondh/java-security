package org.daisleyharrison.security.samples.spring.microservices.shared.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationProvider;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.GenericFilterBean;
import org.springframework.web.util.DefaultUriBuilderFactory;
import org.springframework.web.util.UriBuilder;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.daisleyharrison.security.common.exceptions.MalformedAuthClaimException;
import org.daisleyharrison.security.common.exceptions.TokenizerException;
import org.daisleyharrison.security.common.models.authorization.AuthClaims;
import org.daisleyharrison.security.common.spi.PopTokenConsumer;
import org.daisleyharrison.security.common.spi.TokenizerServiceProvider;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

public class JweTokenAuthenticationFilter extends GenericFilterBean {
    private static final Logger LOGGER = LoggerFactory.getLogger(JweTokenAuthenticationFilter.class);
    private TokenizerServiceProvider tokenizerService;
    private PopTokenConsumer popTokenConsumer;
    private JweAuthorizationProvider jweAuthorizationProvider;
    private AnonymousAuthenticationProvider aap;

    public JweTokenAuthenticationFilter(TokenizerServiceProvider tokenizerService, PopTokenConsumer popTokenConsumer,
            JweAuthorizationProvider jweAuthorizationProvider) {
        this.tokenizerService = tokenizerService;
        this.popTokenConsumer = popTokenConsumer;
        this.jweAuthorizationProvider = jweAuthorizationProvider;
        this.aap = new AnonymousAuthenticationProvider(ANONYMOUS_AUTH_KEY);
    }

    String getAuthToken(HttpServletRequest req) {
        String bearerToken = req.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7, bearerToken.length());
        }
        return null;
    }

    String getPopToken(HttpServletRequest req) {
        String bearerToken = req.getHeader("X-Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7, bearerToken.length());
        }
        return null;
    }

    private static final String ANONYMOUS_AUTH_KEY = "abcef-42";
    public static final String ROLE_ANONYMOUS = "ROLE_ANONYMOUS";
    public static final String ANONYMOUS_USER = "anonymousUser";
    private static final String SIGNED_ID_TOKEN_TYPE = "signedIdToken";
    private static final String ENCRYPTED_ID_TOKEN_TYPE = "encryptedIdToken";
    private static final String POP_TOKEN_TYPE = "PoP";

    private void setAnonymous() {
        AnonymousAuthenticationToken anonymousToken = new AnonymousAuthenticationToken(ANONYMOUS_AUTH_KEY,
                ANONYMOUS_USER, AuthorityUtils.createAuthorityList(ROLE_ANONYMOUS));
        SecurityContextHolder.getContext().setAuthentication(aap.authenticate(anonymousToken));
    }

    private Map<String, List<String>> getHeaders(HttpServletRequest req) {

        Map<String, List<String>> headers = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

        @SuppressWarnings("unchecked")
        Enumeration<String> names = req.getHeaderNames();

        while (names.hasMoreElements()) {

            String name = names.nextElement();

            @SuppressWarnings("unchecked")
            Enumeration<String> values = req.getHeaders(name);

            headers.put(name, Collections.list(values));
        }
        return headers;
    }

    private URI reconstructUri(HttpServletRequest req) {
        try {
            String path = req.getRequestURI();
            String queryString = req.getQueryString();
            if (path == null) {
                path = "/";
            }
            if (queryString == null) {
                queryString = "";
            } else {
                path += "?";
            }
            return new URI(path + queryString);
        } catch (URISyntaxException ex) {
            return null;
        }
    }

    private void validatePopToken(HttpServletRequest req, String popToken, String clientJwk)
            throws TokenizerException, IOException {
        String body;
        if (req.getContentLength() < 0) {
            body = null;
        } else {
            body = req.getReader().lines().collect(Collectors.joining());
        }

        popTokenConsumer.consumePopToken(req.getMethod(), getHeaders(req), reconstructUri(req), body, popToken,
                clientJwk);
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain filterChain)
            throws IOException, ServletException {

        String authToken = getAuthToken((HttpServletRequest) req);
        String popToken = getPopToken((HttpServletRequest) req);
        if (authToken == null) {
            setAnonymous();
        } else {
            try {
                AuthClaims authClaims = tokenizerService.consumeWebToken(ENCRYPTED_ID_TOKEN_TYPE, authToken);
                Authentication auth = jweAuthorizationProvider.getAuthentication(authClaims);
                if (auth == null) {
                    setAnonymous();
                } else {
                    if (popToken != null) {
                        validatePopToken((HttpServletRequest) req, popToken, authClaims.getStringClaimValue("cnf"));
                    }
                    SecurityContextHolder.getContext().setAuthentication(auth);
                }
            } catch (MalformedAuthClaimException | TokenizerException | IOException exception) {
                LOGGER.warn("Invalid JWE authentication token: {}", exception.getLocalizedMessage());
                setAnonymous();
            }
        }
        filterChain.doFilter(req, res);
    }

}