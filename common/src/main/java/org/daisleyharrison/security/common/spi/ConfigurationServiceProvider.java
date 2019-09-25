package org.daisleyharrison.security.common.spi;

import java.io.InputStream;
import java.security.SignatureException;
import java.util.Date;
import java.time.Duration;
import java.util.Set;

import org.daisleyharrison.security.common.models.Endorser;

public interface ConfigurationServiceProvider extends SecurityServiceProvider {

    public void setSource(InputStream inputStream);

    public boolean hasProperty(String propertyName);

    public void requireValidSignature(Endorser endorser) throws SignatureException;

    public boolean isScalar(String propertyName);

    public Set<String> getNames(String propertyName);

    public String getValue(String propertyName, String defaultValue);

    public String getValue(String propertyName) throws IllegalArgumentException;

    public <T> T getValueOfType(String propertyName, Class<T> type, T defaultValue);

    public <T> T getValueOfType(String propertyName, Class<T> type);

    public boolean getBooleanValue(String propertyName, boolean defaultValue);

    public boolean getBooleanValue(String propertyName) throws IllegalArgumentException;

    public Date getDateValue(String propertyName, Date defaultValue);

    public Date getDateValue(String propertyName) throws IllegalArgumentException;

    public double getDoubleValue(String propertyName, double defaultValue);

    public double getDoubleValue(String propertyName) throws IllegalArgumentException;

    public int getIntegerValue(String propertyName, int defaultValue);

    public int getIntegerValue(String propertyName) throws IllegalArgumentException;

    public Duration getDurationValue(String propertyName, Duration defaultValue);

    public Duration getDurationValue(String propertyName) throws IllegalArgumentException;

    public char[] getCharsValue(String propertyName, char[] defaultValue);

    public char[] getCharsValue(String propertyName) throws IllegalArgumentException;
}