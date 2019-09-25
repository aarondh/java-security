package org.daisleyharrison.security.samples.jerseyService.filters;

import java.lang.reflect.Method;
import java.security.Principal;
import java.util.List;
import java.util.Map;

import javax.management.ServiceNotFoundException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.Provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.daisleyharrison.security.common.exceptions.MalformedAuthClaimException;
import org.daisleyharrison.security.common.exceptions.TokenExpiredException;
import org.daisleyharrison.security.common.exceptions.TokenizerException;
import org.daisleyharrison.security.common.models.authorization.AuthClaims;
import org.daisleyharrison.security.samples.jerseyService.Main;

/**
 * This filter verify the access permissions for a user based on username and
 * passowrd provided in request
 */
@Provider
public class AuthenticationFilter implements javax.ws.rs.container.ContainerRequestFilter {
    private static Logger LOGGER = LoggerFactory.getLogger(AuthenticationFilter.class);
    private static final String AUTHORIZATION_PROPERTY = "Authorization";
    private static final String AUTHENTICATION_SCHEME = "Bearer";

    /**
     * The security principle created if the user is allowed access
     */
    private class UserClaimsPrincipleImpl implements UserClaimsPrincipal {
        private AuthClaims userClaims;

        public UserClaimsPrincipleImpl(AuthClaims userClaims) {
            this.userClaims = userClaims;
        }

        public boolean hasRole(String role) {
            try {
                return userClaims.hasScope(role);
            } catch (MalformedAuthClaimException exception) {
                return false;
            }
        }

        @Override
        public String getName() {
            try {
                String name = userClaims.getPreferredUsername();
                if (name == null) {
                    name = userClaims.getName();
                    if (name == null) {
                        name = userClaims.getFamilyName();
                    }
                }
                return name;
            } catch (MalformedAuthClaimException exception1) {
                try {
                    return userClaims.getSubject();
                } catch (MalformedAuthClaimException exception2) {
                    throw new IllegalStateException("Malformed user claims");
                }
            }
        }

        @Override
        public AuthClaims getUserClaims() {
            return userClaims;
        }
    }

    /**
     * The security context created and passed along if the user is allowed access
     */
    private class SecurityContextImpl implements SecurityContext {
        private SecurityContext context;
        private UserClaimsPrincipleImpl userPrincipal;

        public SecurityContextImpl(SecurityContext context, AuthClaims userClaims) {
            this.userPrincipal = new UserClaimsPrincipleImpl(userClaims);
        }

        @Override
        public Principal getUserPrincipal() {
            return this.userPrincipal;
        }

        @Override
        public boolean isUserInRole(String role) {
            return userPrincipal.hasRole(role);
        }

        @Override
        public boolean isSecure() {
            return context.isSecure();
        }

        @Override
        public String getAuthenticationScheme() {
            return AUTHENTICATION_SCHEME;
        }

    }

    @Context
    private ResourceInfo resourceInfo;
    @Context
    private UriInfo uriInfo;

    /**
     * Create an response to access denied Either redirect to an accessable URI if
     * the denied method or class was annotated with @PathOnAccessDenied or
     * returning an access denied message
     * 
     * @param method the method being denied access
     * @return Response the access denied response created
     */
    private Response createAccessDeniedResponse(PermissionRequired required, boolean expired) {
        PathOnAccessDenied pathOnAccessDenied = required.getPathOnAccessDenied();
        if (pathOnAccessDenied != null && pathOnAccessDenied.path() != null) {
            String path = pathOnAccessDenied.path();
            UriBuilder uriBuilder = uriInfo.getRequestUriBuilder();
            if (path.startsWith("/")) {
                uriBuilder.replacePath(path);
            } else {
                uriBuilder.path(path);
            }
            String refersToQueryParam = pathOnAccessDenied.refersTo();
            if (refersToQueryParam != null) {
                uriBuilder.queryParam(refersToQueryParam, uriInfo.getAbsolutePath());
            }
            if (expired) {
                String expiredQueryParam = pathOnAccessDenied.expired();
                if (expiredQueryParam != null) {
                    uriBuilder.queryParam(expiredQueryParam, "true");
                }
            }
            return Response.seeOther(uriBuilder.build()).build();
        }

        return Response.status(Response.Status.UNAUTHORIZED).entity("Access denied").build();
    }

