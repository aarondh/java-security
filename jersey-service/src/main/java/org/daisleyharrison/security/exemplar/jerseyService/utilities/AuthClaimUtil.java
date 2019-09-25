package org.daisleyharrison.security.samples.jerseyService.utilities;

import org.daisleyharrison.security.common.exceptions.MalformedAuthClaimException;
import org.daisleyharrison.security.common.models.authorization.AuthClaims;

public class AuthClaimUtil {
    /**
     * return a string claim or null if the claim was malformed
     * 
     * @param claims
     * @param claimName
     * @return
     */
    public static String getStringClaimOrNull(AuthClaims claims, String claimName) {
        try {
            return claims.getStringClaimValue(claimName);
        } catch (MalformedAuthClaimException exception) {
            return null;
        }
    }

    /**
     * Get a name from a set of claims usesful for distigishing the claims in a log
     * file
     * 
     * @param claims
     * @return
     */
    public static String getFunctionalNameFrom(AuthClaims claims) {
        StringBuilder result = new StringBuilder();
        try {
            String name = claims.getPreferredUsername();
            if(name == null ){
                name = claims.getName();
            }
        if (name == null) {
            String givenName = claims.getGivenName();
            String familyName = claims.getFamilyName();

            if (givenName == null) {
                if (familyName == null) {
                    try {
                        result.append("sub:");
                        result.append(claims.getSubject());
                    } catch (MalformedAuthClaimException exception) {
                        result.append("**mangled**");
                    }
                } else {
                    result.append(familyName);
                }
            } else {
                result.append(givenName);
                if (familyName != null) {
                    result.append(",");
                    result.append(familyName);
                }
            }
        } else {
            result.append(name);
        }
        try {
            result.append(" with scope(s): ");
            claims.getScopes().forEach(scope -> {
                result.append(scope);
                result.append(" ");
            });
        } catch (MalformedAuthClaimException exception) {

        }
    } catch (MalformedAuthClaimException exception) {
        result.append("** malformed claim**");
    }

        return result.toString();
    }
}