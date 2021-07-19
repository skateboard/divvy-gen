package me.brennan.divvy.config.impl;

import com.google.gson.annotations.SerializedName;

/**
 * @author Brennan
 * @since 6/28/21
 **/
public class CardSettings {

    @SerializedName("name")
    private String name;

    public String getName() {
        return name;
    }
}
