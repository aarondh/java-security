package org.daisleyharrison.security.common.utilities;

import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.Collections;

import org.daisleyharrison.security.common.exceptions.MalformedAuthClaimException;
import org.daisleyharrison.security.common.models.authorization.OpenIdClaims;

public class OpenIdClaimsImpl extends JwtClaimsImpl implements OpenIdClaims {
    public OpenIdClaimsImpl() {
        super();
    }
    
    public OpenIdClaimsImpl(Map<String,Object> claimsMap) {
        super(claimsMap);
    }


    @Override
    public String getGivenName() throws MalformedAuthClaimException {
        return getStringClaimValue(OpenIdClaims.ReservedClaims.GIVEN_NAME);
    }

    @Override
    public void setGivenName(String first_name) {
        setClaim(OpenIdClaims.ReservedClaims.GIVEN_NAME, first_name);
    }

    @Override
    public String getFamilyName() throws MalformedAuthClaimException {
        return getStringClaimValue(OpenIdClaims.ReservedClaims.FAMILY_NAME);
    }

    @Override
    public void setFamilyName(String family_name) {
        setClaim(OpenIdClaims.ReservedClaims.FAMILY_NAME, family_name);
    }

    @Override
    public String getMiddleName() throws MalformedAuthClaimException {
        return getStringClaimValue(OpenIdClaims.ReservedClaims.MIDDLE_NAME);
    }

    @Override
    public void setMiddleName(String middle_name) {
        setClaim(OpenIdClaims.ReservedClaims.MIDDLE_NAME, middle_name);
    }

    @Override
    public String getName() throws MalformedAuthClaimException {
        return getStringClaimValue(OpenIdClaims.ReservedClaims.NAME);
    }

    @Override
    public void setName(String name) {
        setClaim(OpenIdClaims.ReservedClaims.NAME, name);
    }

    @Override
    public String getNickname() throws MalformedAuthClaimException {
        return getStringClaimValue(OpenIdClaims.ReservedClaims.NICKNAME);
    }

    @Override
    public void setNickname(String nickname) {
        setClaim(OpenIdClaims.ReservedClaims.NICKNAME, nickname);
    }

    @Override
    public String getPreferredUsername() throws MalformedAuthClaimException {
        return getStringClaimValue(OpenIdClaims.ReservedClaims.PREFERRED_USERNAME);
    }

    @Override
    public void setPreferredUsername(String preferred_username) {
        setClaim(OpenIdClaims.ReservedClaims.PREFERRED_USERNAME, preferred_username);
    }

    @Override
    public String getEmail() throws MalformedAuthClaimException {
        return getStringClaimValue(OpenIdClaims.ReservedClaims.EMAIL);
    }

    @Override
    public void setEmail(String email) {
        setClaim(OpenIdClaims.ReservedClaims.EMAIL, email);
    }

    @Override
    public String getPicture() throws MalformedAuthClaimException {
        return getStringClaimValue(OpenIdClaims.ReservedClaims.PICTURE);
    }

    @Override
    public void setPicture(String picture) {
        setClaim(OpenIdClaims.ReservedClaims.PICTURE, picture);
    }

    @Override
    public List<String> getScopes() throws MalformedAuthClaimException {
        Object roles = getClaimValue(OpenIdClaims.ReservedClaims.SCOPE);
        if (roles instanceof String) {
            return Collections.singletonList((String) roles);
        } else if (roles instanceof List) {
            return toStringList((List) roles);
        } else {
            return Collections.emptyList();
        }
    }

    @Override
    public void addScope(String scope) throws MalformedAuthClaimException {
        if (hasClaim(OpenIdClaims.ReservedClaims.SCOPE)) {
            List<String> scopes = getScopes();
            if (scopes.size() == 1) {
                List<String> newScopes = new ArrayList<>();
                newScopes.addAll(scopes);
                newScopes.add(scope);
                setClaim(OpenIdClaims.ReservedClaims.SCOPE, newScopes);
            } else {
                scopes.add(scope);
                setClaim(OpenIdClaims.ReservedClaims.SCOPE, scopes);
            }
        } else {
            setClaim(OpenIdClaims.ReservedClaims.SCOPE, scope);
        }
    }

    @Override
    public boolean hasScope() {
        return getClaimValue(OpenIdClaims.ReservedClaims.SCOPE) != null;
    }

    @Override
    public void addScope(List<String> scopes) throws MalformedAuthClaimException {
        for (String scope : scopes) {
            addScope(scope);
        }
    }

    @Override
    public boolean removeScope(String scope) throws MalformedAuthClaimException {
        if (hasClaim(OpenIdClaims.ReservedClaims.SCOPE)) {
            List<String> scopes = getScopes();
            if (scopes.remove(scope)) {
                if (scopes.isEmpty()) {
                    unsetClaim(OpenIdClaims.ReservedClaims.SCOPE);
                } else if (scopes.size() == 1) {
                    setClaim(OpenIdClaims.ReservedClaims.SCOPE, scopes.get(0));
                }
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean hasScope(String... scopes) throws MalformedAuthClaimException {
        List<String> haveScopes = getScopes();
        for (String scope : scopes) {
            if (haveScopes.contains(scope)) {
                return true;
            }
        }
        return false;
    }

    public static OpenIdClaims parse(String jsonClaims) throws MalformedAuthClaimException {
        return JwtClaimsImpl.parse(jsonClaims, OpenIdClaimsImpl.class);
    }

}