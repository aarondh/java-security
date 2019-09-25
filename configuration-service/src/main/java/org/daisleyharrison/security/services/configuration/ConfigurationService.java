package org.daisleyharrison.security.services.configuration;

import org.daisleyharrison.security.common.exceptions.CypherException;
import org.daisleyharrison.security.common.models.Endorser;
import org.daisleyharrison.security.common.models.Stage;
import org.daisleyharrison.security.common.spi.ConfigurationServiceProvider;
import org.daisleyharrison.security.common.utilities.StageImpl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import java.io.InputStream;
import java.lang.reflect.Method;
import java.security.SignatureException;
import java.time.Duration;
import java.time.Instant;
import java.time.format.DateTimeFormatter;

import org.cache2k.Cache;
import org.cache2k.Cache2kBuilder;
import org.cache2k.integration.FunctionalCacheLoader;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConfigurationService implements ConfigurationServiceProvider {
    private static Logger LOGGER = LoggerFactory.getLogger(ConfigurationService.class);

    public enum State {
        CREATED, INITIALIZING, INITIALIZED, COMPROMISED, CLOSED
    }

    private State state;
    private InputStream configurationSource;
    private Map<String, Object> yamlObject;
    private Cache<String, String> valuesCache;
    private Endorser endorser;
    private ReentrantLock stateLock;
    private static Pattern componentPattern = Pattern
            .compile("(?:\"([^\"]+)\"\\.?|'([^']+)'\\.?|([^\\.\\s]+)\\.?)\\.?");

    private static String[] propertyNameComponents(final String qualifiedPropertyName) {
        Matcher matcher = componentPattern.matcher(qualifiedPropertyName);
        List<String> components = new ArrayList<>();
        while (matcher.find()) {
            String component = matcher.group(1) == null ? matcher.group(2) == null ? matcher.group(3) : matcher.group(2)
                    : matcher.group(1);
            components.add(component);
        }
        return components.toArray(String[]::new);
    }

    @SuppressWarnings("unchecked")
    private static Object getYamlObjectAtProperty(final Map<?, ?> yamlObject, final String qualifiedPropertyName)
            throws Exception {
        String[] components = propertyNameComponents(qualifiedPropertyName);
        Map<?, ?> next = yamlObject;
        Object result = null;
        for (int i = 0; i < components.length; i++) {
            String component = components[i];
            result = next.get(component);
            if (result == null) {
                return null;
            } else if (result instanceof Map) {
                next = (Map<?, ?>) result;
            } else if (i < components.length - 1) {
                return null;
            }
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    private static String getYamlProperty(final Map<?, ?> yamlObject, final String qualifiedPropertyName)
            throws Exception {
        Object result = getYamlObjectAtProperty(yamlObject, qualifiedPropertyName);
        if (result == null) {
            return null;
        } else if (result instanceof ArrayList) {
            Set<String> names = new HashSet<>();
            ArrayList<?> collection = (ArrayList<?>) result;
            for (Object item : collection) {
                if (item instanceof Map<?, ?>) {
                    Map<String, Object> map = (Map<String, Object>) item;
                    map.keySet().forEach((key) -> {
                        names.add(key);
                    });
                } else {
                    names.add(item.toString());
                }
            }
            Yaml yaml = new Yaml();
            return yaml.dump(names);
        } else if (result instanceof Map<?, ?>) {
            Set<String> names = new HashSet<>();
            Map<String, Object> collection = (Map<String, Object>) result;
            collection.forEach((key, value) -> {
                names.add(key);
            });
            Yaml yaml = new Yaml();
            return yaml.dump(names);
        } else {
            return result.toString();
        }
    }

    private class YamlLoader implements FunctionalCacheLoader<String, String> {
        private Map<String, Object> yamlObject;

        public YamlLoader(Map<String, Object> yamlObject) {
            this.yamlObject = yamlObject;
        }

        @Override
        public String load(final String key) throws Exception {
            return ConfigurationService.getYamlProperty(this.yamlObject, key);
        }
    }

    private static final ConfigurationServiceProvider INSTANCE = new ConfigurationService();

    public static ConfigurationServiceProvider getInstance() {
        return INSTANCE;
    }

    protected ConfigurationService() {
        this.state = State.CREATED;
        this.stateLock = new ReentrantLock();
    }

    @Override
    public void requireValidSignature(Endorser endorser) throws SignatureException {
        if (endorser == null) {
            throw new IllegalArgumentException("endorser cannot be null");
        }
        if (this.endorser != null) {
            throw new IllegalStateException("endorser was already specified");
        }
        switch (this.state) {
        case CLOSED:
        case COMPROMISED:
            throw new IllegalStateException("endorser cannot be sent in state " + this.state.toString());
        case CREATED:
            // The endoser was set before the service is configured
            // The signature will be checked during configuration
            this.endorser = endorser;
            break;
        case INITIALIZED:
        case INITIALIZING:
            this.endorser = endorser;
            validateSignature(new Yaml(), endorser);
            break;
        }
    }

    @Override
    public boolean isInitialized() {
        stateLock.lock();
        try {
            return this.state != State.CREATED && this.state != State.CLOSED && this.state != State.INITIALIZING;
        } finally {
            stateLock.unlock();
        }
    }

    @Override
    public boolean isReady() {
        stateLock.lock();
        try {
            return this.state == State.INITIALIZED;
        } finally {
            stateLock.unlock();
        }
    }

    private void assertInitializing() {
        stateLock.lock();
        try {
            if (this.state != State.INITIALIZING) {
                throw new IllegalStateException(
                        "configuration-service  is in state " + this.state.toString() + " and is not initializing.");
            }
        } finally {
            stateLock.unlock();
        }
    }

    private void assertReady() {
        stateLock.lock();
        try {
            if (!isReady()) {
                throw new IllegalStateException(
                        "configuration-service is in state " + this.state.toString() + " and is not ready.");
            }
        } finally {
            stateLock.unlock();
        }
    }

    @Override
    public Stage beginInitialize() {
        stateLock.lock();
        if (this.state != State.CREATED && this.state != State.CLOSED) {
            stateLock.unlock();
            throw new IllegalStateException(
                    "configuration-service is in state " + this.state.toString() + " and cannot be initialized.");
        }
        this.state = State.INITIALIZING;
        return new StageImpl(() -> {
            if (this.state != State.INITIALIZING) {
                stateLock.unlock();
                throw new IllegalStateException(
                        "configuration-service is in state " + this.state.toString() + " and was not initializing.");
            }
            if (this.valuesCache == null) {
                this.state = State.COMPROMISED;
                throw new IllegalStateException("configuration-service was not correctly configured.");
            }
            this.state = State.INITIALIZED;
            stateLock.unlock();
        });
    }

    private static final String SIGNATURE_PROPERTY_NAME = "signature";

    private void validateSignature(Yaml yaml, Endorser endorser) throws SignatureException {
        try {
            String signature = (String) this.yamlObject.remove(SIGNATURE_PROPERTY_NAME);
            if (signature == null) {
                this.yamlObject = null;
                this.state = State.COMPROMISED;
                throw new SignatureException("The yaml file was not signed");
            }
            String payload = yaml.dump(this.yamlObject);
            if (!endorser.verify(signature, payload)) {
                this.yamlObject = null;
                this.state = State.COMPROMISED;
                throw new SignatureException("The yaml file signature was invalid");
            }
        } catch (CypherException exception) {
            this.state = State.COMPROMISED;
            throw new SignatureException(exception.getMessage(), exception);
        }
    }

    @Override
    public void setSource(InputStream configurationSource) {
        assertInitializing();
        this.configurationSource = configurationSource;
    }

    @Override
    public void configure() throws Exception {
        assertInitializing();
        Yaml yaml = new Yaml();
        this.yamlObject = yaml.load(configurationSource);
        if (this.endorser != null) {
            validateSignature(yaml, endorser);
        }
        this.valuesCache = new Cache2kBuilder<String, String>() {
        }.name("configuration-service-cache").permitNullValues(true).loader(new YamlLoader(yamlObject)).build();
    }

    @Override
    public void close() {
        if (this.valuesCache != null) {
            this.valuesCache.close();
            this.valuesCache = null;
        }
        this.yamlObject = null;
        this.state = State.CLOSED;
    }

    @Override
    public boolean hasProperty(String propertyName) {
        assertReady();
        Object value = this.valuesCache.get(propertyName);
        return value != null;
    }

    @Override
    public Set<String> getNames(String propertyName) {
        assertReady();
        Object value = this.valuesCache.get(propertyName);
        if (value == null) {
            return Collections.emptySet();
        } else {
            Yaml yaml = new Yaml();
            Set<String> names = yaml.load(value.toString());
            return names;
        }
    }

    @Override
    public boolean isScalar(String propertyName) {
        assertReady();
        Object value = this.valuesCache.get(propertyName);
        if (value instanceof String) {
            String stringValue = (String) value;
            return !(stringValue.startsWith("{") && stringValue.endsWith("}"));
        }
        return true;
    }

    @Override
    public String getValue(String propertyName, String defaultValue) {
        assertReady();
        Object value = this.valuesCache.get(propertyName);
        if (value == null) {
            return defaultValue;
        } else {
            return value.toString();
        }
    }

    @Override
    public String getValue(String propertyName) throws IllegalArgumentException {
        assertReady();
        Object value = this.valuesCache.get(propertyName);
        if (value == null) {
            throw new IllegalArgumentException("Property " + propertyName + " was not found.");
        } else {
            return value.toString();
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> T convertToEnum(String text, Class<T> clazz) {
        if (clazz.isEnum()) {
            try {
                Method valuesMethod = clazz.getDeclaredMethod("values");
                Object[] values = (Object[]) valuesMethod.invoke(null);
                for (Object value : values) {
                    if (value.toString().compareToIgnoreCase(text) == 0) {
                        return (T) value;
                    }
                }
            } catch (Exception ex) {
            }
        }
        return null;
    }

    private static Map<String, Boolean> boolMap = new HashMap<>();
    static {
        boolMap.put("true", true);
        boolMap.put("false", false);
        boolMap.put("yes", true);
        boolMap.put("no", false);
        boolMap.put("on", true);
        boolMap.put("off", false);
    }

    private <T> T convertTo(String value, Class<T> clazz) {
        if (clazz.isAssignableFrom(Integer.class)) {
            return clazz.cast(Integer.parseInt(value));
        } else if (clazz.isAssignableFrom(Double.class)) {
            return clazz.cast(Double.parseDouble(value));
        } else if (clazz.isAssignableFrom(Byte.class)) {
            return clazz.cast(Byte.parseByte(value));
        } else if (clazz.isAssignableFrom(UUID.class)) {
            return clazz.cast(UUID.fromString(value));
        } else if (clazz.isAssignableFrom(Date.class)) {
            return clazz.cast(Date.from(Instant.from(isoDateFormatter.parse(value))));
        } else if (clazz.isAssignableFrom(Duration.class)) {
            return clazz.cast(Duration.parse(value));
        } else if (clazz.isAssignableFrom(Boolean.class)) {
            Boolean boolValue = boolMap.get(value);
            if (boolValue == null) {
                return null;
            }
            return clazz.cast(boolValue);
        } else if (clazz.isEnum()) {
            return convertToEnum(value.toString(), clazz);
        }

        Yaml yaml = new Yaml(new Constructor(clazz));
        String yamlValue = yaml.dump(value);
        value = yaml.load(yamlValue);
        return clazz.cast(value);
}

    @Override
    public <T> T getValueOfType(String propertyName, Class<T> clazz, T defaultValue) {
        assertReady();
        try {
            Object value = ConfigurationService.getYamlObjectAtProperty(this.yamlObject, propertyName);
            if (value == null) {
                return defaultValue;
            }
            return convertTo(value.toString(),clazz);
        } catch (Exception exception) {
            return defaultValue;
        }
    }

    @Override
    public <T> T getValueOfType(String propertyName, Class<T> clazz) {
        assertReady();
        try {
            Object value = ConfigurationService.getYamlObjectAtProperty(this.yamlObject, propertyName);
            if (value == null) {
                throw new IllegalArgumentException("Property " + propertyName + " was not found.");
            }
            return convertTo(value.toString(), clazz);
        } catch (Exception exception) {
            throw new IllegalArgumentException("Property " + propertyName + " could not be loaded as " + clazz.getName(),
                    exception);
        }
    }

    @Override
    public int getIntegerValue(String propertyName, int defaultValue) {
        String value = getValue(propertyName, Integer.toString(defaultValue));
        return Integer.parseInt(value);
    }

    @Override
    public int getIntegerValue(String propertyName) throws IllegalArgumentException {
        return Integer.parseInt(getValue(propertyName));
    }

    @Override
    public boolean getBooleanValue(String propertyName, boolean defaultValue) {
        String value = getValue(propertyName, Boolean.toString(defaultValue));
        return Boolean.parseBoolean(value);
    }

    @Override
    public boolean getBooleanValue(String propertyName) throws IllegalArgumentException {
        return Boolean.parseBoolean(getValue(propertyName));
    }

    private DateTimeFormatter isoDateFormatter = DateTimeFormatter.ISO_DATE_TIME;

    @Override
    public Date getDateValue(String propertyName, Date defaultValue) {
        String value = getValue(propertyName,
                defaultValue == null ? null : isoDateFormatter.format(defaultValue.toInstant()));
        return Date.from(Instant.from(isoDateFormatter.parse(value)));
    }

    @Override
    public Date getDateValue(String propertyName) throws IllegalArgumentException {
        return Date.from(Instant.from(isoDateFormatter.parse(getValue(propertyName))));
    }

    @Override
    public double getDoubleValue(String propertyName, double defaultValue) {
        String value = getValue(propertyName, Double.toString(defaultValue));
        return Double.parseDouble(value);
    }

    @Override
    public double getDoubleValue(String propertyName) throws IllegalArgumentException {
        return Double.parseDouble(getValue(propertyName));
    }

    @Override
    public Duration getDurationValue(String propertyName, Duration defaultValue) {
        String value = getValue(propertyName, defaultValue.toString());
        return Duration.parse(value);
    }

    @Override
    public Duration getDurationValue(String propertyName) throws IllegalArgumentException {
        return Duration.parse(getValue(propertyName));
    }

    @Override
    public char[] getCharsValue(String propertyName, char[] defaultValue) {
        assertReady();
        try {
            String value = ConfigurationService.getYamlProperty(this.yamlObject, propertyName);
            if (value == null) {
                return defaultValue;
            }
            return value.toCharArray();
        } catch (Exception exception) {
            return defaultValue;
        }
    }

    @Override
    public char[] getCharsValue(String propertyName) throws IllegalArgumentException {
        assertReady();
        try {
            String value = ConfigurationService.getYamlProperty(this.yamlObject, propertyName);
            if (value == null) {
                throw new IllegalArgumentException("Property " + propertyName + " was not found.");
            }
            return value.toCharArray();
        } catch (Exception exception) {
            throw new IllegalArgumentException(
                    "Error reading property " + propertyName + ": " + exception.getMessage());
        }
    }

}