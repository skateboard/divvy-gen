package me.brennan.divvy.api.impl;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import me.brennan.divvy.DivvyGen;
import me.brennan.divvy.api.API;
import me.brennan.divvy.http.JsonBody;
import me.brennan.divvy.utils.Logger;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import java.util.Scanner;

/**
 * @author Brennan
 * @since 6/28/21
 **/
public class AuthAPI extends API {

    public AuthAPI() {
        super("https://id.divvy.co/api/v1/login");
    }

    @Override
    public void run() {
        try {
            Logger.info("Attempting to Login...");
            final JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("email", DivvyGen.INSTANCE.getConfig().getAccount().getEmail());
            jsonObject.addProperty("password", DivvyGen.INSTANCE.getConfig().getAccount().getPassword());

            final Request request = new Request.Builder()
                    .url(getApiLink())
                    .post(new JsonBody(jsonObject))
                    .header("x-client-type", "WEB")
                    .build();

            try(Response response = DivvyGen.INSTANCE.getHttpClient().newCall(request).execute()) {
                if (response.code() == 401) {
                    Logger.warning("Verification needed!");

                    final Scanner scanner = new Scanner(System.in);
                    verify(scanner.nextLine());
                } else if(response.code() == 200) {
                    Logger.success("Successfully Verified");
                    final JsonObject bodyObject = JsonParser.parseString(response.body().string()).getAsJsonObject();
                    Logger.success("Session Expires at " + bodyObject.get("expires_at").getAsString());

                    DivvyGen.INSTANCE.setAccessToken(bodyObject.get("access_token").getAsString());
                } else {
                    Logger.error("Unsupported error type: " + response.code());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void verify(String code) {
        try {
            final JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("code", code);
            jsonObject.addProperty("remember_device", false);

            final Request request = new Request.Builder()
                    .url("https://id.divvy.co/api/v1/challenge/verify")
                    .post(new JsonBody(jsonObject))
                    .header("x-client-type", "WEB")
                    .header("referrer", "https://app.divvy.co/")
                    .header("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.106 Safari/537.36")
                    .build();
            try(Response response = DivvyGen.INSTANCE.getHttpClient().newCall(request).execute()) {
                if(response.code() != 200) {
                    Logger.error("Failed to verify! Try again");
                    return;
                }
                Logger.success("Successfully Verified");
                final JsonObject bodyObject = JsonParser.parseString(response.body().string()).getAsJsonObject();
                Logger.success("Session Expires at " + bodyObject.get("expires_at").getAsString());

                DivvyGen.INSTANCE.setAccessToken(bodyObject.get("access_token").getAsString());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String refreshToken() {
        try {
            Logger.success("Refreshing Access token...");
            final Request request = new Request.Builder()
                    .url("https://id.divvy.co/api/v1/refresh")
                    .post(new JsonBody(new JsonObject()))
                    .header("x-client-type", "WEB")
                    .build();

            try(Response response = DivvyGen.INSTANCE.getHttpClient().newCall(request).execute()) {
                if(response.code() == 200) {
                    Logger.success("Successfully refreshed token!");
                    final JsonObject jsonObject = JsonParser.parseString(response.body().string()).getAsJsonObject();
                    Logger.success("Session Expires at " + jsonObject.get("expires_at").getAsString());

                    return jsonObject.get("access_token").getAsString();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}
