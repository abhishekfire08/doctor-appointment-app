package com.example.doctorapp.network;

/**
 * Generic callback for API calls. onSuccess/onError always fire on the main thread.
 */
public interface ApiCallback {
    void onSuccess(String responseBody);
    void onError(String message);
}
