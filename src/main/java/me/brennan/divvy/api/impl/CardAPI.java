package me.brennan.divvy.api.impl;

import com.google.gson.JsonArray;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import me.brennan.divvy.DivvyGen;
import me.brennan.divvy.api.API;
import me.brennan.divvy.http.JsonBody;
import me.brennan.divvy.models.Card;
import me.brennan.divvy.utils.Logger;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.apache.commons.logging.Log;

import java.util.Scanner;
import java.util.concurrent.TimeUnit;

/**
 * @author Brennan
 * @since 6/29/21
 **/
public class CardAPI extends API {

    public CardAPI() {
        super("https://app.divvy.co/je/graphql");
    }

    @Override
    public void run() {
        try {
            final Scanner scanner = new Scanner(System.in);
            Logger.info("Do you want subscription or burner? (1, 2)");
            final int type = scanner.nextInt();

            Logger.info("How many cards do you want to generate?");
            final int amount = scanner.nextInt();

            int successfulCards = 0;
            for (int i = 0; i < amount; i++) {
                String cardToken = null;
                if(type == 1) {
                    cardToken = createSubscriptionCard(i);
                } else if(type == 2) {
                    cardToken = createBurnerCard(i);
                }

                if(cardToken != null) {
                    final String panUrl = getPanURL(i, cardToken);

                    if(panUrl != null) {
                        final Card card = getCard(i, panUrl);

                        if(card != null) {
                            DivvyGen.INSTANCE.getWriter().writeNext(new String[]{card.getCardName(),
                                    card.getCardNumber(),
                                    card.getExpData(),
                                    card.getCvv()});
                            successfulCards++;
                        }
                    }

                    try {
                        if(DivvyGen.INSTANCE.getConfig().getSettings().getTimeout() != 0)
                            TimeUnit.SECONDS.sleep(DivvyGen.INSTANCE.getConfig().getSettings().getTimeout());
                    } catch (Exception ignored) {}
                }
            }

            DivvyGen.INSTANCE.getWriter().close();
            Logger.success("Successfully Generated " + successfulCards + " cards!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Card getCard(int index, String pan) throws Exception {
        Logger.info("Grabbing card info for " + index);
        final Request request = new Request.Builder()
                .url(pan)
                .get()
                .build();

        try(Response response = DivvyGen.INSTANCE.getHttpClient().newCall(request).execute()) {
            if(response.code() != 200) {
                Logger.error("Failed to grab card information for " + index + "! " + response.code());
                return null;
            }

            final JsonObject jsonObject = JsonParser.parseString(response.body().string()).getAsJsonObject();

            Logger.success("Successfully grabbed card" + index + "! " + jsonObject.get("cardNumber").getAsString());
            return new Card(DivvyGen.INSTANCE.getConfig().getCardSettings().getName() + "-" + index,
                    jsonObject.get("cardNumber").getAsString(),
                    jsonObject.get("expirationDate").getAsString(), jsonObject.get("cvv").getAsString());
        }
    }

    private String getPanURL(int index, String cardToken) throws Exception {
        Logger.info("Grabbing pan url for " + index);
        final JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("operationName", "FetchPanUrl");

        final JsonObject variablesObject = new JsonObject();
        final JsonObject inputObject = new JsonObject();
        inputObject.addProperty("cardToken", cardToken);
        variablesObject.add("input", inputObject);

        jsonObject.add("variables", variablesObject);
        jsonObject.addProperty("query", "mutation FetchPanUrl($input: GetPanUrlInput!) {\n  getPanUrl(input: $input) {\n    url\n    __typename\n  }\n}");

        DivvyGen.INSTANCE.cookiesFix();

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
                Logger.error("Failed to grab pan url! " + response.code());
                return null;
            }
            final JsonObject bodyObject = JsonParser.parseString(response.body().string()).getAsJsonObject();

            return bodyObject.getAsJsonObject("data").getAsJsonObject("getPanUrl").get("url").getAsString();
        }
    }

