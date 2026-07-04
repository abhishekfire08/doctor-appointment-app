package com.example.doctorapp.ui.splash;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

import com.example.doctorapp.R;
import com.example.doctorapp.ui.auth.LoginActivity;
import com.example.doctorapp.ui.main.MainActivity;
import com.example.doctorapp.utils.SessionManager;

public class SplashActivity extends AppCompatActivity {

    private static final long SPLASH_DELAY_MS = 1200;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            Intent intent = SessionManager.isLoggedIn()
                    ? new Intent(SplashActivity.this, MainActivity.class)
                    : new Intent(SplashActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        }, SPLASH_DELAY_MS);
    }
}
