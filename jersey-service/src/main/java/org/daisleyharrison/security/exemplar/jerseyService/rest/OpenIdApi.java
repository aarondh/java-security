package org.daisleyharrison.security.samples.jerseyService.rest;

import org.daisleyharrison.security.common.exceptions.AuthorizationException;
import org.daisleyharrison.security.common.exceptions.AuthorizationFailedException;
import org.daisleyharrison.security.samples.jerseyService.models.LoginRequest;
import org.daisleyharrison.security.samples.jerseyService.models.OpenIdRequest;

import javax.ws.rs.core.Response;

import org.daisleyharrison.security.common.models.Stage;
import org.daisleyharrison.security.common.models.authorization.AuthClaims;
import org.daisleyharrison.security.common.serviceProvider.LibraryServiceProvider;
import org.daisleyharrison.security.common.spi.AuthorizationServiceProvider;
import org.daisleyharrison.security.common.spi.OpenIdServiceProvider;
import org.daisleyharrison.security.samples.jerseyService.exceptions.UnauthorizedException;
import org.daisleyharrison.security.samples.jerseyService.ControllerBase;
import org.daisleyharrison.security.samples.jerseyService.Main;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URISyntaxException;

import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.management.ServiceNotFoundException;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

/**
 * Root resource (exposed at "login" path)
 */
@Path("api/openid")
@Consumes(MediaType.APPLICATION_JSON)
public class OpenIdApi extends ControllerBase {
    private static Logger LOGGER = LoggerFactory.getLogger(OpenIdApi.class);

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

    @GET
    @RolesAllowed("Basic")
    @Path("logout")
    public Response logout(@QueryParam("redirect_uri") String redirectUri) {
        URI location;
        if (redirectUri == null) {
            return Response.ok().cookie().build();
        } else {
            try {
                location = new URI(redirectUri);
            } catch (URISyntaxException exception) {
                location = Main.getHomeUri();
            }
        }
        return Response.seeOther(location).build();
    }

    @GET
    @Path("login")
    @PermitAll
    @Produces(MediaType.TEXT_HTML)
    public Response getLogin(@QueryParam("client_id") String client_id, @QueryParam("login_hint") String login_hint,
            @QueryParam("prompt") String prompt, @QueryParam("nonce") String nonce,
            @QueryParam("redirect_uri") String redirect_uri, @QueryParam("state") String state,
            @QueryParam("scope") String scope, @QueryParam("response_mode") String response_mode,
            @QueryParam("response_type") String response_type, @QueryParam("access_type") String access_type) {
        OpenIdRequest openidRequest = new OpenIdRequest();
        openidRequest.setClient_id(client_id);
        openidRequest.setLogin_hint(login_hint);
        openidRequest.setPrompt(prompt);
        openidRequest.setNonce(nonce);
        openidRequest.setRedirect_uri(redirect_uri);
        openidRequest.setState(state);
        openidRequest.setScope(scope);
        openidRequest.setResponse_mode(response_mode);
        openidRequest.setResponse_type(response_type);
        openidRequest.setAccess_type(access_type);
        return postLogin(openidRequest);
    }

    @POST
    @Path("login")
    @PermitAll
    @Produces(MediaType.TEXT_HTML)
    public Response postLogin(OpenIdRequest openidRequest) {
        return Response.ok("Got the post login").build();
    }

    @POST
    @Path("login_request")
    @PermitAll
    @Produces(MediaType.APPLICATION_JSON)
    public Response postLoginRequest(LoginRequest loginRequest) {
        try {
            AuthClaims userClaims = this.getAuthorizationService().login(loginRequest.getEmail(),
                    loginRequest.getPassword());
            URI redirectUri = new URI(loginRequest.getRedirectUri());
            return Response.seeOther(redirectUri).build();
        } catch (URISyntaxException | ServiceNotFoundException exception) {
            throw new UnauthorizedException("Email and/or password combination not recognized.");
        } catch (AuthorizationFailedException exception) {
            throw new UnauthorizedException("Email and/or password combination not recognized.");
        } catch (AuthorizationException exception) {
            throw new UnauthorizedException("Email and/or password combination not recognized.");
        } finally {
            loginRequest.destroy();
        }
    }
}
