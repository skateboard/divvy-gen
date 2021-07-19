package me.brennan.divvy.models;

import java.util.LinkedList;
import java.util.List;

/**
 * @author Brennan
 * @since 6/28/21
 **/
public class User {

    private String id, name;
    private int loadedCardsNumber;

    private Budget currentBudget;
    private final List<Budget> currentBudgets = new LinkedList<>();

    public User() {
    }

    public List<String> getBudgetNames() {
        final List<String> budgets = new LinkedList<>();

        for(Budget budget : getCurrentBudgets()) {
            budgets.add(budget.getName());
        }

        return budgets;
    }

    public void setCurrentBudget(Budget currentBudget) {
        this.currentBudget = currentBudget;
    }

    public Budget getCurrentBudget() {
        return currentBudget;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Budget getBudget(String name) {
        for(Budget budget : getCurrentBudgets()) {
            if(budget.getName().equalsIgnoreCase(name))
                return budget;
        }

        return null;
    }

    public List<Budget> getCurrentBudgets() {
        return currentBudgets;
    }

    public int getLoadedCardsNumber() {
        return loadedCardsNumber;
    }

    public void setLoadedCardsNumber(int loadedCardsNumber) {
        this.loadedCardsNumber = loadedCardsNumber;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
