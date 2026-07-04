package com.example.doctorapp.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Wraps SharedPreferences to persist the logged-in user's session
 * (Supabase access token + basic profile fields) across app restarts.
 */
public class SessionManager {

    private static final String PREF_NAME = "doctor_app_session";
    private static SharedPreferences prefs;

    public static void init(Context context) {
        prefs = context.getApplicationContext()
                .getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public static void saveSession(String accessToken, String refreshToken, String userId, String email, String fullName) {
        prefs.edit()
                .putString("access_token", accessToken)
                .putString("refresh_token", refreshToken)
                .putString("user_id", userId)
                .putString("email", email)
                .putString("full_name", fullName)
                .putBoolean("is_logged_in", true)
                .apply();
    }

    public static boolean isLoggedIn() {
        return prefs.getBoolean("is_logged_in", false);
    }

    public static String getAccessToken() {
        return prefs.getString("access_token", null);
    }

    public static String getUserId() {
        return prefs.getString("user_id", null);
    }

    public static String getEmail() {
        return prefs.getString("email", null);
    }

    public static String getFullName() {
        return prefs.getString("full_name", null);
    }

    public static void updateFullName(String fullName) {
        prefs.edit().putString("full_name", fullName).apply();
    }

    public static void clear() {
        prefs.edit().clear().apply();
    }
}
