package me.brennan.divvy.api;

import me.brennan.divvy.DivvyGen;
import me.brennan.divvy.api.impl.*;
import me.brennan.divvy.models.Budget;
import me.brennan.divvy.utils.Logger;

import java.util.Arrays;
import java.util.Scanner;

/**
 * @author Brennan
 * @since 6/29/21
 **/
public class APIManager {
    private final AuthAPI authAPI;
    private final UserAPI userAPI;
    private final BudgetAPI budgetAPI;
    private final CardAPI cardAPI;
    private final DeleteAPI deleteAPI;

    public APIManager() {
        this.authAPI = new AuthAPI();
        this.userAPI = new UserAPI();
        this.budgetAPI = new BudgetAPI();
        this.cardAPI = new CardAPI();
        this.deleteAPI = new DeleteAPI();
    }

    public String refreshToken() {
        return this.authAPI.refreshToken();
    }

    public void login() {
        this.authAPI.run();
        this.userAPI.run();

        Logger.info("What action do you want todo? (create, delete)");
        final Scanner actionScanner = new Scanner(System.in);
        if(actionScanner.nextLine().equalsIgnoreCase("delete")) {
            this.deleteAPI.run();
            return;
        }
        this.budgetAPI.run();

        Logger.info("Pleas select a budget! " + Arrays.toString(DivvyGen.INSTANCE.getCurrentUser()
                .getBudgetNames().toArray(new String[DivvyGen.INSTANCE.getCurrentUser().getCurrentBudgets().size()])));
        Scanner scanner = new Scanner(System.in);
        final Budget budget = DivvyGen.INSTANCE.getCurrentUser().getBudget(scanner.nextLine());

        if(budget != null) {
           Logger.success("Set budget to " + budget.getName());
           DivvyGen.INSTANCE.getCurrentUser().setCurrentBudget(budget);

           this.cardAPI.run();
        } else {
            Logger.error("Failed to find budget!");
        }
    }
}
