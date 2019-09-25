package org.daisleyharrison.security.common.models.authorization;

import java.util.List;

import org.daisleyharrison.security.common.exceptions.MalformedAuthClaimException;

public interface OpenIdClaims extends JwtClaims {
    public class ReservedClaims {
        public static final String GIVEN_NAME = "given_name";
        public static final String FAMILY_NAME = "family_name";
        public static final String MIDDLE_NAME = "middle_name";
        public static final String NAME = "name";
        public static final String NICKNAME = "nickname";
        public static final String PREFERRED_USERNAME = "preferred_username";
        public static final String EMAIL = "email";
        public static final String SCOPE = "scopes";
        public static final String PICTURE = "picture";
        public static final String AT_HASH = "at_hash";
        public static final String C_HASH = "c_hash";
    }
    public String getGivenName() throws MalformedAuthClaimException;
    
    public void setGivenName(String first_name);

    public String getFamilyName() throws MalformedAuthClaimException;
    
    public void setFamilyName(String family_name);

    public String getMiddleName() throws MalformedAuthClaimException;
    
    public void setMiddleName(String middle_name);

    public String getName() throws MalformedAuthClaimException;
    
    public void setName(String name);

    public String getNickname() throws MalformedAuthClaimException;
    
    public void setNickname(String nickname);

    public String getPreferredUsername() throws MalformedAuthClaimException;
    
    public void setPreferredUsername(String preferred_username);

    public String getEmail() throws MalformedAuthClaimException;
    
    public void setEmail(String email);

    public String getPicture() throws MalformedAuthClaimException;
    
    public void setPicture(String picture);

    public boolean hasScope();
    
    public void addScope(String scope) throws MalformedAuthClaimException;

    public void addScope(List<String> scopes) throws MalformedAuthClaimException;

    public boolean removeScope(String scope) throws MalformedAuthClaimException;

    public boolean hasScope(String... scopes) throws MalformedAuthClaimException;

    public List<String> getScopes() throws MalformedAuthClaimException;

}