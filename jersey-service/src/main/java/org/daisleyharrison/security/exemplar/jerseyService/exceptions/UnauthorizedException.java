package org.daisleyharrison.security.samples.jerseyService.exceptions;

import javax.ws.rs.*;
import javax.ws.rs.core.*;
import javax.ws.rs.core.Response.*;


public class UnauthorizedException extends WebApplicationException {
    private static final long serialVersionUID = -6329122801197186821L;

    public UnauthorizedException() {
         super(Response.status(Status.UNAUTHORIZED).build());
     }
     public UnauthorizedException(String message) {
         super(Response.status(Status.UNAUTHORIZED).entity(message).type("text/plain").build());
     }

}