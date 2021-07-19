package me.brennan.divvy.api.impl;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import me.brennan.divvy.DivvyGen;
import me.brennan.divvy.api.API;
import me.brennan.divvy.http.JsonBody;
import me.brennan.divvy.models.User;
import me.brennan.divvy.utils.Logger;
import okhttp3.Request;
import okhttp3.Response;

/**
 * @author Brennan
 * @since 6/28/21
 **/
public class UserAPI extends API {

    public UserAPI() {
        super("https://app.divvy.co/je/graphql");
    }

    @Override
    public void run() {
        try {
            Logger.info("Attempting to gather User information");
            final JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("operationName", "CurrentUser");

            final JsonObject variablesObject = new JsonObject();
            variablesObject.addProperty("companyId", DivvyGen.INSTANCE.getConfig().getIdentifications().getCompanyID());

            jsonObject.add("variables", variablesObject);
            jsonObject.addProperty("query", "query CurrentUser($companyId: ID) {\n  currentUser(actingOnCompany: $companyId) {\n    id\n    intercomUserHash(platform: WEB)\n    displayName\n    hasDateOfBirth\n    __typename\n  }\n}\n");

            System.out.println(jsonObject);

            final Request request = new Request.Builder()
                    .url(getApiLink())
                    .post(new JsonBody(jsonObject))
                    .header("Authorization", "Bearer " + DivvyGen.INSTANCE.getAccessToken())
                    .header("x-acting-on-company", DivvyGen.INSTANCE.getConfig().getIdentifications().getCompanyID())
                    .header("x-api-version", "2")
                    .header("x-client-version", "28.16.0")
                    .build();

            User user = new User();

            try(Response response = DivvyGen.INSTANCE.getHttpClient().newCall(request).execute()) {
                if(response.code() != 200) {
                    Logger.error("Failed to gather user information! " + response.code());
                    return;
                }
                Logger.success("Gathered User information");
                final JsonObject bodyObject = JsonParser.parseString(response.body().string()).getAsJsonObject();

                final JsonObject currentUser = bodyObject.getAsJsonObject("data").getAsJsonObject("currentUser");

                user.setId(currentUser.get("id").getAsString());
                user.setName(currentUser.get("displayName").getAsString());
                user.setLoadedCardsNumber(loadedCards(user.getId()));
                DivvyGen.INSTANCE.setCurrentUser(user);

                Logger.success("Welcome, " + user.getName());
                Logger.success("Total Cards: " + user.getLoadedCardsNumber());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private int loadedCards(String id) {
        try {
            final JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("operationName", "GetUserCards");

            final JsonObject variablesObject = new JsonObject();
            variablesObject.addProperty("currentUserId", id);
            variablesObject.addProperty("first", 200);

            final JsonArray typeArray = new JsonArray();
            typeArray.add("BURNER");

            variablesObject.add("types", typeArray);

            variablesObject.addProperty("sortColumn", "name");
            variablesObject.addProperty("sortDirection", "asc");

            jsonObject.add("variables", variablesObject);
            jsonObject.addProperty("query", "query GetUserCards($currentUserId: ID!, $after: String, $budgets: [ID], $first: Int, $search: String, $sortColumn: String, $sortDirection: String, $types: [UserCardType]) {\n  node(id: $currentUserId) {\n    ... on User {\n      id\n      allCards(first: $first, after: $after, types: $types, budgets: $budgets, search: $search, sortColumn: $sortColumn, sortDirection: $sortDirection) {\n        totalCount\n        pageInfo {\n          hasNextPage\n          endCursor\n          __typename\n        }\n        edges {\n          node {\n            id\n            ...CardsListVirtualCard\n            __typename\n          }\n          __typename\n        }\n        __typename\n      }\n      __typename\n    }\n    __typename\n  }\n}\n\nfragment CardsListVirtualCard on Card {\n  ...CardsListGenericCardFields\n  userAllocation {\n    ...CardsListUserAllocationFields\n    __typename\n  }\n  __typename\n}\n\nfragment CardsListGenericCardFields on Card {\n  id\n  frozen\n  lastFour\n  lastUsed\n  name\n  canToggleFrozen\n  cardType\n  activationStatus\n  blocked\n  token\n  expirationDate\n  brand\n  latestTransaction {\n    id\n    merchantName\n    cleanedMerchantName\n    merchantLogoUrl\n    clearedAt\n    occurredAt\n    __typename\n  }\n  user {\n    id\n    displayName\n    firstName\n    lastName\n    avatarUrl\n    avatarFallback\n    __typename\n  }\n  __typename\n}\n\nfragment CardsListUserAllocationFields on UserAllocation {\n  id\n  expiresAt\n  type\n  recurringAmount\n  availableFunds\n  limit\n  budget {\n    id\n    name\n    balance\n    retired\n    __typename\n  }\n  __typename\n}");

            final Request request = new Request.Builder()
                    .url(getApiLink())
                    .post(new JsonBody(jsonObject))
                    .header("Authorization", "Bearer " + DivvyGen.INSTANCE.getAccessToken())
                    .header("x-acting-on-company", DivvyGen.INSTANCE.getConfig().getIdentifications()
                            .getCompanyID())
                    .header("x-api-version", "2")
                    .header("x-client-version", "28.16.0")
                    .build();

            try(Response response = DivvyGen.INSTANCE.getHttpClient().newCall(request).execute()) {
                if(response.code() != 200) {
                    Logger.error("Failed to grab loaded cards! " + response.code());
                    return 0;
                }
                final JsonObject bodyObject = JsonParser.parseString(response.body().string()).getAsJsonObject();

                return bodyObject.getAsJsonObject("data")
                        .getAsJsonObject("node")
                        .getAsJsonObject("allCards")
                        .get("totalCount").getAsInt();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return 0;
    }
}
