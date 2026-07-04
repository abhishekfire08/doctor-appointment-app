package com.example.doctorapp.network;

import android.os.Handler;
import android.os.Looper;

import com.example.doctorapp.config.Config;
import com.example.doctorapp.utils.SessionManager;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Thin wrapper around OkHttp for talking to Supabase's:
 *  - Auth REST endpoints (/auth/v1/...)
 *  - PostgREST table endpoints (/rest/v1/{table})
 *
 * All calls are async; results are delivered on the main thread via ApiCallback.
 */
public class ApiClient {

    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    private static final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .build();

    private static final Handler mainHandler = new Handler(Looper.getMainLooper());

    private static Request.Builder baseRequest(String url, boolean withAuth) {
        Request.Builder builder = new Request.Builder()
                .url(url)
                .addHeader("apikey", Config.SUPABASE_ANON_KEY)
                .addHeader("Content-Type", "application/json");

        String token = withAuth ? SessionManager.getAccessToken() : null;
        builder.addHeader("Authorization", "Bearer " + (token != null ? token : Config.SUPABASE_ANON_KEY));
        return builder;
    }

    /** GET against /rest/v1/{table}?... or any full URL. Requires the user's auth token. */
    public static void get(String url, ApiCallback callback) {
        Request request = baseRequest(url, true).get().build();
        enqueue(request, callback);
    }

    /** POST a JSON body. Adds Prefer: return=representation so Supabase returns the inserted row(s). */
    public static void post(String url, String jsonBody, boolean withAuth, ApiCallback callback) {
        RequestBody body = RequestBody.create(jsonBody, JSON);
        Request request = baseRequest(url, withAuth)
                .addHeader("Prefer", "return=representation")
                .post(body)
                .build();
        enqueue(request, callback);
    }

    /** PATCH (partial update) a JSON body against a filtered URL, e.g. .../appointments?id=eq.5 */
    public static void patch(String url, String jsonBody, ApiCallback callback) {
        RequestBody body = RequestBody.create(jsonBody, JSON);
        Request request = baseRequest(url, true)
                .addHeader("Prefer", "return=representation")
                .patch(body)
                .build();
        enqueue(request, callback);
    }

    /** PUT a JSON body. Supabase's Auth API (e.g. updating the logged-in user) expects PUT, not PATCH. */
    public static void put(String url, String jsonBody, ApiCallback callback) {
        RequestBody body = RequestBody.create(jsonBody, JSON);
        Request request = baseRequest(url, true).put(body).build();
        enqueue(request, callback);
    }

    /** DELETE against a filtered URL. */
    public static void delete(String url, ApiCallback callback) {
        Request request = baseRequest(url, true).delete().build();
        enqueue(request, callback);
    }

    private static void enqueue(Request request, ApiCallback callback) {
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                postError(callback, "Network error: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String bodyString = response.body() != null ? response.body().string() : "";
                if (response.isSuccessful()) {
                    postSuccess(callback, bodyString);
                } else {
                    postError(callback, "Request failed (" + response.code() + "): " + bodyString);
                }
                response.close();
            }
        });
    }

    private static void postSuccess(ApiCallback callback, String body) {
        mainHandler.post(() -> callback.onSuccess(body));
    }

    private static void postError(ApiCallback callback, String message) {
        mainHandler.post(() -> callback.onError(message));
    }
}
