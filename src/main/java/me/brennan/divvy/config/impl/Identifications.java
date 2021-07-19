package me.brennan.divvy.config.impl;

import com.google.gson.annotations.SerializedName;

/**
 * @author Brennan
 * @since 6/28/21
 **/
public class Identifications {

    @SerializedName("owner_id")
    private String ownerID;

    @SerializedName("company_id")
    private String companyID;

    public String getOwnerID() {
        return ownerID;
    }

    public String getCompanyID() {
        return companyID;
    }
}
