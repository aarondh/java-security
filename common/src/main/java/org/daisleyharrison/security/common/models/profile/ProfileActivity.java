package org.daisleyharrison.security.common.models.profile;

import java.util.Date;

public class ProfileActivity {
    private String id;
    private String profileId;
    private String activityType;
    private String client;
    private Date created;

    /**
     * @return String return the id
     */
    public String getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * @return String return the profileId
     */
    public String getProfileId() {
        return profileId;
    }

    /**
     * @param profileId the profileId to set
     */
    public void setProfileId(String profileId) {
        this.profileId = profileId;
    }

    /**
     * @return String return the activityType
     */
    public String getActivityType() {
        return activityType;
    }

    /**
     * @param activityType the activityType to set
     */
    public void setActivityType(String activityType) {
        this.activityType = activityType;
    }

    /**
     * @return String return the client
     */
    public String getClient() {
        return client;
    }

    /**
     * @param client the client to set
     */
    public void setClient(String client) {
        this.client = client;
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

}