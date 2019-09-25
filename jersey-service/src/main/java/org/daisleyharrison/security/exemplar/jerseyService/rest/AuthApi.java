package org.daisleyharrison.security.samples.jerseyService.rest;

import org.daisleyharrison.security.common.utilities.AuthClaimsImpl;
import org.daisleyharrison.security.common.exceptions.AuthorizationException;
import org.daisleyharrison.security.common.exceptions.AuthorizationFailedException;
import org.daisleyharrison.security.common.exceptions.AuthorizationLogoutFailedException;
import org.daisleyharrison.security.common.exceptions.MalformedAuthClaimException;
import org.daisleyharrison.security.common.exceptions.OpenIdException;
import org.daisleyharrison.security.common.exceptions.TokenizerException;
import org.daisleyharrison.security.samples.jerseyService.models.CheckEmailRequest;
import org.daisleyharrison.security.samples.jerseyService.models.CheckEmailResponse;
import org.daisleyharrison.security.samples.jerseyService.models.LoginRequest;
import org.daisleyharrison.security.samples.jerseyService.models.LoginResponse;
import org.daisleyharrison.security.samples.jerseyService.models.NonceRequest;
import org.daisleyharrison.security.samples.jerseyService.models.NonceResponse;
import org.glassfish.grizzly.http.server.Request;
import org.glassfish.grizzly.http.util.MimeHeaders;

import javax.ws.rs.core.Response;

import org.daisleyharrison.security.common.models.Stage;
import org.daisleyharrison.security.common.models.authorization.AuthClaims;
import org.daisleyharrison.security.common.models.authorization.OpenIdClaims;
import org.daisleyharrison.security.common.models.openId.OpenIdHeaders;
import org.daisleyharrison.security.common.models.openId.OpenIdResponse;
import org.daisleyharrison.security.common.models.profile.Profile;
import org.daisleyharrison.security.common.serviceProvider.LibraryServiceProvider;
import org.daisleyharrison.security.common.spi.AuthorizationServiceProvider;
import org.daisleyharrison.security.common.spi.OpenIdServiceProvider;
import org.daisleyharrison.security.samples.jerseyService.exceptions.UnauthorizedException;
import org.daisleyharrison.security.samples.jerseyService.filters.CrossOrigin;
import org.daisleyharrison.security.samples.jerseyService.ControllerBase;
import org.daisleyharrison.security.samples.jerseyService.Main;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletionException;

import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.management.ServiceNotFoundException;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.NewCookie;

/**
 * Root resource (exposed at "login" path)
 */
@Path("api/auth")
@Consumes(MediaType.APPLICATION_JSON)
public class AuthApi extends ControllerBase {
    private static Logger LOGGER = LoggerFactory.getLogger(AuthApi.class);

    private static LibraryServiceProvider _serviceProvider = LibraryServiceProvider.getInstance();

    private AuthorizationServiceProvider getAuthorizationService() throws ServiceNotFoundException {
        AuthorizationServiceProvider authorizationService = _serviceProvider.provideService(AuthorizationServiceProvider.class);
        if (!authorizationService.isInitialized()) {
            try (Stage stage = authorizationService.beginInitialize()) {
                authorizationService.configure();
            } catch (Exception exception) {
                LOGGER.error("authorization-service failed to initialize", exception);
                throw new ServiceNotFoundException("authorization-service failed to initialize");
            }
        }
        return authorizationService;
    }

    private OpenIdServiceProvider getOpenIdService() throws ServiceNotFoundException {
        return Main.getOpenIdService();
    }

    @POST
    @Path("openid_login")
    @PermitAll
    @CrossOrigin
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response openid_login(@FormParam("issuer") String issuer, @FormParam("email") String email,
            @FormParam("redirectUri") String redirectUri) {
        try {
            URI authenticationUri = this.getOpenIdService().getAuthenticationURI(issuer)
                    .setProperty("redirect_uri", "http://localhost:3000/c2id/api/auth/openid_response")
                    .setState(redirectUri).setProperty("login_hint", email).setProperty("prompt", "login").build()
                    .join();
            return Response.seeOther(authenticationUri).build();
        } catch (IllegalArgumentException exception) {
            throw new WebApplicationException("login failed: " + exception.getMessage());
        } catch (ServiceNotFoundException | URISyntaxException | OpenIdException exception) {
            throw new UnauthorizedException("login failed");
        }
    }

    private AuthClaims lookupLocalAccount(String issuer, AuthClaims openidClaims)
            throws ServiceNotFoundException, AuthorizationException {
        AuthClaims userClaims;
        String domain = getAuthorizationService().getDomain();
        try {
            userClaims = getAuthorizationService().loginByUserClaims(issuer, openidClaims);
        } catch (AuthorizationFailedException exception) {
            // set up claims for a new user
            userClaims = new AuthClaimsImpl();
            userClaims.setIssuer(domain);
            userClaims.addAudience(domain);
            userClaims.setSubject("NewUser");
            userClaims.setGivenName(openidClaims.getGivenName());
            userClaims.setFamilyName(openidClaims.getFamilyName());
            userClaims.setEmail(openidClaims.getEmail());
            userClaims.setIdTokenIssuer(openidClaims.getIssuer());
            userClaims.setIdToken(openidClaims);
            List<String> roles = new ArrayList<>();
            roles.add("NewUser");
            userClaims.setClaim(OpenIdClaims.ReservedClaims.SCOPE, roles);
        }
        String initialUri = userClaims.getStringClaimValue("state");
        if (initialUri == null) {
            initialUri = Main.getHomeUri().toString();
        }
        userClaims.setClaim(AuthClaims.PrivateClaims.INITIAL_URI, initialUri);

        return userClaims;

    }

