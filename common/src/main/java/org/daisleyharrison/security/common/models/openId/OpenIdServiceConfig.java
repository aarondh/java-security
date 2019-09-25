package org.daisleyharrison.security.common.models.openId;

import javax.json.bind.annotation.JsonbProperty;

import com.fasterxml.jackson.annotation.JsonProperty;

public class OpenIdServiceConfig implements Cloneable {
    @JsonbProperty("issuers")
    @JsonProperty("issuers")
    private OpenIdIssuer[] issuers;
    @JsonbProperty("properties")
    @JsonProperty("properties")
    private OpenIdPropertyDef[] properties;

    public OpenIdServiceConfig() {
        this.properties = new OpenIdPropertyDef[0];
    }

    public Object clone() throws CloneNotSupportedException {
        OpenIdServiceConfig clone = (OpenIdServiceConfig) super.clone();
        OpenIdIssuer[] issuers = this.getIssuers();
        if (issuers != null) {
            OpenIdIssuer[] clonedIssuers = new OpenIdIssuer[issuers.length];
            for (int i = 0; i < issuers.length; i++) {
                clonedIssuers[i] = (OpenIdIssuer) issuers[i].clone();
            }
            clone.setIssuers(clonedIssuers);
        }
        OpenIdPropertyDef[] properties = this.getProperties();
        if (properties != null) {
            OpenIdPropertyDef[] clonedProperties = new OpenIdPropertyDef[properties.length];
            for (int i = 0; i < properties.length; i++) {
                clonedProperties[i] = (OpenIdPropertyDef) properties[i].clone();
            }
            clone.setProperties(clonedProperties);
        }
        return clone;
    }

    /**
     * @return OpenIdIssuer[] return the issuers
     */
    public OpenIdIssuer[] getIssuers() {
        return issuers;
    }

    /**
     * @param issuers the issuers to set
     */
    public void setIssuers(OpenIdIssuer[] issuers) {
        this.issuers = issuers;
    }

    /**
     * @return OpenIdPropertyDef[] return the issuers
     */
    public OpenIdPropertyDef[] getProperties() {
        return properties;
    }

    /**
     * @param properties the properties to set
     */
    public void setProperties(OpenIdPropertyDef[] properties) {
        this.properties = properties;
    }

}