package me.brennan.divvy.api.impl;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import me.brennan.divvy.DivvyGen;
import me.brennan.divvy.api.API;
import me.brennan.divvy.http.JsonBody;
import me.brennan.divvy.models.Budget;
import me.brennan.divvy.utils.Logger;
import okhttp3.Request;
import okhttp3.Response;

/**
 * @author Brennan
 * @since 6/29/21
 **/
public class BudgetAPI extends API {

    public BudgetAPI() {
        super("https://app.divvy.co/je/graphql");
    }

    @Override
    public void run() {
        try {
            Logger.info("Attempting to gather budgets");
            final JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("operationName", "GetBudgetsSecondaryNavBudgetList");

            final JsonObject variablesObject = new JsonObject();
            variablesObject.addProperty("companyId", DivvyGen.INSTANCE.getConfig().getIdentifications().getCompanyID());
            variablesObject.addProperty("first", 9999999);

            jsonObject.add("variables", variablesObject);
            jsonObject.addProperty("query", "query GetBudgetsSecondaryNavBudgetList($companyId: ID!, $first: Int) {\n  node(id: $companyId) {\n    ... on Company {\n      id\n      activeBudgets: budgets(retired: false, first: $first) {\n        edges {\n          node {\n            id\n            name\n            __typename\n          }\n          __typename\n        }\n        __typename\n      }\n      retiredBudgets: budgets(retired: true, first: $first) {\n        edges {\n          node {\n            id\n            name\n            __typename\n          }\n          __typename\n        }\n        __typename\n      }\n      __typename\n    }\n    __typename\n  }\n}\n");

            final Request request = new Request.Builder()
                    .url(getApiLink())
                    .post(new JsonBody(jsonObject))
                    .header("Authorization", "Bearer " + DivvyGen.INSTANCE.getAccessToken())
                    .header("x-acting-on-company", DivvyGen.INSTANCE.getConfig().getIdentifications().getCompanyID())
                    .header("x-api-version", "2")
                    .header("x-client-version", "28.16.0")
                    .build();

            try(Response response = DivvyGen.INSTANCE.getHttpClient().newCall(request).execute()) {
                if(response.code() != 200) {
                    Logger.error("Failed to grab budgets!");
                    return;
                }
                final JsonObject bodyObject = JsonParser.parseString(response.body().string()).getAsJsonObject();

                final JsonArray budgets = bodyObject.getAsJsonObject("data").getAsJsonObject("node")
                        .getAsJsonObject("activeBudgets").getAsJsonArray("edges");

                for(JsonElement element : budgets) {
                    if(element instanceof JsonObject) {
                        final JsonObject budgetObject = element.getAsJsonObject().getAsJsonObject("node");

                        DivvyGen.INSTANCE.getCurrentUser()
                                .getCurrentBudgets().add(new Budget(budgetObject.get("name").getAsString(),
                                                                budgetObject.get("id").getAsString()));
                    }
                }

                Logger.success("Successfully gathered " + DivvyGen.INSTANCE.getCurrentUser().getCurrentBudgets().size() + " budgets.");

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