    private String getInitialUri(AuthClaims userClaims) {
        try {
            return userClaims.getInitialUri();
        } catch (MalformedAuthClaimException exception) {
            return null;
        }
    }

    /**
     * Create a cookie to hold the bearer token
     * 
     * @param bearerToken the bearer token to be "cookieized"
     * @return NewCookie containing the bearer token
     */
    private NewCookie createBearTokenCookie(String bearerToken, Date expires) {
        // TBD: NOTE this cookie should be a secure cookie (unsecured for testing only)
        URI baseUri = Main.getBaseUri();
        LocalDateTime expiresAt = expires.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        int expiresInSeconds = (int) Duration.between(LocalDateTime.now(), expiresAt).toSeconds();
        return new NewCookie("bearer-token", bearerToken, baseUri.getPath(), null, 1, null, expiresInSeconds, false);
    }

    private NewCookie deleteBearTokenCookie() {
        URI baseUri = Main.getBaseUri();
        return new NewCookie("bearer-token", null, baseUri.getPath(), null, null, 0, false, true);
    }

    private class OpenIdHeadersImpl implements OpenIdHeaders {
        private MimeHeaders mimeHeaders;

        public OpenIdHeadersImpl(MimeHeaders mimeHeaders) {
            this.mimeHeaders = mimeHeaders;
        }

        @Override
        public Iterable<String> names() {
            return this.mimeHeaders.names();
        }

        @Override
        public String getHeader(String name) {
            return this.mimeHeaders.getHeader(name);
        }
    }

    @GET
    @PermitAll
    @Path("openid_response")
    public Response get_openid_response(@Context Request request) {
        try {
            String url = request.getRequestURL().toString();
            String queryString = request.getQueryString();
            URL fullUrl = new URL(url + "?" + queryString);
            OpenIdHeaders headers = new OpenIdHeadersImpl(request.getRequest().getHeaders());
            OpenIdResponse response = this.getOpenIdService().validateOpenIdResponse(headers, fullUrl);
            AuthClaims openidClaims = this.getOpenIdService().requestId(response.getStateToken(), response.getCode())
                    .setProperty("redirect_uri", "http://localhost:3000/c2id/api/auth/openid_response").build().join();

            AuthClaims userClaims = lookupLocalAccount(response.getIssuer(), openidClaims);
            String initialUri = getInitialUri(userClaims);
            String token = getTokenizerService().produceWebToken("bearer", userClaims.getClaimsMap());
            NewCookie bearerToken = createBearTokenCookie(token, userClaims.getExpirationTime());
            // return templateResponseBuilder("openid_response",
            // session).cookie(bearerToken).build();
            return Response.seeOther(new URI(initialUri)).cookie(bearerToken).build();
        } catch (AuthorizationException | ServiceNotFoundException | OpenIdException | MalformedURLException
                | IllegalArgumentException | URISyntaxException | CompletionException | TokenizerException exception) {
            LOGGER.info("OpenId failed: " + exception.getMessage());
            throw new WebApplicationException("login failed: " + exception.getMessage());
        }
    }

    @POST
    @RolesAllowed("NewUser")
    @Path("new_user")
    public Response post_new_user(@Context Request request, Profile profile) {
        try {
            AuthClaims newUserClaims = getUserClaims();
            AuthClaims userClaims = getAuthorizationService().createAccount(newUserClaims);

            return Response.ok().entity(userClaims).build();
        } catch (Exception exception) {
            return errorResponse(exception);
        }
    }

    @POST
    @PermitAll
    @Path("openid_response")
    public Response post_openid_response(@Context Request request, String x) {
        try {
            String url = request.getRequestURL().toString();
            String queryString = request.getQueryString();
            URL fullUrl = new URL(url + "?" + queryString);
            OpenIdHeaders headers = new OpenIdHeadersImpl(request.getRequest().getHeaders());
            OpenIdResponse response = this.getOpenIdService().validateOpenIdResponse(headers, fullUrl);
            AuthClaims openidClaims = this.getOpenIdService().requestId(response.getStateToken(), response.getCode())
                    .setProperty("redirect_uri", "http://localhost:3000/c2id/api/auth/openid_response").build().join();

            AuthClaims userClaims = lookupLocalAccount(response.getIssuer(), openidClaims);
            String initialUri = getInitialUri(userClaims);
            String token = getTokenizerService().produceWebToken("bearer", userClaims.getClaimsMap());
            NewCookie bearerToken = createBearTokenCookie(token, userClaims.getExpirationTime());
            if (initialUri == null) {
                return templateResponseBuilder("openid_response", userClaims).cookie(bearerToken).build();
            } else {
                return Response.seeOther(new URI(initialUri)).cookie(bearerToken).build();
            }
        } catch (AuthorizationException | ServiceNotFoundException | OpenIdException | MalformedURLException
                | IllegalArgumentException | URISyntaxException | CompletionException | TokenizerException exception) {
            LOGGER.info("OpenId failed: " + exception.getMessage());
            throw new WebApplicationException("login failed: " + exception.getMessage());
        }
    }

