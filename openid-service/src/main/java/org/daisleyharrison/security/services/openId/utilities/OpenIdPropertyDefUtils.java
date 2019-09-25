package org.daisleyharrison.security.services.openId.utilities;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.daisleyharrison.security.common.models.openId.OpenIdPropertyDef;

public class OpenIdPropertyDefUtils {

    public static class UsageKey {
        private OpenIdPropertyDef.Usage usage;
        private String name;

        public UsageKey(OpenIdPropertyDef.Usage usage, String name) {
            this.usage = usage;
            this.name = name;
        }

        public UsageKey(OpenIdPropertyDef propertyDef) {
            this.usage = propertyDef.getUsage();
            this.name = propertyDef.getName();
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof UsageKey) {
                UsageKey test = (UsageKey) obj;
                return this.usage.equals(test.usage) && this.name.equals(test.name);
            }
            return false;
        }

        @Override
        public int hashCode() {
            return this.usage.hashCode() | this.name.hashCode();
        }

        @Override
        public String toString() {
            return "(" + this.usage.toString() + "," + this.name + ")";
        }
    }

    /**
     * Merge two sets of property definitions Note if a property definition in set A
     * is locked this function will not override it (an exception will be thrown if
     * throwOnError is true)
     * 
     * Properties are merged based on the compound key (usage,name)
     * 
     * @param propertyDefsA The first set of property definitions
     * @param propertyDefsB The set of property definitions to override the
     *                      properties in set A (if the property is not locked)
     * @param throwOnError  if true an exception with occur if a property definition
     *                      in set B attempts to override a locked property in set A
     * @return Set<OpenIdPropertyDef> the merged set of property definitions
     */
    public static Set<OpenIdPropertyDef> merge(Set<OpenIdPropertyDef> propertyDefsA,
            Set<OpenIdPropertyDef> propertyDefsB, boolean throwOnError) {
        if (propertyDefsA == null) {
            return propertyDefsB;
        } else if (propertyDefsB == null) {
            return propertyDefsA;
        }

        Map<UsageKey, OpenIdPropertyDef> merged = new HashMap<>();

        for (OpenIdPropertyDef propertyA : propertyDefsA) {
            merged.put(new UsageKey(propertyA), propertyA);
        }
        for (OpenIdPropertyDef propertyB : propertyDefsB) {
            UsageKey key = new UsageKey(propertyB);
            OpenIdPropertyDef propertyA = merged.get(key);
            if (propertyA == null) {
                merged.put(key, propertyB);
            } else if (!propertyA.equals(propertyB)) {
                if (propertyA.isLocked()) {
                    if (throwOnError) {
                        throw new IllegalArgumentException(
                                "property \"" + propertyA.getName() + "\" is locked and cannot be overridden.");
                    } else {
                        merged.put(key, propertyB);
                    }
                }
            }
        }
        Set<OpenIdPropertyDef> mergedPropertyDefs = new HashSet<>();
        merged.forEach((key, value) -> {
            mergedPropertyDefs.add(value);
        });
        return mergedPropertyDefs;
    }

    /**
     * Merge two arrays of property definitions Note if the property definitions in
     * array A are locked this function will not override it (an exception will be
     * thrown if throwOnError is true)
     * 
     * @param propertyDefsA The first array of property definitions
     * @param propertyDefsB
     * @param throwOnError  if true an exception with occur if a property definition
     *                      in array B attempts to override a locked property in
     *                      array A
     * @return OpenIdPropertyDef[] the merged array of property definitions
     */
    public static OpenIdPropertyDef[] merge(OpenIdPropertyDef[] propertyDefsA, OpenIdPropertyDef[] propertyDefsB,
            boolean throwOnError) {
        return toArray(merge(toSet(propertyDefsA), toSet(propertyDefsB), throwOnError));
    }

    public static OpenIdPropertyDef[] toArray(Set<OpenIdPropertyDef> propertySet) {
        return propertySet.toArray(OpenIdPropertyDef[]::new);
    }

    public static Set<OpenIdPropertyDef> toSet(OpenIdPropertyDef[] propertyArray) {
        return Set.<OpenIdPropertyDef>of(propertyArray);
    }

    public static Set<OpenIdPropertyDef> propertyDefsFor(OpenIdPropertyDef.Usage usage,
            Set<OpenIdPropertyDef> propertyDefSet) {
        Set<OpenIdPropertyDef> propertyDefsFor = new HashSet<>();
        propertyDefSet.forEach(propertyDef -> {
            if (propertyDef.getUsage().equals(usage)) {
                propertyDefsFor.add(propertyDef);
            }
        });
        return propertyDefsFor;
    }

    public static Set<OpenIdPropertyDef> propertyDefsFor(OpenIdPropertyDef.Usage usage,
            OpenIdPropertyDef[] propertyDefArray) {
        return propertyDefsFor(usage, toSet(propertyDefArray));
    }

    public static String createRequiredMissingMessage(Set<OpenIdPropertyDef> missing) {
        StringBuilder message = new StringBuilder();
        if (missing.size() == 1) {
            missing.forEach(definition -> {
                message.append(String.format("property \"%s\" is required but not set.", definition.getName()));
            });
        } else {
            boolean isFirst = true;
            message.append("properties ");
            for (OpenIdPropertyDef definition : missing) {
                if (isFirst) {
                    isFirst = false;
                } else {
                    message.append(", ");
                }
                message.append("\"");
                message.append(definition.getName());
                message.append("\"");
            }

            message.append(" are required but not set.");
        }
        return message.toString();
    }

}