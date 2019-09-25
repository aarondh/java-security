package org.daisleyharrison.security.samples.jerseyService;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.daisleyharrison.security.samples.jerseyService.filters.PathOnAccessDenied;

/**
 * Root resource (exposed at "login" path)
 */
@RolesAllowed("Admin")
@PathOnAccessDenied(path = "/c2id/home")
@Path("")
public class Admin extends ControllerBase {

    @GET
    @Path("admin/{page}")
    @Produces(MediaType.TEXT_HTML)
    @PathOnAccessDenied(path = "/c2id/home")
    public String getPage(@PathParam("page") String page) {
        return template("admin/" + page, getContext());
    }

}