    private String toUserName(AuthClaims userClaims) {
        StringBuilder result = new StringBuilder();
        try {
            String userName = userClaims.getPreferredUsername();
            if (userName == null) {
                userName = userClaims.getName();
                if (userName == null) {
                    userName = userClaims.getFamilyName();
                    if (userName == null) {
                        userName = "**no username**";
                    }
                }
            }
            result.append(userName);
            try {
                result.append(" with role(s): ");
                userClaims.getScopes().forEach(scope -> {
                    result.append(scope);
                    result.append(" ");
                });
            } catch (MalformedAuthClaimException exception) {

            }
        } catch (MalformedAuthClaimException exception) {
            result.append("**malformed claim**");
        }

        return result.toString();
    }

    private String getBearerToken(ContainerRequestContext requestContext) {
        final MultivaluedMap<String, String> headers = requestContext.getHeaders();
        final Map<String, Cookie> cookies = requestContext.getCookies();

        // Fetch authorization header
        final List<String> authorization = headers.get(AUTHORIZATION_PROPERTY);

        // If no authorization information present; deny access
        if (authorization == null || authorization.isEmpty()) {
            // no Authorization header
            Cookie bearerTokenCookie = cookies.get("bearer-token");
            if (bearerTokenCookie != null) {
                // The bearer token is the value of the cookie
                return bearerTokenCookie.getValue();
            }
        } else {
            // Get the bearer token
            return authorization.get(0).replaceFirst(AUTHENTICATION_SCHEME + " ", "");
        }

        return null;
    }

    @Override
    public void filter(ContainerRequestContext requestContext) {

        Method method = resourceInfo.getResourceMethod();

        PermissionRequired required = PermissionRequired.getFor(method);

        String bearerToken = getBearerToken(requestContext);

        if (bearerToken == null || bearerToken.isEmpty()) {
            // there is no bearer token present this is an anonymous access request
            if (required.isPermitAll()) {
                // The @PermitAll annotation was present on the method or class so allow access
                return;
            }
            LOGGER.info("anonymous user not authorized to access path {}",
                    requestContext.getUriInfo().getAbsolutePath());
        } else {
            // There is a bearer token present: Lookup the user session based on the bearer
            // token
            try {
                AuthClaims userClaims = Main.getTokenizerService().consumeWebToken("bearer", bearerToken);
                if (userClaims == null) {
                    LOGGER.error("invalid JWE {}", bearerToken);
                } else {
                    boolean allowAccess = false;
                    // Verify user access
                    if (required.isPermitAll()) {
                        // There is no @RolesAllowed annotation so check to see if @PermitAll is present
                        allowAccess = true;
                    } else if (required.isRoleRequired()) {
                        try {
                            String[] rolesRequired = required.getRoles();
                            if (rolesRequired.length == 1 && rolesRequired[0].equals("*")) {
                                // special case of "any role"
                                allowAccess = true;
                            } else {
                                allowAccess = userClaims.hasScope(rolesRequired);
                            }
                        } catch (MalformedAuthClaimException exception) {
                            allowAccess = false;
                        }
                    }
                    if (allowAccess) {
                        // The user is allowed access; create a security context based on the user
                        // session and allow access
                        final SecurityContext securityContext = requestContext.getSecurityContext();
                        requestContext.setSecurityContext(new SecurityContextImpl(securityContext, userClaims));
                        return;
                    }
                    LOGGER.info("session {} not authorized to access path {}", toUserName(userClaims),
                            requestContext.getUriInfo().getAbsolutePath());
                }
            } catch (TokenExpiredException exception) {
                if (required.isPermitAll()) {
                    // Even with an expired token, access is allowed to this resource
                    return;
                } else {
                    // Abort by indicate that the token was legit but expired
                    requestContext.abortWith(createAccessDeniedResponse(required, true));
                    return;
                }
            } catch (TokenizerException | ServiceNotFoundException exception) {
                // The token was invalid or the tokenization service was not available;
                // either way just fall through and deny access
                LOGGER.error("token processing failed", exception);
            }
        } // end bearerToken

        // The user does not have permission to access his method
        // deny access
        requestContext.abortWith(createAccessDeniedResponse(required, false));

        return;
    }
}