    @GET
    @Path("openid_logout")
    @RolesAllowed("Basic")
    @CrossOrigin
    public Response openid_logout() {
        try {
            String issuer = getUserClaims().getIssuer();
            URI endSessionUri = this.getOpenIdService().getEndSessionURI(issuer)
                    .setProperty("redirect_uri", "http://localhost:3000/c2id/loggedOut").build().join();
            return Response.seeOther(endSessionUri).cookie(deleteBearTokenCookie()).build();
        } catch (IllegalArgumentException exception) {
            throw new WebApplicationException("logout failed: " + exception.getMessage());
        } catch (MalformedAuthClaimException | ServiceNotFoundException | URISyntaxException
                | OpenIdException exception) {
            throw new UnauthorizedException("login failed");
        }
    }

    @GET
    @PermitAll
    @Path("logout")
    public Response logout(@QueryParam("see") String redirectUri) {
        URI location;
        if (redirectUri == null) {
            location = Main.getHomeUri();
        } else {
            try {
                location = new URI(redirectUri);
            } catch (URISyntaxException exception) {
                location = Main.getHomeUri();
            }
        }
        AuthClaims userClaims = getUserClaims();
        if (userClaims != null) {
            try {
                this.getAuthorizationService().logout(userClaims);
            } catch (AuthorizationLogoutFailedException exception) {
                LOGGER.error("Error during logout", exception);
                // allow the logout to fall through and remove the cookie from the client
            } catch (AuthorizationException | ServiceNotFoundException exception) {
                LOGGER.error("Error during logout", exception);
            }
        }

        return Response.seeOther(location).cookie(deleteBearTokenCookie()).build();
    }

    @POST
    @Path("checkemail")
    @PermitAll
    @Produces(MediaType.APPLICATION_JSON)
    public Response checkEmail(CheckEmailRequest checkEmail) {
        try {
            String issuer = this.getAuthorizationService().getIssuerForEmail(checkEmail.getEmail());
            return Response.ok().entity(new CheckEmailResponse(issuer)).build();
        } catch (AuthorizationException | ServiceNotFoundException exception) {
            LOGGER.error("Could not check email", exception);
            return Response.status(404).build();
        }
    }

    @POST
    @Path("login")
    @PermitAll
    @Produces(MediaType.APPLICATION_JSON)
    public Response login(LoginRequest loginRequest) {
        try {
            AuthClaims userClaims = this.getAuthorizationService().login(loginRequest.getEmail(),
                    loginRequest.getPassword());
            String bearerToken = this.getTokenizerService().produceWebToken("bearer", userClaims.getClaimsMap());
            String bearerTokenCookie = createBearTokenCookie(bearerToken, userClaims.getExpirationTime()).toString();
            LoginResponse loginResponse = new LoginResponse(userClaims, bearerToken, bearerTokenCookie,
                    loginRequest.getRedirectUri());
            if (loginRequest.isRedirect()) {
                URI redirectUri = new URI(loginRequest.getRedirectUri());
                return Response.seeOther(redirectUri)
                        .cookie(createBearTokenCookie(bearerToken, userClaims.getExpirationTime())).build();
            } else {
                return Response.ok().entity(loginResponse).build();
            }
        } catch (TokenizerException | URISyntaxException | ServiceNotFoundException exception) {
            throw new UnauthorizedException("Email and/or password combination not recognized.");
        } catch (AuthorizationFailedException exception) {
            throw new UnauthorizedException("Email and/or password combination not recognized.");
        } catch (AuthorizationException exception) {
            throw new UnauthorizedException("Email and/or password combination not recognized.");
        } finally {
            loginRequest.destroy();
        }
    }

    @POST
    @Path("nonce")
    @RolesAllowed("*")
    @Produces(MediaType.APPLICATION_JSON)
    public Response postRequestNonce(NonceRequest request) {
        try {
            AuthClaims userClaims = getUserClaims();
            String id = userClaims.getSubject();
            Date tokenExpires = userClaims.getExpirationTime();
            Duration expires = Duration.between(new Date().toInstant(), tokenExpires.toInstant());
            expires.plus(Duration.ofMinutes(15));
            String nonce = getTokenizerService().produceNonce("server", id, expires);
            return Response.ok(new NonceResponse(nonce)).build();
        } catch (MalformedAuthClaimException | TokenizerException | ServiceNotFoundException exception) {
            return Response.status(401).build();
        }
    }
}
