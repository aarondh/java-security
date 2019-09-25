package org.daisleyharrison.security.services.tokenizer.models;

import org.daisleyharrison.security.common.models.authorization.TokenMetaData;
import org.daisleyharrison.security.common.spi.ConfigurationServiceProvider;

public class WebTokenDefinition implements TokenMetaData {
    private static final int DEFAULT_EXPIRES_IN_MINUTES = 15;
    private static final int DEFAULT_NOT_BEFORE_IN_MINUTES = 2;

    private String name;
    private Type type;
    private String algHeader;
    private String contentEncryptionAlg;
    private String key;
    private String issuer;
    private String audience;
    private int expires;
    private int notBefore;

    public WebTokenDefinition(ConfigurationServiceProvider config, String prefix, String definitionName) {
        String propertyPrefix = prefix + "." + definitionName;
        setName(definitionName);
        setType(config.getValueOfType(propertyPrefix + ".type", Type.class, null ));
        setAlgHeader(config.getValue(propertyPrefix + ".alg-header", null));
        setContentEncryptionAlg(config.getValue(propertyPrefix + ".content-encryption-alg", null));
        setIssuer(config.getValue(propertyPrefix + ".issuer", null));
        setAudience(config.getValue(propertyPrefix + ".audience", null));
        setKey(config.getValue(propertyPrefix + ".key", null));
        setExpires(config.getIntegerValue(propertyPrefix + ".expires", DEFAULT_EXPIRES_IN_MINUTES));
        setNotBefore(config.getIntegerValue(propertyPrefix + ".not-before", DEFAULT_NOT_BEFORE_IN_MINUTES));

    }

    /**
     * @return String return the key
     */
    public String getKey() {
        return key;
    }

    /**
     * @param key the key to set
     */
    public void setKey(String key) {
        this.key = key;
    }

    public String getAudience() {
        return audience;
    }

    public void setAudience(String audience) {
        this.audience = audience;
    }

    public String getIssuer() {
        return issuer;
    }

    public void setIssuer(String issuer) {
        this.issuer = issuer;
    }

    public int getExpires() {
        return expires;
    }

    public void setExpires(int expires) {
        this.expires = expires;
    }

    public int getNotBefore() {
        return notBefore;
    }

    public void setNotBefore(int notBefore) {
        this.notBefore = notBefore;
    }

    @Override
    public String toString() {
        return "JwtDefinition{" + name + "}";
    }

    /**
     * @return String return the contentEncryptionAlg
     */
    public String getContentEncryptionAlg() {
        return contentEncryptionAlg;
    }

    /**
     * @param contentEncryptionAlg the contentEncryptionAlg to set
     */
    public void setContentEncryptionAlg(String contentEncryptionAlg) {
        this.contentEncryptionAlg = contentEncryptionAlg;
    }


    /**
     * @return String return the algHeader
     */
    public String getAlgHeader() {
        return algHeader;
    }

    /**
     * @param algHeader the algHeader to set
     */
    public void setAlgHeader(String algHeader) {
        this.algHeader = algHeader;
    }


    /**
     * @return String return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @param type the type to set
     */
    public void setType(Type type) {
        this.type = type;
    }

    /**
     * @return Type return the type
     */
    public Type getType() {
        return type;
    }

}