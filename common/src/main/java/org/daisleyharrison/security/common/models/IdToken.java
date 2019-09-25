package org.daisleyharrison.security.common.models;

import java.util.Date;

import java.util.List;

import org.daisleyharrison.security.common.exceptions.MalformedAuthClaimException;
import org.daisleyharrison.security.common.models.authorization.AuthClaims;

public class IdToken {
    private String iss;
    private String sub;
    private List<String> aud;
    private Date exp;
    private Date iat;

    public IdToken() {

    }

    public IdToken(AuthClaims claims) throws MalformedAuthClaimException {
        iss = claims.getIssuer();
        sub = claims.getSubject();
        aud = claims.getAudience();
        exp = claims.getExpirationTime();
        iat = claims.getIssuedAt();
    }

    /**
     * @return String return the iss
     */
    public String getIss() {
        return iss;
    }

    /**
     * @param iss the iss to set
     */
    public void setIss(String iss) {
        this.iss = iss;
    }

    /**
     * @return String return the sub
     */
    public String getSub() {
        return sub;
    }

    /**
     * @param sub the sub to set
     */
    public void setSub(String sub) {
        this.sub = sub;
    }

    /**
     * @return String return the aud
     */
    public List<String> getAud() {
        return aud;
    }

    /**
     * @param aud the aud to set
     */
    public void setAud(List<String> aud) {
        this.aud = aud;
    }

    /**
     * @return Date return the exp
     */
    public Date getExp() {
        return exp;
    }

    /**
     * @param Date the exp to set
     */
    public void setExp(Date exp) {
        this.exp = exp;
    }

    /**
     * @return String return the iat
     */
    public Date getIat() {
        return iat;
    }

    /**
     * @param iat the iat to set
     */
    public void setIat(Date iat) {
        this.iat = iat;
    }

}