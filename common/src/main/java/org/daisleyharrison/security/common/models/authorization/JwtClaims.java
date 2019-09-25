package org.daisleyharrison.security.common.models.authorization;

import java.util.Date;
import java.util.List;

import org.daisleyharrison.security.common.exceptions.MalformedAuthClaimException;

public interface JwtClaims extends Claims {
    public class ReservedClaims {
        public static final String ISSUER = "iss";
        public static final String AUDIENCE = "aud";
        public static final String ISSUED_AT = "iat";
        public static final String EXPIRES = "exp";
        public static final String SUBJECT = "sub";
        public static final String NOT_BEFORE = "nbf";
        public static final String JWT_ID = "jti";
        public static final String CONFIRMATION = "cnf";
        }
    public String getIssuer() throws MalformedAuthClaimException;

    public void setIssuer(String issuer);

    public String getSubject() throws MalformedAuthClaimException;

    public void setSubject(String subject);

    public Date getIssuedAt() throws MalformedAuthClaimException;

    public Date getNotBefore() throws MalformedAuthClaimException;

    public void setNotBefore(Date date);

    public Date getExpirationTime() throws MalformedAuthClaimException;

    public void setExpirationTime(Date date);

    public List<String> getAudience() throws MalformedAuthClaimException;

    public void addAudience(String audience) throws MalformedAuthClaimException;

    public void addAudience(List<String> audience) throws MalformedAuthClaimException;

    public boolean hasAudience();

    public String getJwtId() throws MalformedAuthClaimException;

    public void setJwtId(String jwtId);

    public void setGeneratedJwtId();

    public void setGeneratedJwtId(int numberOfBytes);

}