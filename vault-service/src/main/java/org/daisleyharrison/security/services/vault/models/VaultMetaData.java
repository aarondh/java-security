
package org.daisleyharrison.security.services.vault.models;

import com.fasterxml.jackson.annotation.JsonProperty;

public class VaultMetaData {
    private static final int DEFAULT_KEY_TTL_IN_SECONDS = 10;

    public VaultMetaData() {
        keyTTL = DEFAULT_KEY_TTL_IN_SECONDS;
    }

    @JsonProperty("keyStore")
    private KeyStoreMetaData keyStore;
    @JsonProperty("masterCypher")
    private CypherMetaData masterCypher;
    @JsonProperty("tokenSignatureCypher")
    private CypherMetaData tokenSignatureCypher;
    @JsonProperty("keyTTL")
    private int keyTTL;
    @JsonProperty("issuerDN")
    private String issuerDN;
    @JsonProperty("subjectDN")
    private String subjectDN;
    @JsonProperty("userPrincipleTTL")
    private int userPrincipleTTL;
    @JsonProperty("servicePrincipleTTL")
    private int servicePrincipleTTL;
    @JsonProperty("userPrincipleTokenTTL")
    private int userPrincipleTokenTTL;
    @JsonProperty("servicePrincipleTokenTTL")
    private int servicePrincipleTokenTTL;

    /**
     * @return KeyStore return the keystore
     */
    public KeyStoreMetaData getKeyStore() {
        return keyStore;
    }

    /**
     * @param keystore the keystore to set
     */
    public void setKeyStore(KeyStoreMetaData keystore) {
        this.keyStore = keystore;
    }

    /**
     * @return int return the keyTTL
     */
    public int getKeyTTL() {
        return keyTTL;
    }

    /**
     * @param keyTTL the keyTTL to set
     */
    public void setKeyTTL(int keyTTL) {
        this.keyTTL = keyTTL;
    }

    public CypherMetaData getMasterCypher() {
        return masterCypher;
    }

    public void setMasterCypher(CypherMetaData masterCypher) {
        this.masterCypher = masterCypher;
    }

    /**
     * @return int return the userPrincipleTTL
     */
    public int getUserPrincipleTTL() {
        return userPrincipleTTL;
    }

    /**
     * @param userPrincipleTTL the userPrincipleTTL to set
     */
    public void setUserPrincipleTTL(int userPrincipleTTL) {
        this.userPrincipleTTL = userPrincipleTTL;
    }

    /**
     * @return int return the servicePrincipleTTL
     */
    public int getServicePrincipleTTL() {
        return servicePrincipleTTL;
    }

    /**
     * @param servicePrincipleTTL the servicePrincipleTTL to set
     */
    public void setServicePrincipleTTL(int servicePrincipleTTL) {
        this.servicePrincipleTTL = servicePrincipleTTL;
    }

    /**
     * @return String return the issuerDN
     */
    public String getIssuerDN() {
        return issuerDN;
    }

    /**
     * @param issuerDN the issuerDN to set
     */
    public void setIssuerDN(String issuerDN) {
        this.issuerDN = issuerDN;
    }

    /**
     * @return String return the subjectDN
     */
    public String getSubjectDN() {
        return subjectDN;
    }

    /**
     * @param subjectDN the subjectDN to set
     */
    public void setSubjectDN(String subjectDN) {
        this.subjectDN = subjectDN;
    }

    /**
     * @return int return the userPrincipleTokenTTL
     */
    public int getUserPrincipleTokenTTL() {
        return userPrincipleTokenTTL;
    }

    /**
     * @param userPrincipleTokenTTL the userPrincipleTokenTTL to set
     */
    public void setUserPrincipleTokenTTL(int userPrincipleTokenTTL) {
        this.userPrincipleTokenTTL = userPrincipleTokenTTL;
    }

    /**
     * @return int return the servicePrincipleTokenTTL
     */
    public int getServicePrincipleTokenTTL() {
        return servicePrincipleTokenTTL;
    }

    /**
     * @param servicePrincipleTokenTTL the servicePrincipleTokenTTL to set
     */
    public void setServicePrincipleTokenTTL(int servicePrincipleTokenTTL) {
        this.servicePrincipleTokenTTL = servicePrincipleTokenTTL;
    }

    /**
     * @return CypherMetaData return the tokenSignatureCypher
     */
    public CypherMetaData getTokenSignatureCypher() {
        return tokenSignatureCypher;
    }

    /**
     * @param tokenSignatureCypher the tokenSignatureCypher to set
     */
    public void setTokenSignatureCypher(CypherMetaData tokenSignatureCypher) {
        this.tokenSignatureCypher = tokenSignatureCypher;
    }

}