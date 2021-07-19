package me.brennan.divvy;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.opencsv.CSVWriter;
import me.brennan.divvy.api.APIManager;
import me.brennan.divvy.config.Config;
import me.brennan.divvy.models.Budget;
import me.brennan.divvy.models.Card;
import me.brennan.divvy.models.User;
import me.brennan.divvy.utils.CustomCookieJar;
import okhttp3.OkHttpClient;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.List;

/**
 * @author Brennan
 * @since 6/28/21
 **/
public enum DivvyGen {
    INSTANCE;

    private Config config;
    private String accessToken;
    private User currentUser;

    private APIManager apiManager;

    private final OkHttpClient httpClient = new OkHttpClient.Builder()
            .cookieJar(new CustomCookieJar())
            .build();

    private final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private CSVWriter writer;


    public void start() {
        this.config = loadConfig();

        if(config == null) {
            System.out.println("Please edit config");
            return;
        }
        initCSV();
        this.writer.writeNext(new String[]{"Name", "Number", "Exp Date", "CVV"});

        this.apiManager = new APIManager();
        apiManager.login();

    }

    public CSVWriter getWriter() {
        return writer;
    }

    private void initCSV() {
        try {
            this.writer = new CSVWriter(new FileWriter(config.getCardSettings().getName() + "-cards.csv"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void cookiesFix() {
        if(((CustomCookieJar) httpClient.cookieJar()).getCookies().size() > 30)
            ((CustomCookieJar) httpClient.cookieJar()).clearCookies();
    }

    public OkHttpClient getHttpClient() {
        return httpClient;
    }

    public Config getConfig() {
        return config;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public APIManager getApiManager() {
        return apiManager;
    }

    public void setCurrentUser(User currentUser) {
        this.currentUser = currentUser;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getAccessToken() {
        return accessToken;
    }

    private Config loadConfig() {
        try {
            final File configFile = new File("config.json");

            if(configFile.exists()) {
                return GSON.fromJson(new FileReader(configFile), Config.class);
            } else {
                configFile.createNewFile();

                final FileWriter fileWriter = new FileWriter(configFile);
                fileWriter.write(GSON.toJson(Config.class));
                fileWriter.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}
