
package org.daisleyharrison.security.services.vault.models;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonProperty;

public class NamespaceMetaData {
    private NamespaceMetaData parent;
    @JsonProperty("defaultTTL")
    private int defaultTTL;
    @JsonProperty("cypher")
    private CypherMetaData cypher;
    @JsonProperty("created")
    private Date created;
    @JsonProperty("deleted")
    private Date deleted;
    @JsonProperty("version")
    private int version;
    @JsonProperty("destroyed")
    private boolean destroyed;
    @JsonProperty("createdBy")
    private String createdBy;
    @JsonProperty("deletedBy")
    private String deletedBy;
    @JsonProperty("root")
    private boolean root;

    public NamespaceMetaData() {
        this.created = new Date();
    }

    /**
     * @return NamespaceMetaData return the parent
     */
    public NamespaceMetaData getParent() {
        if (root) {
            return null;
        }
        return parent;
    }

    /**
     * @param parent the parent to set
     */
    public void setParent(NamespaceMetaData parent) {
        this.parent = parent;
    }

    /**
     * @return int return the defaultTTL
     */
    public int getDefaultTTL() {
        return defaultTTL;
    }

    /**
     * @param defaultTTL the defaultTTL to set
     */
    public void setDefaultTTL(int defaultTTL) {
        this.defaultTTL = defaultTTL;
    }

    /**
     * @return Date return the created
     */
    public Date getCreated() {
        return created;
    }

    /**
     * @param created the created to set
     */
    public void setCreated(Date created) {
        this.created = created;
    }

    /**
     * @return Date return the deleted
     */
    public Date getDeleted() {
        return deleted;
    }

    /**
     * @param deleted the deleted to set
     */
    public void setDeleted(Date deleted) {
        this.deleted = deleted;
    }

    /**
     * @return int return the version
     */
    public int getVersion() {
        return version;
    }

    /**
     * @param version the version to set
     */
    public void setVersion(int version) {
        this.version = version;
    }

    /**
     * @return boolean return the destroyed
     */
    public boolean isDestroyed() {
        return destroyed;
    }

    /**
     * @param destroyed the destroyed to set
     */
    public void setDestroyed(boolean destroyed) {
        this.destroyed = destroyed;
    }

    /**
     * @return String return the createdBy
     */
    public String getCreatedBy() {
        return createdBy;
    }

    /**
     * @param createdBy the createdBy to set
     */
    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    /**
     * @return String return the deletedBy
     */
    public String getDeletedBy() {
        return deletedBy;
    }

    /**
     * @param deletedBy the deletedBy to set
     */
    public void setDeletedBy(String deletedBy) {
        this.deletedBy = deletedBy;
    }

    /**
     * @return boolean return the root
     */
    public boolean isRoot() {
        return root;
    }

    /**
     * @param root the root to set
     */
    public void setRoot(boolean root) {
        this.root = root;
    }

    /**
     * @return CypherMetaData return the cypher
     */
    public CypherMetaData getCypher() {
        if (cypher == null) {
            NamespaceMetaData parent = getParent();
            if (parent == null) {
                return null;
            } else {
                return parent.getCypher();
            }
        }
        return cypher;
    }

    /**
     * @param cypher the cypher to set
     */
    public void setCypher(CypherMetaData cypher) {
        this.cypher = cypher;
    }

}