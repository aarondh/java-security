package org.daisleyharrison.security.common.models.authorization;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.daisleyharrison.security.common.exceptions.MalformedAuthClaimException;

public interface Claims {

    public Map<String, Object> getClaimsMap();

    public Collection<String> getClaimNames();

    public boolean hasClaim(String claimName);

    public boolean isClaimValueString(String claimName);

    public boolean isClaimValueStringList(String claimName);

    public boolean isClaimValueOfType(String claimName, Class type);

    public Object getClaimValue(String claimName);

    public <T> T getClaimValue(String claimName, Class<T> type) throws MalformedAuthClaimException;

    public String getStringClaimValue(String claimName) throws MalformedAuthClaimException;

    public Date getDateClaimValue(String claimName) throws MalformedAuthClaimException;

    public List<String> getStringListClaimValue(String claimName) throws MalformedAuthClaimException;

    public void setClaim(String claimName, Object value);

    public void unsetClaim(String claimName);

    public String toJson();
}