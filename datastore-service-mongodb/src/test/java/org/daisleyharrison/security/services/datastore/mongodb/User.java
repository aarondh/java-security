package org.daisleyharrison.security.services.datastore.mongodb;

import java.util.Date;

import org.jongo.marshall.jackson.oid.MongoId;
import org.jongo.marshall.jackson.oid.MongoObjectId;

public class User {
    //  @MongoObjectId
    // @MongoId
    private String _id;
    private String id;
    private String given_name;
    private String middle_name;
    private String family_name;
    private String nickname;
    private String preferred_username;
    private String profile;
    private String picture;
    private String website;
    private boolean email_verified;
    private String gender;
    private Date birthdate;
    private String zoneinfo;
    private String locale;
    private String phone_number;
    private boolean phone_number_verified;
    private Address address;
    private String email;
    private Date updated_at;
    private Date created_at;

    /**
     * @return String return the id
     */
    public String get_id() {
        return _id;
    }
    /**
     * @param _id the _id to set
     */
    public void set_id(String _id) {
        this._id = _id;
    }
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
     * @return String return the given_name
     */
    public String getGiven_name() {
        return given_name;
    }

    /**
     * @param given_name the given_name to set
     */
    public void setGiven_name(String given_name) {
        this.given_name = given_name;
    }

    /**
     * @return String return the middle_name
     */
    public String getMiddle_name() {
        return middle_name;
    }

    /**
     * @param middle_name the middle_name to set
     */
    public void setMiddle_name(String middle_name) {
        this.middle_name = middle_name;
    }

    /**
     * @return String return the family_name
     */
    public String getFamily_name() {
        return family_name;
    }

    /**
     * @param family_name the family_name to set
     */
    public void setFamily_name(String family_name) {
        this.family_name = family_name;
    }

    /**
     * @return String return the nickname
     */
    public String getNickname() {
        return nickname;
    }

    /**
     * @param nickname the nickname to set
     */
    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    /**
     * @return String return the preferred_username
     */
    public String getPreferred_username() {
        return preferred_username;
    }

    /**
     * @param preferred_username the preferred_username to set
     */
    public void setPreferred_username(String preferred_username) {
        this.preferred_username = preferred_username;
    }

    /**
     * @return String return the profile
     */
    public String getProfile() {
        return profile;
    }

    /**
     * @param profile the profile to set
     */
    public void setProfile(String profile) {
        this.profile = profile;
    }

    /**
     * @return String return the picture
     */
    public String getPicture() {
        return picture;
    }

    /**
     * @param picture the picture to set
     */
    public void setPicture(String picture) {
        this.picture = picture;
    }

    /**
     * @return String return the website
     */
    public String getWebsite() {
        return website;
    }

    /**
     * @param website the website to set
     */
    public void setWebsite(String website) {
        this.website = website;
    }

    /**
     * @return boolean return the email_verified
     */
    public boolean isEmail_verified() {
        return email_verified;
    }

    /**
     * @param email_verified the email_verified to set
     */
    public void setEmail_verified(boolean email_verified) {
        this.email_verified = email_verified;
    }

    /**
     * @return String return the gender
     */
    public String getGender() {
        return gender;
    }

    /**
     * @param gender the gender to set
     */
    public void setGender(String gender) {
        this.gender = gender;
    }

    /**
     * @return Date return the birthdate
     */
    public Date getBirthdate() {
        return birthdate;
    }

    /**
     * @param birthdate the birthdate to set
     */
    public void setBirthdate(Date birthdate) {
        this.birthdate = birthdate;
    }

    /**
     * @return String return the zoneinfo
     */
    public String getZoneinfo() {
        return zoneinfo;
    }

    /**
     * @param zoneinfo the zoneinfo to set
     */
    public void setZoneinfo(String zoneinfo) {
        this.zoneinfo = zoneinfo;
    }

    /**
     * @return String return the locale
     */
    public String getLocale() {
        return locale;
    }

    /**
     * @param locale the locale to set
     */
    public void setLocale(String locale) {
        this.locale = locale;
    }

    /**
     * @return String return the phone_number
     */
    public String getPhone_number() {
        return phone_number;
    }

    /**
     * @param phone_number the phone_number to set
     */
    public void setPhone_number(String phone_number) {
        this.phone_number = phone_number;
    }

    /**
     * @return boolean return the phone_number_verified
     */
    public boolean isPhone_number_verified() {
        return phone_number_verified;
    }

    /**
     * @param phone_number_verified the phone_number_verified to set
     */
    public void setPhone_number_verified(boolean phone_number_verified) {
        this.phone_number_verified = phone_number_verified;
    }

    /**
     * @return Address return the address
     */
    public Address getAddress() {
        return address;
    }

    /**
     * @param address the address to set
     */
    public void setAddress(Address address) {
        this.address = address;
    }

    /**
     * @return String return the email
     */
    public String getEmail() {
        return email;
    }

    /**
     * @param email the email to set
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * @return String return the updated_at
     */
    public Date getUpdated_at() {
        return updated_at;
    }

    /**
     * @param updated_at the updated_at to set
     */
    public void setUpdated_at(Date updated_at) {
        this.updated_at = updated_at;
    }

    /**
     * @return Date return the created_at
     */
    public Date getCreated_at() {
        return created_at;
    }

    /**
     * @param created_at the created_at to set
     */
    public void setCreated_at(Date created_at) {
        this.created_at = created_at;
    }

}