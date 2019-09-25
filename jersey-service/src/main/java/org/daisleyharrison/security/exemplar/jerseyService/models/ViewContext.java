package org.daisleyharrison.security.samples.jerseyService.models;

import java.util.Date;

import org.daisleyharrison.security.common.exceptions.MalformedAuthClaimException;
import org.daisleyharrison.security.common.models.authorization.AuthClaims;
import org.daisleyharrison.security.samples.jerseyService.Main;

public class ViewContext {
    private AuthClaims userClaims;
    private String logoutUri = "/logout";
    private String currentUri;

    public ViewContext(AuthClaims userClaims, String currentUri) {
        this.userClaims = userClaims;
        this.currentUri = currentUri;
    }

    public AuthClaims getUserClaims() {
        return this.userClaims;
    }

    public boolean isAuthorized() {
        return getUserClaims() != null;
    }

    public String getBaseUri() {
        return Main.getBaseUri().toString();
    }

    public String getLogoutUri() {
        return this.getBaseUri() + this.logoutUri;
    }

    public String getIssuer() {
        try {
            return this.userClaims.getIdTokenIssuer();
        } catch( NullPointerException| MalformedAuthClaimException exception) {
            return null;
        }
    }

    public String getCurrentUri() {
        return this.currentUri;
    }

    /**
     * @param userClaims the userClaims to set
     */
    public void setUserClaims(AuthClaims userClaims) {
        this.userClaims = userClaims;
    }

    /**
     * @param logoutUri the logoutUri to set
     */
    public void setLogoutUri(String logoutUri) {
        this.logoutUri = logoutUri;
    }

    /**
     * @param currentUri the currentUri to set
     */
    public void setCurrentUri(String currentUri) {
        this.currentUri = currentUri;
    }

    /**
     * @return String return the userPicureUrl
     */
    public String getUserPictureUrl() {
        try {
            return this.userClaims.getPicture();
        } catch( NullPointerException| MalformedAuthClaimException exception) {
            return null;
        }
    }

    public Date getIssuedAt() {
        try {
            return this.userClaims.getIssuedAt();
        } catch( NullPointerException| MalformedAuthClaimException exception) {
            return null;
        }
    }

    /**
     * @return String return the preferredUsername
     */
    public String getPreferredUsername() {
        try {
            return this.userClaims.getPreferredUsername();
        } catch( NullPointerException| MalformedAuthClaimException exception) {
            return null;
        }
    }

}
