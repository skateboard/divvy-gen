package me.brennan.divvy.api.impl;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import me.brennan.divvy.DivvyGen;
import me.brennan.divvy.api.API;
import me.brennan.divvy.http.JsonBody;
import me.brennan.divvy.utils.Logger;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import java.util.LinkedList;
import java.util.List;

/**
 * @author Brennan
 * @since 7/10/21
 **/
public class DeleteAPI extends API {

    public DeleteAPI() {
        super("https://app.divvy.co/je/graphql");
    }

    @Override
    public void run() {
        try {
            final List<String> cards = getCards();

            if(!cards.isEmpty()) {
                cards.forEach(card -> {
                    try {
                        this.deleteCard(card);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void deleteCard(String id) throws Exception {
        final JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("operationName", "DeleteVirtualCard");

        final JsonObject variablesObject = new JsonObject();

        final JsonObject inputObject = new JsonObject();
        inputObject.addProperty("cardId", id);
        inputObject.addProperty("clientMutationId", "0");

        variablesObject.add("input", inputObject);

        jsonObject.add("variables", variablesObject);
        jsonObject.addProperty("query", "mutation DeleteVirtualCard($input: DeleteCardInput!) {\n  deleteCard(input: $input) {\n    deletedCardId\n    budget {\n      id\n      ...BudgetSpend\n      __typename\n    }\n    __typename\n  }\n}\n\nfragment BudgetSpend on Budget {\n  availableAmountForBudgetPeriod\n  availableAmountForGoalForBudgetPeriod\n  bufferForBudgetPeriod\n  softGoal\n  totalAvailableToSpend: allocationsSummedBalance\n  totalSpentForBudgetPeriod\n  __typename\n}");

        DivvyGen.INSTANCE.cookiesFix();
        final Request request = new Request.Builder()
                .url("https://app.divvy.co/je/graphql")
                .post(new JsonBody(jsonObject))
                .header("Authorization", "Bearer " + DivvyGen.INSTANCE.getAccessToken())
                .header("x-acting-on-company", DivvyGen.INSTANCE.getConfig().getIdentifications().getCompanyID())
                .header("x-api-version", "2")
                .header("x-client-version", "28.16.0")
                .build();

        try(Response response = DivvyGen.INSTANCE.getHttpClient().newCall(request).execute()) {
            if(response.code() != 200) {
                Logger.error("Failed to delete " + id + "! " + response.code());
                return;
            }
            Logger.success("Deleted " + id);
        }
    }

    private List<String> getCards() throws Exception {
        Logger.info("Trying to grab cards...");
        final List<String> ids = new LinkedList<>();

        final JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("operationName", "GetUserCards");

        final JsonObject variablesObject = new JsonObject();
        variablesObject.addProperty("currentUserId", DivvyGen.INSTANCE.getCurrentUser().getId());
        variablesObject.addProperty("first", 200);

        final JsonArray typeArray = new JsonArray();
        typeArray.add("BURNER");
        typeArray.add("SUBSCRIPTION");

        variablesObject.add("types", typeArray);

        variablesObject.addProperty("sortColumn", "name");
        variablesObject.addProperty("sortDirection", "asc");

        jsonObject.add("variables", variablesObject);
        jsonObject.addProperty("query", "query GetUserCards($currentUserId: ID!, $after: String, $budgets: [ID], $first: Int, $search: String, $sortColumn: String, $sortDirection: String, $types: [UserCardType]) {\n  node(id: $currentUserId) {\n    ... on User {\n      id\n      allCards(first: $first, after: $after, types: $types, budgets: $budgets, search: $search, sortColumn: $sortColumn, sortDirection: $sortDirection) {\n        totalCount\n        pageInfo {\n          hasNextPage\n          endCursor\n          __typename\n        }\n        edges {\n          node {\n            id\n            ...CardsListVirtualCard\n            __typename\n          }\n          __typename\n        }\n        __typename\n      }\n      __typename\n    }\n    __typename\n  }\n}\n\nfragment CardsListVirtualCard on Card {\n  ...CardsListGenericCardFields\n  userAllocation {\n    ...CardsListUserAllocationFields\n    __typename\n  }\n  __typename\n}\n\nfragment CardsListGenericCardFields on Card {\n  id\n  frozen\n  lastFour\n  lastUsed\n  name\n  canToggleFrozen\n  cardType\n  activationStatus\n  blocked\n  token\n  expirationDate\n  brand\n  latestTransaction {\n    id\n    merchantName\n    cleanedMerchantName\n    merchantLogoUrl\n    clearedAt\n    occurredAt\n    __typename\n  }\n  user {\n    id\n    displayName\n    firstName\n    lastName\n    avatarUrl\n    avatarFallback\n    __typename\n  }\n  __typename\n}\n\nfragment CardsListUserAllocationFields on UserAllocation {\n  id\n  expiresAt\n  type\n  recurringAmount\n  availableFunds\n  limit\n  budget {\n    id\n    name\n    balance\n    retired\n    __typename\n  }\n  __typename\n}");

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
                Logger.error("Failed to grab cards! " + response.code());
                return ids;
            }
            final JsonObject bodyObject = JsonParser.parseString(response.body().string()).getAsJsonObject();

            final JsonArray cardsArray = bodyObject.getAsJsonObject("data")
                    .getAsJsonObject("node")
                    .getAsJsonObject("allCards")
                    .getAsJsonArray("edges");

            for (JsonElement jsonElement : cardsArray) {
                if(jsonElement instanceof JsonObject) {
                    final JsonObject cardObject = jsonElement.getAsJsonObject();

                    ids.add(cardObject.getAsJsonObject("node").get("id").getAsString());
                }
            }

            Logger.success("Successfully grabbed " + ids.size() + " cards!");
        }

        return ids;
    }
}
