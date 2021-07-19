package me.brennan.divvy.config.impl;

import com.google.gson.annotations.SerializedName;

/**
 * @author Brennan
 * @since 6/28/21
 **/
public class Account {

    @SerializedName("email")
    private String email;

    @SerializedName("password")
    private String password;

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }
}
