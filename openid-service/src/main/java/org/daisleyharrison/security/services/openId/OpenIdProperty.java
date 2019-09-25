package org.daisleyharrison.security.services.openId;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.daisleyharrison.security.common.models.openId.OpenIdPropertyDef;
import org.daisleyharrison.security.common.models.openId.OpenIdPropertyDef.RestPart;
import org.daisleyharrison.security.common.models.openId.OpenIdPropertyDef.Usage;

public class OpenIdProperty {
    private OpenIdPropertyDef definition;
    private char[] values;
    private String value;

    public OpenIdProperty(OpenIdPropertyDef definition, String value) {
        this.definition = definition;
        this.value = value;
    }

    public OpenIdProperty(OpenIdPropertyDef definition, char[] values) {
        this.definition = definition;
        this.values = values;
    }

    public OpenIdProperty(OpenIdPropertyDef definition) {
        this.definition = definition;
    }

    public OpenIdPropertyDef getDefinition() {
        return this.definition;
    }

    public boolean isDefinedBy(OpenIdPropertyDef definition) {
        return this.definition.equals(definition);
    }

    public String getName() {
        return this.definition.getName();
    }

    public String getValue() {
        if (this.value == null) {
            if (this.values == null) {
                return this.definition.getDefaultValue();
            } else {
                return new String(this.values);
            }
        } else {
            return this.value;
        }
    }

    public Usage getUsage() {
        return this.definition.getUsage();
    }

    public RestPart getRestPart() {
        return this.definition.getRestPart();
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        result.append("(");
        result.append(getDefinition());
        result.append("): ");
        if (this.value != null) {
            result.append("\"");
            result.append(this.value);
            result.append("\"");
        } else if (this.value != null) {
            result.append("\"");
            result.append("*".repeat(this.values.length));
            result.append("\"");
        } else {
            String value = getDefinition().getDefaultValue();
            if (value == null) {
                result.append("null");
            } else {
                result.append("\"");
                result.append(value);
                result.append("\"(default)");
            }
        }
        result.append(getRestPart());
        return result.toString();
    }

    public static Set<OpenIdProperty> propertiesFor(OpenIdPropertyDef.Usage usage, Set<OpenIdProperty> properties) {
        Set<OpenIdProperty> propertiesFor = new HashSet<>();
        properties.forEach(property -> {
            if (property.getUsage().equals(usage)) {
                propertiesFor.add(property);
            }
        });

        return propertiesFor;
    }

    public static Map<String, String> propertyMapFor(OpenIdPropertyDef.Usage usage, Set<OpenIdProperty> properties) {
        return toMap(propertiesFor(usage, properties));
    }

    public static Set<OpenIdProperty> propertiesFor(OpenIdPropertyDef.RestPart restpart,
            Set<OpenIdProperty> properties) {
        Set<OpenIdProperty> propertiesFor = new HashSet<>();
        properties.forEach(property -> {
            if (property.getRestPart().equals(restpart)) {
                propertiesFor.add(property);
            }
        });

        return propertiesFor;
    }

    public static Map<String, String> propertyMapFor(OpenIdPropertyDef.RestPart restpart,
            Set<OpenIdProperty> properties) {
        return toMap(propertiesFor(restpart, properties));
    }

    public static Map<String, String> toMap(Set<OpenIdProperty> properties) {
        Map<String, String> map = new HashMap<>();
        properties.forEach(property -> {
            map.put(property.getName(), property.getValue());
        });
        return map;
    }

}
