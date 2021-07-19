package me.brennan.divvy.config.impl;

import com.google.gson.annotations.SerializedName;

/**
 * @author Brennan
 * @since 6/28/21
 **/
public class Settings {

    @SerializedName("time_out")
    private int timeout;

    public int getTimeout() {
        return timeout;
    }
}
