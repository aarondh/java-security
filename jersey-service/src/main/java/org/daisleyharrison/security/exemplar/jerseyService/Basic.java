package org.daisleyharrison.security.samples.jerseyService;

import java.util.Optional;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.daisleyharrison.security.common.models.PagedCollection;
import org.daisleyharrison.security.common.models.authorization.AuthClaims;
import org.daisleyharrison.security.common.models.profile.Profile;
import org.daisleyharrison.security.common.models.profile.ProfileActivity;
import org.daisleyharrison.security.samples.jerseyService.filters.PathOnAccessDenied;
import org.daisleyharrison.security.samples.jerseyService.models.ViewContext;

/**
 * Root resource (exposed at "login" path)
 */
@RolesAllowed("Basic")
@PathOnAccessDenied(path = "/c2id/home")
@Path("")
public class Basic extends ControllerBase {
    private static final int DEFAULT_PAGE_SIZE = 32;

    @Context
    public UriInfo uriInfo;

    public class ProfileContext extends ViewContext {
        private Profile profile;
        private PagedCollection<ProfileActivity> activities;

        public ProfileContext(AuthClaims userClaims, String currentUri) {
            super(userClaims, currentUri);
        }

        /**
         * @return Profile return the profile
         */
        public Profile getProfile() {
            return profile;
        }

        /**
         * @param profile the profile to set
         */
        public void setProfile(Profile profile) {
            this.profile = profile;
        }

        public void setActivities(PagedCollection<ProfileActivity> activities) {
            this.activities = activities;
        }

        /**
         * @return PagedCollection<ProfileActivity> return the activity
         */
        public PagedCollection<ProfileActivity> getActivities() {
            return activities;
        }
    }

    @Override
    protected ProfileContext getContext() {
        return new ProfileContext(this.getUserClaims(), this.uriInfo.getPath());
    }

    private Response pageForCurrentProfile(String templateName) {
        try {
            Optional<Profile> profile = getProfileService().read(getUserClaims().getSubject());
            if (profile.isPresent()) {
                ProfileContext context = getContext();
                context.setProfile(profile.get());
                return templateResponseBuilder(templateName, context).build();
            } else {
                return errorResponse(404, "profile not found");
            }
        } catch (Exception exception) {
            return errorResponse(exception);
        }
    }

    private Response pageForCurrentProfileActivity(String templateName, int pageNumber, int pageSize) {
        try {
            Optional<Profile> profile = getProfileService().read(getUserClaims().getSubject());
            if (profile.isPresent()) {
                ProfileContext context = getContext();
                context.setProfile(profile.get());
                PagedCollection<ProfileActivity> activities = getProfileService()
                        .listActivity(getUserClaims().getSubject(), pageNumber, pageSize);
                context.setActivities(activities);
                return templateResponseBuilder(templateName, context).build();
            } else {
                return errorResponse(404, "profile not found");
            }
        } catch (Exception exception) {
            return errorResponse(exception);
        }
    }

    @GET
    @Path("basic/profile/edit")
    @Produces(MediaType.TEXT_HTML)
    public Response getEditProfilePage() {
        return pageForCurrentProfile("basic/profile/edit");
    }

    @GET
    @Path("basic/profile/view")
    @Produces(MediaType.TEXT_HTML)
    public Response getViewProfilePage() {
        return pageForCurrentProfile("basic/profile/view");
    }

    @GET
    @Path("basic/profile/activity")
    @Produces(MediaType.TEXT_HTML)
    public Response getViewProfileActivityPage(@QueryParam("p") int pageNumber, @QueryParam("ps") int pageSize) {
        if (pageSize == 0) {
            pageSize = DEFAULT_PAGE_SIZE;
        }
        return pageForCurrentProfileActivity("basic/profile/activity", pageNumber, pageSize);
    }

    @GET
    @Path("basic/{page}")
    @Produces(MediaType.TEXT_HTML)
    public String getPage(@PathParam("page") String page) {
        return template("basic/" + page, getContext());
    }

}
