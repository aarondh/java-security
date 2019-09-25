package org.daisleyharrison.security.services.datastore.mongodb;

public class Address {
    private String street_address;
    private String locality;
    private String postal_code;
    private String country;

    /**
     * @return String return the street_address
     */
    public String getStreet_address() {
        return street_address;
    }

    /**
     * @param street_address the street_address to set
     */
    public void setStreet_address(String street_address) {
        this.street_address = street_address;
    }

    /**
     * @return String return the locality
     */
    public String getLocality() {
        return locality;
    }

    /**
     * @param locality the locality to set
     */
    public void setLocality(String locality) {
        this.locality = locality;
    }

    /**
     * @return String return the postal_code
     */
    public String getPostal_code() {
        return postal_code;
    }

    /**
     * @param postal_code the postal_code to set
     */
    public void setPostal_code(String postal_code) {
        this.postal_code = postal_code;
    }

    /**
     * @return String return the country
     */
    public String getCountry() {
        return country;
    }

    /**
     * @param country the country to set
     */
    public void setCountry(String country) {
        this.country = country;
    }

}