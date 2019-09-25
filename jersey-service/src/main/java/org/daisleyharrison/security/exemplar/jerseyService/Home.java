package org.daisleyharrison.security.samples.jerseyService;

import javax.annotation.security.PermitAll;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.daisleyharrison.security.samples.jerseyService.filters.PathOnAccessDenied;

/**
 * Root resource (exposed at "login" path)
 */
@PermitAll
@Path("")
public class Home extends ControllerBase {

    /**
     * Method handling HTTP GET requests. The returned object will be sent to the
     * client as "text/plain" media type.
     *
     * @return String that will be returned as a text/plain response.
     */
    @GET
    @Path("home")
    @Produces(MediaType.TEXT_HTML)
    public String getHome() {
        return template("home", getContext());
    }

    @GET
    @PermitAll
    @Path("home/{page}")
    @Produces(MediaType.TEXT_HTML)
    @PathOnAccessDenied(path = "/c2id/home")
    public String getPage(@PathParam("page") String page) {
        return template("home/" + page, getContext());
    }

}
