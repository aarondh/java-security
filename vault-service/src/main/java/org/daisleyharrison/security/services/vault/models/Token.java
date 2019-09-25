package org.daisleyharrison.security.services.vault.models;

import org.jose4j.jwt.MalformedClaimException;

public interface Token {
    public boolean isRenewable();
    public boolean isExpired() throws MalformedClaimException;
    public int getUses() throws MalformedClaimException;
    public void setUses(int uses);
    public String getSubject() throws MalformedClaimException;
}