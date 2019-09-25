package org.daisleyharrison.security.samples.jerseyService;

import javax.annotation.Resource;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Resource
@Path("/fubar")
public class DefaultResource extends ControllerBase {
   private class Context {
      private String path;

      public Context(String path) {
         this.path = path;
      }
      public String getPath() {
         return this.path;
      }
   }

   @GET
   @Path("{path:.*}")
   @Produces(MediaType.TEXT_HTML)
   public String get(@PathParam("path") String inPath) {
      return template("404", new Context(inPath));
   }
}
