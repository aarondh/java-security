package org.daisleyharrison.security.samples.jerseyService.filters;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.security.Principal;
import java.time.Duration;
import java.util.List;

import javax.management.ServiceNotFoundException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.Provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.daisleyharrison.security.common.exceptions.MalformedAuthClaimException;
import org.daisleyharrison.security.common.exceptions.NonceReplayException;
import org.daisleyharrison.security.common.exceptions.TokenizerException;
import org.daisleyharrison.security.common.spi.TokenizerServiceProvider;
import org.daisleyharrison.security.samples.jerseyService.Main;
import org.glassfish.jersey.message.internal.ReaderWriter;
import org.glassfish.jersey.server.ContainerException;

/**
 * This filter checks against replay attacks if a nonce query param is present
 * and rejects requests to paths marked with @NonceRequired and no nonce query
 * parameter was detected
 */
@Provider
public class NonceFilter implements javax.ws.rs.container.ContainerRequestFilter {
    private static Logger LOGGER = LoggerFactory.getLogger(NonceFilter.class);
    private static final String NONCE_QUERY_PARAM_NAME = "nonce";
    private static final String HASH_QUERY_PARAM_NAME = "hash";

    @Context
    private ResourceInfo resourceInfo;

    @Context
    private UriInfo uriInfo;

    private class NonceParams {
        private String nonce;
        private String hash;

        public NonceParams(UriInfo uriInfo) {
            MultivaluedMap<String, String> queryParams = uriInfo.getQueryParameters();

            List<String> nonceList = queryParams.get(NONCE_QUERY_PARAM_NAME);
            if (nonceList != null && !nonceList.isEmpty()) {
                nonce = nonceList.get(0);
            }

            List<String> hashList = queryParams.get(HASH_QUERY_PARAM_NAME);
            if (hashList != null && !hashList.isEmpty()) {
                hash = hashList.get(0);
            }
        }

        public String getNonce() {
            return nonce;
        }

        public String getHash() {
            return hash;
        }

        public boolean hasNonce() {
            return nonce != null;
        }

        public boolean hasHash() {
            return hash != null;
        }
    }

    /**
     * get the posted request content body from the request context then setup a new
     * entity stream so downstream methods can also access the content
     * 
     * @param requestContext
     * @return a string representation of the content
     */
    private String getRequestContent(ContainerRequestContext requestContext) {
        try {
            String content = null;
            Charset charset = Charset.forName("UTF-8");
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            InputStream input = requestContext.getEntityStream();
            if (input.available() > 0) {
                ReaderWriter.writeTo(input, output);

                byte[] requestEntity = output.toByteArray();

                content = new String(requestEntity, charset);

                requestContext.setEntityStream(new ByteArrayInputStream(requestEntity));
            }
            return content;
        } catch (

        IOException exception) {
            throw new ContainerException(exception);
        }
    }

    @Override
    public void filter(ContainerRequestContext requestContext) {

        Method method = resourceInfo.getResourceMethod();

        PermissionRequired required = PermissionRequired.getFor(method);

        NonceParams params = new NonceParams(requestContext.getUriInfo());

        if (params.hasNonce()) {
            try {
                TokenizerServiceProvider tokenizer = Main.getTokenizerService();

                // At this point a nonce was detected in the request uri
                // Ensure that the nonce is used only once
                tokenizer.consumeNonce("client", params.getNonce(), Duration.ofHours(2));

                if (params.hasHash()) {
                    UserClaimsPrincipal userClaimsPrincipal = null;
                    SecurityContext securityContext = requestContext.getSecurityContext();
                    if (securityContext != null) {
                        Principal principal = securityContext.getUserPrincipal();
                        if (principal instanceof UserClaimsPrincipal) {
                            userClaimsPrincipal = (UserClaimsPrincipal) principal;
                        }
                    }
                    if (userClaimsPrincipal == null) {
                        requestContext
                                .abortWith(Response.status(401, "No security context for nonce validation").build());
                        return;
                    }
                    String subject = userClaimsPrincipal.getUserClaims().getSubject();
                    String url = requestContext.getUriInfo().toString();
                    String content = getRequestContent(requestContext);
                    String payload = url + content;
                    tokenizer.consumeNonce("nonce", subject, params.getNonce(), payload, params.getHash());
                }

            } catch (NonceReplayException exception) {
                requestContext.abortWith(Response.status(401, "Replay detected").build());
            } catch (MalformedAuthClaimException | TokenizerException | ServiceNotFoundException exception) {
                requestContext.abortWith(Response.status(500).build());
            }
        } else if (required.isNonceRequired()) {
            requestContext.abortWith(Response.status(401, "Cannot determine replay").build());
            LOGGER.info("request rejected. Path {} requires a nonce.", uriInfo.toString());
        }
        return;
    }
}
