package com.example.doctorapp;

import android.app.Application;
import com.example.doctorapp.utils.SessionManager;

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        SessionManager.init(this);
    }
}
