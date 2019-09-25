package org.daisleyharrison.security.common.models.openId;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonProperty;

public class OpenIdPropertyDef implements Cloneable {
    public enum Usage {
        AUTHENTICATION("Authentication"), TOKEN("Token"), USERINFO("User Info"), REVOCATION("Revocation"), JWKS("JWKS"),
        DISCOVERY("Discovery"), END_SESSION("End Session"), CUSTOM("Custom");
        private String label;

        Usage(String label) {
            this.label = label;
        }

        @Override
        public String toString() {
            return this.label;
        }
    }

    public enum RestPart {
        URI("Uri"), QUERY("Query"), BODY("Body");
        private String label;

        RestPart(String label) {
            this.label = label;
        }

        @Override
        public String toString() {
            return this.label;
        }
    }

    private Usage usage;
    private RestPart restpart;
    private String name;
    private String default_value;
    private boolean locked;
    private boolean required;

    public OpenIdPropertyDef() {

    }

    public static final Set<OpenIdPropertyDef> EMPTY_SET = Collections
            .unmodifiableSet(new HashSet<OpenIdPropertyDef>());

    public OpenIdPropertyDef(OpenIdPropertyDef.Usage usage, OpenIdPropertyDef.RestPart restpart, String name,
            String default_value, boolean locked, boolean required) {
        this.usage = usage;
        this.restpart = restpart;
        this.name = name;
        this.default_value = default_value;
        this.locked = locked;
        this.required = required;
    }

    public OpenIdPropertyDef(OpenIdPropertyDef.Usage usage, OpenIdPropertyDef.RestPart restpart, String name,
            String default_value) {
        this(usage, restpart, name, default_value, false, false);
    }

    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    /**
     * @return Usage return the method the property is used in
     */
    @JsonProperty("usage")
    public Usage getUsage() {
        return usage;
    }

    /**
     * @param usage which method the property is used in
     */
    @JsonProperty("usage")
    public void setUsage(Usage usage) {
        this.usage = usage;
    }

    /**
     * @return RestPart return the part of the REST call where the property will be
     *         place
     */
    @JsonProperty("restpart")
    public RestPart getRestPart() {
        return restpart;
    }

    /**
     * @param part of the REST call where the property will be place in the rest
     *             call
     */
    @JsonProperty("restpart")
    public void setRestPart(RestPart restpart) {
        this.restpart = restpart;
    }

    /**
     * @return String return the name
     */
    @JsonProperty("name")
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    @JsonProperty("name")
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return String return the value
     */
    @JsonProperty("default_value")
    public String getDefaultValue() {
        return default_value;
    }

    /**
     * @param value the value to set
     */
    @JsonProperty("default_value")
    public String setDefaultValue(String default_value) {
        return this.default_value = default_value;
    }

    /**
     * @return boolean return the if the property is locked or not
     */
    @JsonProperty("locked")
    public boolean isLocked() {
        return locked;
    }

    /**
     * @param locked lock or unlock the property
     */
    @JsonProperty("locked")
    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    /**
     * @return boolean return if the property is required or not
     */
    @JsonProperty("required")
    public boolean isRequired() {
        return required;
    }

    /**
     * @param required set if the property is required or not
     */
    @JsonProperty("required")
    public void setRequired(boolean required) {
        this.required = required;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof OpenIdPropertyDef) {
            OpenIdPropertyDef property = (OpenIdPropertyDef) obj;
            return this.getName().equals(property.getName()) && this.getUsage().equals(property.getUsage());
        } else if (obj instanceof String) {
            String name = (String) obj;
            return this.getName().equals(name);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return this.getName().hashCode() | this.getUsage().hashCode();
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        result.append("[");
        result.append(getUsage());
        result.append("@");
        result.append(getRestPart());
        result.append("]");
        result.append(getName());
        result.append(":");
        if (isRequired()) {
            if (isLocked()) {
                result.append("R,L ");
            } else {
                result.append("R ");
            }
        } else if (isLocked()) {
            result.append("L ");
        } else {
            result.append(" ");
        }
        if (this.default_value != null) {
            result.append("(\"");
            result.append(this.default_value);
            result.append("\")");
        }
        return result.toString();
    }
}