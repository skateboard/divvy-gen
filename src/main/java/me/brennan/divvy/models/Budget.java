package me.brennan.divvy.models;

/**
 * @author Brennan
 * @since 6/28/21
 **/
public class Budget {

    private String name, id;

    public Budget(String name, String id) {
        this.name = name;
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public String getId() {
        return id;
    }
}
