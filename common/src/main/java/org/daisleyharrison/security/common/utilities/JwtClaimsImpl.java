package org.daisleyharrison.security.common.utilities;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Map.Entry;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import org.daisleyharrison.security.common.exceptions.MalformedAuthClaimException;
import org.daisleyharrison.security.common.models.authorization.JwtClaims;

public class JwtClaimsImpl implements JwtClaims {
    private static final ObjectMapper s_objectMapper = new ObjectMapper();
    private static final ObjectReader s_stringListReader = s_objectMapper.readerFor(new TypeReference<List<String>>() {
    });
    private static final int DEFAULT_JWT_ID_LENGTH = 8;
    private static final int MINIMUM_JWT_ID_LENGTH = 4;
    private static final int MAXIMUM_JWT_ID_LENGTH = 128;

    private Map<String, Object> claims;

    public JwtClaimsImpl() {
        claims = new HashMap<>();
        setIssuedAt(new Date());
    }

    public JwtClaimsImpl(Map<String, Object> claimsMap) {
        claims = claimsMap;
    }

    @Override
    public Map<String, Object> getClaimsMap() {
        return new HashMap<>(this.claims);
    }

    @Override
    public Collection<String> getClaimNames() {
        Set<String> names = new HashSet<String>();
        this.claims.forEach((name, value) -> names.add(name));
        return names;
    }

    @Override
    public String getIssuer() throws MalformedAuthClaimException {
        return getStringClaimValue(JwtClaims.ReservedClaims.ISSUER);
    }

    @Override
    public void setIssuer(String issuer) {
        setClaim(JwtClaims.ReservedClaims.ISSUER, issuer);
    }

    @Override
    public String getSubject() throws MalformedAuthClaimException {
        return getStringClaimValue(JwtClaims.ReservedClaims.SUBJECT);
    }

    @Override
    public void setSubject(String subject) {
        setClaim(JwtClaims.ReservedClaims.SUBJECT, subject);
    }

    @Override
    public List<String> getAudience() throws MalformedAuthClaimException {
        Object audience = getClaimValue(JwtClaims.ReservedClaims.AUDIENCE);
        if (audience instanceof String) {
            return Collections.singletonList((String) audience);
        } else if (audience instanceof List) {
            return toStringList((List) audience);
        } else {
            return Collections.emptyList();
        }
    }

    @Override
    public void addAudience(String audience) throws MalformedAuthClaimException {
        if (hasAudience()) {
            List<String> audiences = getAudience();
            if (audiences.size() == 1) {
                List<String> newAudiences = new ArrayList<>();
                newAudiences.addAll(audiences);
                newAudiences.add(audience);
                setClaim(JwtClaims.ReservedClaims.AUDIENCE, newAudiences);
            } else {
                audiences.add(audience);
            }
        } else {
            setClaim(JwtClaims.ReservedClaims.AUDIENCE, audience);
        }
    }

    @Override
    public void addAudience(List<String> audiences) throws MalformedAuthClaimException {
        for (String audience : audiences) {
            addAudience(audience);
        }
    }

    @Override
    public boolean hasAudience() {
        return hasClaim(JwtClaims.ReservedClaims.AUDIENCE);
    }

    @Override
    public Date getExpirationTime() throws MalformedAuthClaimException {
        return getDateClaimValue(JwtClaims.ReservedClaims.EXPIRES);
    }

    @Override
    public void setExpirationTime(Date date) {
        setDateClaimValue(JwtClaims.ReservedClaims.EXPIRES, date);
    }

    @Override
    public Date getDateClaimValue(String claimName) throws MalformedAuthClaimException {
        try {
            Object secondsFromEpochObject = getClaimValue(claimName);
            if (secondsFromEpochObject instanceof Integer) {
                return new Date((long) ((int) secondsFromEpochObject) * 1000);
            } else if (secondsFromEpochObject instanceof Long) {
                return new Date(((long) secondsFromEpochObject) * 1000);
            }
        } catch (ClassCastException exception) {
            throw new MalformedAuthClaimException("expected claim " + claimName + " to be a Number", exception);
        }
        return null;
    }

    private void setDateClaimValue(String claimName, Date date) {
        setClaim(claimName, (long) (date.getTime() / 1000));
    }

    @Override
    public Date getNotBefore() throws MalformedAuthClaimException {
        return getDateClaimValue(JwtClaims.ReservedClaims.NOT_BEFORE);
    }

    @Override
    public void setNotBefore(Date date) {
        setDateClaimValue(JwtClaims.ReservedClaims.NOT_BEFORE, date);
    }

    @Override
    public Date getIssuedAt() throws MalformedAuthClaimException {
        return getDateClaimValue(JwtClaims.ReservedClaims.ISSUED_AT);
    }

    private void setIssuedAt(Date date) {
        setDateClaimValue(JwtClaims.ReservedClaims.ISSUED_AT, date);
    }

    public String getJwtId() throws MalformedAuthClaimException {
        return getStringClaimValue(JwtClaims.ReservedClaims.JWT_ID);
    }

