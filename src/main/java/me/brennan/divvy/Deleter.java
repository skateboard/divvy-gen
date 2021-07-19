package me.brennan.divvy;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import me.brennan.divvy.utils.CustomCookieJar;
import okhttp3.*;

import java.util.LinkedList;
import java.util.List;

/**
 * @author Brennan
 * @since 6/26/21
 **/
public class Deleter {
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    public static void deleteCard(OkHttpClient httpClient, String accessToken, String company, String id) {
        try {
            final JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("operationName", "DeleteVirtualCard");

            final JsonObject variablesObject = new JsonObject();

            final JsonObject inputObject = new JsonObject();
            inputObject.addProperty("cardId", id);
            inputObject.addProperty("clientMutationId", "0");

            variablesObject.add("input", inputObject);

            jsonObject.add("variables", variablesObject);
            jsonObject.addProperty("query", "mutation DeleteVirtualCard($input: DeleteCardInput!) {\n  deleteCard(input: $input) {\n    deletedCardId\n    budget {\n      id\n      ...BudgetSpend\n      __typename\n    }\n    __typename\n  }\n}\n\nfragment BudgetSpend on Budget {\n  availableAmountForBudgetPeriod\n  availableAmountForGoalForBudgetPeriod\n  bufferForBudgetPeriod\n  softGoal\n  totalAvailableToSpend: allocationsSummedBalance\n  totalSpentForBudgetPeriod\n  __typename\n}");

            if(((CustomCookieJar) httpClient.cookieJar()).getCookies().size() > 30) {
                ((CustomCookieJar) httpClient.cookieJar()).clearCookies();
            }
            final Request request = new Request.Builder()
                    .url("https://app.divvy.co/je/graphql")
                    .post(RequestBody.create(JSON, jsonObject.toString()))
                    .header("Authorization", "Bearer " + accessToken)
                    .header("x-acting-on-company", company)
                    .header("x-api-version", "2")
//                    .header("x-client-type", "WEB")
                    .header("x-client-version", "28.16.0")
                    .build();

            try(Response response = httpClient.newCall(request).execute()) {
                if(response.code() != 200) {
                    System.out.println(response.body().string());
                    System.out.println("Failed to delete " + id + " with code " + response.code());
                    return;
                }

                System.out.println("Deleted " + id);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static List<String> getCards(OkHttpClient httpClient, String userID, String accessToken, String company) {
        final List<String> ids = new LinkedList<>();
        try {
            final JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("operationName", "GetUserCards");

            final JsonObject variablesObject = new JsonObject();
            variablesObject.addProperty("currentUserId", userID);
            variablesObject.addProperty("first", 200);

            final JsonArray typeArray = new JsonArray();
            typeArray.add("BURNER");

            variablesObject.add("types", typeArray);

            variablesObject.addProperty("sortColumn", "name");
            variablesObject.addProperty("sortDirection", "asc");

            jsonObject.add("variables", variablesObject);
            jsonObject.addProperty("query", "query GetUserCards($currentUserId: ID!, $after: String, $budgets: [ID], $first: Int, $search: String, $sortColumn: String, $sortDirection: String, $types: [UserCardType]) {\n  node(id: $currentUserId) {\n    ... on User {\n      id\n      allCards(first: $first, after: $after, types: $types, budgets: $budgets, search: $search, sortColumn: $sortColumn, sortDirection: $sortDirection) {\n        totalCount\n        pageInfo {\n          hasNextPage\n          endCursor\n          __typename\n        }\n        edges {\n          node {\n            id\n            ...CardsListVirtualCard\n            __typename\n          }\n          __typename\n        }\n        __typename\n      }\n      __typename\n    }\n    __typename\n  }\n}\n\nfragment CardsListVirtualCard on Card {\n  ...CardsListGenericCardFields\n  userAllocation {\n    ...CardsListUserAllocationFields\n    __typename\n  }\n  __typename\n}\n\nfragment CardsListGenericCardFields on Card {\n  id\n  frozen\n  lastFour\n  lastUsed\n  name\n  canToggleFrozen\n  cardType\n  activationStatus\n  blocked\n  token\n  expirationDate\n  brand\n  latestTransaction {\n    id\n    merchantName\n    cleanedMerchantName\n    merchantLogoUrl\n    clearedAt\n    occurredAt\n    __typename\n  }\n  user {\n    id\n    displayName\n    firstName\n    lastName\n    avatarUrl\n    avatarFallback\n    __typename\n  }\n  __typename\n}\n\nfragment CardsListUserAllocationFields on UserAllocation {\n  id\n  expiresAt\n  type\n  recurringAmount\n  availableFunds\n  limit\n  budget {\n    id\n    name\n    balance\n    retired\n    __typename\n  }\n  __typename\n}");

            final Request request = new Request.Builder()
                    .url("https://app.divvy.co/je/graphql")
                    .post(RequestBody.create(JSON, jsonObject.toString()))
                    .header("Authorization", "Bearer " + accessToken)
                    .header("x-acting-on-company", company)
                    .header("x-api-version", "2")
//                    .header("x-client-type", "WEB")
                    .header("x-client-version", "28.16.0")
                    .build();

            try(Response response = httpClient.newCall(request).execute()) {
                if(response.code() != 200) {
                    System.out.println("Failed to grab cards! " + response.code());
                    System.out.println(response.body().string());
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
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return ids;
    }

}
