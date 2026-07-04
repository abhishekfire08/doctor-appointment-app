package com.example.doctorapp.network;

import com.example.doctorapp.config.Config;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * Handles Supabase Auth sign-up / sign-in / password-recovery calls and
 * mirrors the resulting profile fields into the "users" table via PostgREST,
 * since Supabase Auth itself only stores id/email/password.
 */
public class SupabaseAuthManager {

    public interface AuthCallback {
        void onSuccess(String userId, String accessToken, String refreshToken);
        void onError(String message);
    }

    /** Step 1 of registration: create the Auth user (email + password). */
    public static void signUp(String email, String password, ApiCallback rawCallback) {
        signUp(email, password, null, rawCallback);
    }

    /**
     * Creates the Auth user, passing extra profile fields as user_metadata so a
     * "handle_new_user" DB trigger can populate public.users automatically
     * (see README section 6) — this avoids fighting RLS on a client-side insert.
     */
    public static void signUp(String email, String password, JsonObject metadata, ApiCallback rawCallback) {
        JsonObject body = new JsonObject();
        body.addProperty("email", email);
        body.addProperty("password", password);
        if (metadata != null) {
            body.add("data", metadata);
        }
        ApiClient.post(Config.AUTH_SIGNUP_ENDPOINT, body.toString(), false, rawCallback);
    }

    public static void login(String email, String password, AuthCallback callback) {
        JsonObject body = new JsonObject();
        body.addProperty("email", email);
        body.addProperty("password", password);

        ApiClient.post(Config.AUTH_LOGIN_ENDPOINT, body.toString(), false, new ApiCallback() {
            @Override
            public void onSuccess(String responseBody) {
                try {
                    JsonObject json = JsonParser.parseString(responseBody).getAsJsonObject();
                    String accessToken = json.get("access_token").getAsString();
                    String refreshToken = json.has("refresh_token") ? json.get("refresh_token").getAsString() : "";
                    String userId = json.getAsJsonObject("user").get("id").getAsString();
                    callback.onSuccess(userId, accessToken, refreshToken);
                } catch (Exception e) {
                    callback.onError("Could not parse login response: " + e.getMessage());
                }
            }

            @Override
            public void onError(String message) {
                callback.onError(message);
            }
        });
    }

    /** Sends a password-reset email via Supabase Auth. */
    public static void forgotPassword(String email, ApiCallback callback) {
        JsonObject body = new JsonObject();
        body.addProperty("email", email);
        ApiClient.post(Config.AUTH_RECOVER_ENDPOINT, body.toString(), false, callback);
    }
}