    private String createSubscriptionCard(int index) throws Exception {
        final JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("operationName", "CreateBudgetCard");

        final JsonObject variablesObject = new JsonObject();
        final JsonObject inputObject = new JsonObject();
        inputObject.addProperty("budgetId", DivvyGen.INSTANCE.getCurrentUser().getCurrentBudget().getId());
        inputObject.addProperty("name", DivvyGen.INSTANCE.getConfig().getCardSettings().getName() + " - " +index);
        inputObject.addProperty("amount", 1000000);
        inputObject.addProperty("type", "RECURRING");
        inputObject.addProperty("ownerId", DivvyGen.INSTANCE.getConfig().getIdentifications().getOwnerID());
        inputObject.add("selectedTags", JsonNull.INSTANCE);
        variablesObject.add("input", inputObject);
        jsonObject.add("variables", variablesObject);
        jsonObject.addProperty("query", "mutation CreateBudgetCard($input: CreateVirtualCardForBudgetInput!) {\n  createVirtualCardForBudget(input: $input) {\n    budget {\n      id\n      ...BudgetSpend\n      __typename\n    }\n    newCardEdge {\n      node {\n        ...NewlyCreatedCardInfo\n        __typename\n      }\n      __typename\n    }\n    __typename\n  }\n}\n\nfragment BudgetSpend on Budget {\n  availableAmountForBudgetPeriod\n  availableAmountForGoalForBudgetPeriod\n  bufferForBudgetPeriod\n  softGoal\n  totalAvailableToSpend: allocationsSummedBalance\n  totalSpentForBudgetPeriod\n  __typename\n}\n\nfragment NewlyCreatedCardInfo on Card {\n  id\n  expirationDate\n  lastFour\n  name\n  token\n  user {\n    id\n    __typename\n  }\n  __typename\n}\n");

        DivvyGen.INSTANCE.cookiesFix();
        final Request request = new Request.Builder()
                .url("https://app.divvy.co/je/graphql")
                .post(new JsonBody(jsonObject))
                .header("Authorization", "Bearer " + DivvyGen.INSTANCE.getAccessToken())
                .header("x-acting-on-company", DivvyGen.INSTANCE.getConfig().getIdentifications().getCompanyID())
                .header("x-api-version", "2")
                .header("x-client-version", "28.30.2")
                .header("user-agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36")
                .build();


        try(Response response = DivvyGen.INSTANCE.getHttpClient().newCall(request).execute()) {
            if (response.code() != 200) {
                if(response.code() == 403) {
                    DivvyGen.INSTANCE.setAccessToken(DivvyGen.INSTANCE.getApiManager().refreshToken());

                    return createSubscriptionCard(index);
                }

                System.out.println("Response Code: " + response.code());
                System.out.println("Body: " +response.body().string());
                //Logger.error("Failed to generate card! " + response.code());
                return null;
            }
            final JsonObject bodyObject = JsonParser.parseString(response.body().string()).getAsJsonObject();

            if (bodyObject.has("errors")) {
                System.out.println(bodyObject);
//                Logger.error("Encountered an error creating cards!: " +
//                        bodyObject.getAsJsonArray("errors").get(0).getAsJsonObject().get("errorMessage").getAsString());
                return null;
            }

            return bodyObject
                    .getAsJsonObject("data").getAsJsonObject("createVirtualCardForBudget")
                    .getAsJsonObject("newCardEdge")
                    .getAsJsonObject("node")
                    .get("token").getAsString();
        }

    }

    private String createBurnerCard(int index) throws Exception {
        final JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("operationName", "CreateBudgetCard");

        final JsonObject variablesObject = new JsonObject();
        final JsonObject inputObject = new JsonObject();
        inputObject.addProperty("budgetId", DivvyGen.INSTANCE.getCurrentUser().getCurrentBudget().getId());
        inputObject.addProperty("name", DivvyGen.INSTANCE.getConfig().getCardSettings().getName() + " - " +index);
        inputObject.addProperty("amount", 1000000);
        inputObject.addProperty("expiresAt", System.currentTimeMillis() + 14 * 24 * 3600 * 1000);
        inputObject.addProperty("type", "ONE_TIME");
        inputObject.addProperty("ownerId", DivvyGen.INSTANCE.getConfig().getIdentifications().getOwnerID());
        inputObject.add("selectedTags", JsonNull.INSTANCE);

        variablesObject.add("input", inputObject);
        jsonObject.add("variables", variablesObject);
        jsonObject.addProperty("query", "mutation CreateBudgetCard($input: CreateVirtualCardForBudgetInput!) {\n  createVirtualCardForBudget(input: $input) {\n    budget {\n      id\n      ...BudgetSpend\n      __typename\n    }\n    newCardEdge {\n      node {\n        ...NewlyCreatedCardInfo\n        __typename\n      }\n      __typename\n    }\n    __typename\n  }\n}\n\nfragment BudgetSpend on Budget {\n  availableAmountForBudgetPeriod\n  availableAmountForGoalForBudgetPeriod\n  bufferForBudgetPeriod\n  softGoal\n  totalAvailableToSpend: allocationsSummedBalance\n  totalSpentForBudgetPeriod\n  __typename\n}\n\nfragment NewlyCreatedCardInfo on Card {\n  id\n  expirationDate\n  lastFour\n  name\n  token\n  user {\n    id\n    __typename\n  }\n  __typename\n}\n");

        DivvyGen.INSTANCE.cookiesFix();
        final Request request = new Request.Builder()
                .url("https://app.divvy.co/je/graphql")
                .post(new JsonBody(jsonObject))
                .header("Authorization", "Bearer " + DivvyGen.INSTANCE.getAccessToken())
                .header("x-acting-on-company", DivvyGen.INSTANCE.getConfig().getIdentifications().getCompanyID())
                .header("x-api-version", "2")
                .header("x-client-version", "28.30.2")
                .header("user-agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36")
                .build();


        try(Response response = DivvyGen.INSTANCE.getHttpClient().newCall(request).execute()) {
            if (response.code() != 200) {
                if(response.code() == 403) {
                    DivvyGen.INSTANCE.setAccessToken(DivvyGen.INSTANCE.getApiManager().refreshToken());

                    return createBurnerCard(index);
                }

                System.out.println("Response Code: " + response.code());
                System.out.println("Body: " +response.body().string());
                //Logger.error("Failed to generate card! " + response.code());
                return null;
            }
            final JsonObject bodyObject = JsonParser.parseString(response.body().string()).getAsJsonObject();

            if (bodyObject.has("errors")) {
                System.out.println(bodyObject);
//                Logger.error("Encountered an error creating cards!: " +
//                        bodyObject.getAsJsonArray("errors").get(0).getAsJsonObject().get("errorMessage").getAsString());
                return null;
            }

            return bodyObject
                    .getAsJsonObject("data").getAsJsonObject("createVirtualCardForBudget")
                    .getAsJsonObject("newCardEdge")
                    .getAsJsonObject("node")
                    .get("token").getAsString();
        }
    }
}
