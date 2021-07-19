package me.brennan.divvy.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;
import me.brennan.divvy.config.impl.Account;
import me.brennan.divvy.config.impl.CardSettings;
import me.brennan.divvy.config.impl.Identifications;
import me.brennan.divvy.config.impl.Settings;

/**
 * @author Brennan
 * @since 6/28/21
 **/
public class Config {

    @SerializedName("identifications")
    private Identifications identifications;

    @SerializedName("account")
    private Account account;

    @SerializedName("card_settings")
    private CardSettings cardSettings;

    @SerializedName("settings")
    private Settings settings;

    public Identifications getIdentifications() {
        return identifications;
    }

    public Account getAccount() {
        return account;
    }

    public CardSettings getCardSettings() {
        return cardSettings;
    }

    public Settings getSettings() {
        return settings;
    }
}
