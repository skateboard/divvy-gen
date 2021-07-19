package me.brennan.divvy.api;

/**
 * @author Brennan
 * @since 6/28/21
 **/
public abstract class API {
    private String apiLink;

    public API(String apiLink) {
        this.apiLink = apiLink;
    }

    public abstract void run();

    public String getApiLink() {
        return apiLink;
    }
}
