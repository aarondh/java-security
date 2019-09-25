package org.daisleyharrison.security.common.models.openId;

import com.fasterxml.jackson.annotation.JsonProperty;

public class OpenIdIssuer implements Cloneable {

    @JsonProperty("issuer")
    private String issuer;
    @JsonProperty("discovery_endpoint")
    private String discovery_endpoint;
    @JsonProperty("configuration")
    private OpenIdIssuerConfig configuration;
    @JsonProperty("properties")
    private OpenIdPropertyDef[] properties;
    @JsonProperty("token_jsonRequestSupported")
    private boolean token_jsonRequestSupported;

    public OpenIdIssuer() {

    }

    public Object clone() throws CloneNotSupportedException {
        OpenIdIssuer clone = (OpenIdIssuer) super.clone();
        if (this.getConfiguration() != null) {
            clone.setConfiguration((OpenIdIssuerConfig) this.getConfiguration().clone());
        }
        if (this.getConfiguration() != null) {
            clone.setConfiguration((OpenIdIssuerConfig) this.getConfiguration().clone());
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

    @JsonProperty("issuer")
    public String getIssuer() {
        return issuer;
    }

    @JsonProperty("issuer")
    public void setIssuer(String issuer) {
        this.issuer = issuer;
    }

    @JsonProperty("token_jsonRequestSupported")
    public boolean isTokenJsonRequestBodySupported() {
        return token_jsonRequestSupported;
    }

    @JsonProperty("token_jsonRequestSupported")
    public void setTokenJsonRequestBodySupported(boolean token_jsonRequestSupported) {
        this.token_jsonRequestSupported = token_jsonRequestSupported;
    }

    @JsonProperty("discovery_endpoint")
    public String getDiscoveryEndpoint() {
        return discovery_endpoint;
    }

    @JsonProperty("discovery_endpoint")
    public void setDiscoveryEndpoint(String discovery_endpoint) {
        this.discovery_endpoint = discovery_endpoint;
    }

    /**
     * @return OpenIdIssuerConfig return the issuer's configuration
     */
    @JsonProperty("configuration")
    public OpenIdIssuerConfig getConfiguration() {
        return configuration;
    }

    /**
     * @param configuration the issuer's configuration to set
     */
    @JsonProperty("configuration")
    public void setConfiguration(OpenIdIssuerConfig configuration) {
        this.configuration = configuration;
    }

    /**
     * @return Map<String,String> return the properties
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
