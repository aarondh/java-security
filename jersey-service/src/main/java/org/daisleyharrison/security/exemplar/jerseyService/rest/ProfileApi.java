package org.daisleyharrison.security.samples.jerseyService.rest;

import org.daisleyharrison.security.samples.jerseyService.models.PagedData;

import javax.ws.rs.core.Response;

import org.daisleyharrison.security.common.exceptions.MalformedAuthClaimException;
import org.daisleyharrison.security.common.models.PagedCollection;
import org.daisleyharrison.security.common.models.authorization.AuthClaims;
import org.daisleyharrison.security.common.models.profile.Profile;
import org.daisleyharrison.security.common.models.profile.ProfileActivity;
import org.daisleyharrison.security.samples.jerseyService.ControllerBase;
import org.daisleyharrison.security.samples.jerseyService.filters.NonceRequired;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.Optional;

import javax.annotation.security.RolesAllowed;
import javax.management.ServiceNotFoundException;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

/**
 * Root resource (exposed at "login" path)
 */
@Path("api/profile")
@RolesAllowed("Admin")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class ProfileApi extends ControllerBase {
    private static Logger LOGGER = LoggerFactory.getLogger(Profile.class);
    private static int DEFAULT_PAGE_SIZE = 20;

    public ProfileApi() {
    }

    @GET
    @Path("profiles")
    public Response getProfiles(@QueryParam("p") int pageNumber, @QueryParam("ps") int pageSize) {
        try {
            if (pageSize == 0) {
                pageSize = DEFAULT_PAGE_SIZE;
            }
            PagedCollection<Profile> profiles = getProfileService().list(pageNumber, pageSize);
            return Response.ok(new PagedData<Profile>(profiles, profiles.getTotalSize(), pageNumber)).build();
        } catch (ServiceNotFoundException exception) {
            return errorResponse(exception);
        }
    }

    @GET
    @Path("profiles/{profileId}")
    public Response getProfile(@PathParam("profileId") String profileId) {
        try {
            Optional<Profile> profile = getProfileService().read(profileId);
            if (profile.isPresent()) {
                return Response.ok(profile.get()).build();
            } else {
                return errorResponse(404, "profile not found");
            }
        } catch (ServiceNotFoundException exception) {
            return errorResponse(exception);
        }
    }

    @DELETE
    @Path("profiles/{profileId}")
    @NonceRequired
    public Response deleteProfile(@PathParam("profileId") String profileId) {
        try {
            Optional<Profile> profile = getProfileService().delete(profileId, this.getUserClaims().getSubject());
            if (profile.isPresent()) {
                return Response.ok(profile.get()).build();
            } else {
                return errorResponse(404, "profile not found");
            }
        } catch (MalformedAuthClaimException | ServiceNotFoundException exception) {
            return errorResponse(exception);
        }
    }

    @PUT
    @Path("profiles/{profileId}")
    @NonceRequired
    @RolesAllowed({ "Admin", "Basic" })
    public Response putProfile(@PathParam("profileId") String profileId, Profile profile) {
        try {
            if (profileId == null || profileId.isBlank() || !profileId.equals(profile.getId())) {
                return errorResponse(404, "profile not found");
            }
            AuthClaims userClaims = this.getUserClaims();
            String sessionProfileId = userClaims.getSubject();
            if (!userClaims.hasScope("Admin")) {
                if (!profileId.equals(sessionProfileId)) {
                    return errorResponse(404, "profile not found");
                }
            }
            Optional<Profile> optExistingProfile = getProfileService().read(profileId);
            if (optExistingProfile.isPresent()) {
                Profile existingProfile = optExistingProfile.get();
                existingProfile.setGivenName(profile.getGivenName());
                existingProfile.setFamilyName(profile.getFamilyName());
                existingProfile.setPreferredUsername(profile.getPreferredUsername());
                existingProfile.setEmail(profile.getEmail());
                existingProfile.setModified(new Date());
                existingProfile.setModifiedBy(sessionProfileId);
                Optional<Profile> optUpdatedProfile = getProfileService().update(existingProfile, sessionProfileId);
                return Response.ok(optUpdatedProfile.get()).build();
            } else {
                return errorResponse(404, "profile not found");
            }
        } catch (MalformedAuthClaimException | ServiceNotFoundException exception) {
            return errorResponse(exception);
        }
    }

    @GET
    @Path("profiles/{profileId}/activity")
    @NonceRequired
    @RolesAllowed({ "Admin", "Basic" })
    public Response getProfileActivity(@PathParam("profileId") String profileId, @QueryParam("p") int pageNumber,
            @QueryParam("ps") int pageSize) {
        try {
            if (pageSize == 0) {
                pageSize = DEFAULT_PAGE_SIZE;
            }
            PagedCollection<ProfileActivity> activity = getProfileService().listActivity(profileId, pageNumber,
                    pageSize);
            return Response.ok(new PagedData<ProfileActivity>(activity, activity.getTotalSize(), pageNumber)).build();
        } catch (ServiceNotFoundException exception) {
            return errorResponse(exception);
        }
    }

}