    public void setJwtId(String jwtId) {
        setClaim(JwtClaims.ReservedClaims.JWT_ID, jwtId);
    }

    public void setGeneratedJwtId() {
        setGeneratedJwtId(DEFAULT_JWT_ID_LENGTH);
    }

    public void setGeneratedJwtId(int numberOfBytes) {
        if (numberOfBytes < MINIMUM_JWT_ID_LENGTH || numberOfBytes > MAXIMUM_JWT_ID_LENGTH) {
            throw new IllegalArgumentException("numberOfBytes is invalid");
        }
        setJwtId(SecureRandomUtil.generateRandomString(numberOfBytes));
    }

    @Override
    public boolean hasClaim(String claimName) {
        return getClaimValue(claimName) != null;
    }

    @Override
    public boolean isClaimValueOfType(String claimName, Class type) {
        Object value = getClaimValue(claimName);
        return value != null && type.isAssignableFrom(value.getClass());
    }

    @Override
    public boolean isClaimValueString(String claimName) {
        return isClaimValueOfType(claimName, String.class);
    }

    @Override
    public boolean isClaimValueStringList(String claimName) {
        try {
            return getStringListClaimValue(claimName) != null;
        } catch (MalformedAuthClaimException e) {
            return false;
        }
    }

    @Override
    public Object getClaimValue(String claimName) {
        return this.claims.get(claimName);
    }

    @Override
    public <T> T getClaimValue(String claimName, Class<T> type) throws MalformedAuthClaimException {
        try {
            return type.cast(this.claims.get(claimName));
        } catch (ClassCastException exception) {
            throw new MalformedAuthClaimException("claim " + claimName + " is not of type " + type.getName(),
                    exception);
        }
    }

    @Override
    public String getStringClaimValue(String claimName) throws MalformedAuthClaimException {
        return getClaimValue(claimName, String.class);
    }

    protected List<String> toStringList(List list) throws MalformedAuthClaimException {
        try {
            if (list == null) {
                return Collections.emptyList();
            }
            List<String> values = new ArrayList<>();
            for (Object object : list) {
                values.add((String) object);
            }
            return values;
        } catch (ClassCastException exception) {
            throw new MalformedAuthClaimException("one of the elements of the list is not a string", exception);
        }
    }

    @Override
    public List<String> getStringListClaimValue(String claimName) throws MalformedAuthClaimException {
        List<?> list = getClaimValue(claimName, List.class);
        return toStringList(list);
    }

    @Override
    public void setClaim(String claimName, Object value) {
        this.claims.put(claimName, value);
    }

    @Override
    public void unsetClaim(String claimName) {
        this.claims.remove(claimName);
    }

    @Override
    public String toJson() {
        try {
            return s_objectMapper.writeValueAsString(this.getClaimsMap());
        } catch (JsonProcessingException exception) {
            return null;
        }
    }

    private static <T extends JwtClaims> T parse(Iterator<Entry<String, JsonNode>> fields, Class<T> type)
            throws SecurityException, NoSuchMethodException, InstantiationException, InvocationTargetException,
            IllegalAccessException, IOException {
        T claims = type.getDeclaredConstructor().newInstance();
        while (fields.hasNext()) {
            Entry<String, JsonNode> entry = fields.next();
            String claimName = entry.getKey();
            JsonNode value = entry.getValue();
            if (value.isArray()) {
                List<String> stringListValue = s_stringListReader.readValue(value);
                claims.setClaim(claimName, stringListValue);
            } else if (value.isInt()) {
                int intValue = value.asInt();
                claims.setClaim(claimName, intValue);
            } else if (value.isLong()) {
                long longValue = value.asLong();
                claims.setClaim(claimName, longValue);
            } else if (value.isBoolean()) {
                boolean boolValue = value.asBoolean();
                claims.setClaim(claimName, boolValue);
            } else if (value.isArray()) {
                boolean boolValue = value.asBoolean();
                claims.setClaim(claimName, boolValue);
            } else if (value.isTextual()) {
                String stringValue = value.asText();
                claims.setClaim(claimName, stringValue);
            } else if (value.isContainerNode()) {
                JwtClaims subclaims = parse(value.fields(), type);
                claims.setClaim(claimName, subclaims);
            }
        }
        return claims;
    }

    public static <T extends JwtClaims> T parse(String jsonClaims, Class<T> type) throws MalformedAuthClaimException {
        try {
            JsonNode tree = s_objectMapper.readTree(jsonClaims);
            return parse(tree.fields(), type);
        } catch (SecurityException | NoSuchMethodException | InstantiationException | InvocationTargetException
                | IllegalAccessException | IOException exception) {
            throw new MalformedAuthClaimException("Error parsing claims", exception);
        }
    }

    public static JwtClaims parse(String jsonClaims) throws MalformedAuthClaimException {
        return parse(jsonClaims, JwtClaimsImpl.class);
    }

    @Override
    public String toString() {
        try {
            return s_objectMapper.writeValueAsString(this);
        } catch (IOException exception) {
            return super.toString();
        }
    }

}