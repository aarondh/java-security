package org.daisleyharrison.security.samples.spring.microservices.shared.datafeed;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonProperty;

public class DatafeedMetaData {
    public static final int DEFAULT_MAX_ALLOWED_ERRORS = 20;

    public enum Status {
        @JsonProperty("undefined")
        UNDEFINED, @JsonProperty("error")
        ERROR, @JsonProperty("processing")
        PROCESSING, @JsonProperty("complete")
        COMPLETE, @JsonProperty("aborted")
        ABORTED

    }

    private String id;
    private String targetCollection;
    private String feedPath;
    private String feedOrganization;
    private String feedType;
    private String feedSchema;
    private String feedSchemaVersion;
    private String feedVersion;
    private String feedTimestamp;
    private int items;
    private int errors;
    private int inserts;
    private int updates;
    private int deletions;
    private int processed;
    private int exist;
    private Date processDate;
    private Status status;
    private int maxErrorsAllowed;

    public DatafeedMetaData() {
        this.processDate = new Date();
        this.status = Status.UNDEFINED;
        this.maxErrorsAllowed = DEFAULT_MAX_ALLOWED_ERRORS;
    }

    public int getProcessed() {
        return processed;
    }

    public void setProcessed(int processed) {
        this.processed = processed;
    }

    public String getFeedTimestamp() {
        return feedTimestamp;
    }

    public void setFeedTimestamp(String feedTimestamp) {
        this.feedTimestamp = feedTimestamp;
    }

    public String getFeedSchemaVersion() {
        return feedSchemaVersion;
    }

    public void setFeedSchemaVersion(String feedSchemaVersion) {
        this.feedSchemaVersion = feedSchemaVersion;
    }

    public String getFeedOrganization() {
        return feedOrganization;
    }

    public void setFeedOrganization(String feedOrganization) {
        this.feedOrganization = feedOrganization;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public String getFeedPath() {
        return feedPath;
    }

    public void setFeedPath(String feedPath) {
        this.feedPath = feedPath;
    }

    public String getId() {
        return id;
    }

    public Date getProcessDate() {
        return processDate;
    }

    public void setProcessDate(Date processDate) {
        this.processDate = processDate;
    }

    public int getDeletions() {
        return deletions;
    }

    public void setDeletions(int deletions) {
        this.deletions = deletions;
    }

    public int getUpdates() {
        return updates;
    }

    public void setUpdates(int updates) {
        this.updates = updates;
    }

    public int getInserts() {
        return inserts;
    }

    public void setInserts(int inserts) {
        this.inserts = inserts;
    }

    public int getErrors() {
        return errors;
    }

    public void setErrors(int errors) {
        this.errors = errors;
    }

    public int getItems() {
        return items;
    }

    public void setItems(int items) {
        this.items = items;
    }

    public String getFeedVerison() {
        return feedVersion;
    }

    public void setFeedVersion(String feedVersion) {
        this.feedVersion = feedVersion;
    }

    public String getFeedSchema() {
        return feedSchema;
    }

    public void setFeedSchema(String feedSchema) {
        this.feedSchema = feedSchema;
    }

    public String getFeedType() {
        return feedType;
    }

    public void setFeedType(String feedType) {
        this.feedType = feedType;
    }

    public String getTargetCollection() {
        return targetCollection;
    }

    public void setTargetCollection(String targetCollection) {
        this.targetCollection = targetCollection;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int incrementItems() {
        return this.items++;
    }

    public int incrementInserts() {
        return this.inserts++;
    }

    public int incrementUpdates() {
        return this.updates++;
    }

    public int incrementDeletions() {
        return this.deletions++;
    }

    public int incrementExist() {
        return this.exist++;
    }

    public int incrementErrors() {
        return this.errors++;
    }

    public int incrementProcessed() {
        return this.processed++;
    }

    /**
     * @return int return the maxErrorsAllowed
     */
    public int getMaxErrorsAllowed() {
        return maxErrorsAllowed;
    }

    /**
     * @param maxErrorsAllowed the maxErrorsAllowed to set
     */
    public void setMaxErrorsAllowed(int maxErrorsAllowed) {
        this.maxErrorsAllowed = maxErrorsAllowed;
    }


    /**
     * @return String return the feedVersion
     */
    public String getFeedVersion() {
        return feedVersion;
    }

    /**
     * @return int return the exist
     */
    public int getExist() {
        return exist;
    }

    /**
     * @param exist the exist to set
     */
    public void setExist(int exist) {
        this.exist = exist;
    }

}