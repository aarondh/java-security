package org.daisleyharrison.security.services.openId.utilities;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class URIBuilder {
    private static final String PROPERTY_PATTERN = "\\{(?<name>\\w+)(?:\\:(?<default>[^\\}]*))?\\}";
    private Map<String, String> properties = new HashMap<>();
    private Map<String, String> parameters = new HashMap<>();
    private boolean hasQueryString;
    private static Pattern propertyPattern = Pattern.compile(PROPERTY_PATTERN);
    private String uriPattern;
    private String encoding = "UTF-8";
    private boolean allowMissingProperties;

    public URIBuilder() {
        this.allowMissingProperties = false;
    }

    public URIBuilder(String uriPattern) {
        this();
        setUriPattern(uriPattern);
    }

    public boolean getAllowMissingProperies() {
        return allowMissingProperties;
    }

    public URIBuilder setAllowMissingProperies(boolean value) {
        this.allowMissingProperties = value;
        return this;
    }

    public URIBuilder setUriPattern(String uriPattern) {
        this.uriPattern = uriPattern;
        if (uriPattern == null) {
            throw new IllegalArgumentException("uriPattern cannot be null");
        }
        hasQueryString = uriPattern.contains("?");
        properties.clear();

        Matcher matcher = propertyPattern.matcher(uriPattern);

        while (matcher.find()) {
            String propertyName = matcher.group("name");
            String value = matcher.group("default");
            properties.put(propertyName, value);
        }
        return this;
    }

    public URIBuilder reset() {
        if (uriPattern == null) {
            this.properties.clear();
            this.parameters.clear();
            this.hasQueryString = false;
        } else {
            setUriPattern(getUriPattern());
        }
        return this;
    }

    public String getUriPattern() {
        return this.uriPattern;
    }

    public Set<String> getPropertyNames() {
        return properties.keySet();
    }

    public URIBuilder setProperty(String propertyName, String value) {
        if (parameters == null) {
            throw new IllegalArgumentException("propertyName cannot be null.");
        }
        if (this.properties.containsKey(propertyName)) {
            this.properties.put(propertyName, value);
        } else {
            throw new IllegalArgumentException("propertyName \"" + propertyName + "\" was not found.");
        }
        return this;
    }

    public String getProperty(String propertyName) {
        if (parameters == null) {
            throw new IllegalArgumentException("propertyName cannot be null");
        }
        if (this.properties.containsKey(propertyName)) {
            return this.properties.get(propertyName);
        } else {
            throw new IllegalArgumentException("propertyName \"" + propertyName + "\" was not found.");
        }
    }

    public URIBuilder setProperties(Map<String, String> properties) {
        if (properties == null) {
            throw new IllegalArgumentException("properties map cannot be null");
        }
        properties.forEach((name, value) -> setProperty(name, value));
        return this;
    }

    public Map<String, String> getProperties() {
        return Collections.unmodifiableMap(this.properties);
    }

    public URIBuilder setParameter(String parameterName, String value) {
        if (parameters == null) {
            throw new IllegalArgumentException("parameterName cannot be null");
        }
        this.parameters.put(parameterName, value);
        return this;
    }

    public String getParameter(String parameterName) {
        return this.parameters.get(parameterName);
    }

    public URIBuilder setParameters(Map<String, String> parameters) {
        if (parameters == null) {
            throw new IllegalArgumentException("parameters map cannot be null");
        }
        parameters.forEach((name, value) -> setParameter(name, value));
        return this;
    }

    public Map<String, String> getParameters() {
        return Collections.unmodifiableMap(this.parameters);
    }

    /**
     * build the URI string and return as a URI
     * 
     * @return the URI
     * @throws URISyntaxException
     */
    public URI build() throws URISyntaxException, UnsupportedEncodingException {
        Matcher matcher = propertyPattern.matcher(uriPattern);
        StringBuffer result = new StringBuffer();
        while (matcher.find()) {
            String propertyName = matcher.group("name");
            String defaultValue = matcher.group("default");
            String value = properties.get(propertyName);
            if (value == null) {
                if (defaultValue != null) {
                    value = defaultValue;
                } else if (!allowMissingProperties) {
                    throw new IllegalArgumentException("URI property \"" + propertyName + "\" not specified");
                }
                value = "";
            } else {
                value = URLEncoder.encode(value, encoding);
            }
            matcher.appendReplacement(result, value);
        }
        matcher.appendTail(result);

        int fragmentOffset = result.indexOf("#");
        String querySuffix;
        if (fragmentOffset < 0) {
            querySuffix = "";
        } else {
            querySuffix = result.substring(fragmentOffset);
            result.setLength(fragmentOffset);
        }

        boolean isFirst = true;
        for (Map.Entry<String, String> entry : parameters.entrySet()) {
            String name = entry.getKey();
            String value = entry.getValue();
            if (isFirst) {
                isFirst = false;
                if (hasQueryString) {
                    result.append('&');
                } else {
                    result.append('?');
                }
            } else {
                result.append('&');
            }
            result.append(URLEncoder.encode(name, encoding));
            if (value != null) {
                result.append('=');
                result.append(URLEncoder.encode(value, encoding));
            }
        }
        result.append(querySuffix);
        return new URI(result.toString());
    }

    public String toString() {
        try {
            return build().toString();
        } catch (URISyntaxException | UnsupportedEncodingException | IllegalArgumentException ex) {
            return ex.getMessage();
        }
    }
}