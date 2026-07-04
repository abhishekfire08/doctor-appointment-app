package com.example.doctorapp.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.doctorapp.R;
import com.example.doctorapp.network.SupabaseAuthManager;
import com.example.doctorapp.ui.main.MainActivity;
import com.example.doctorapp.utils.SessionManager;

public class LoginActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private ProgressBar progressLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        progressLogin = findViewById(R.id.progressLogin);

        TextView btnLogin = findViewById(R.id.btnLogin);
        TextView tvGoToRegister = findViewById(R.id.tvGoToRegister);
        TextView tvForgotPassword = findViewById(R.id.tvForgotPassword);

        btnLogin.setOnClickListener(v -> attemptLogin());
        tvGoToRegister.setOnClickListener(v ->
                startActivity(new Intent(this, RegisterActivity.class)));
        tvForgotPassword.setOnClickListener(v ->
                startActivity(new Intent(this, ForgotPasswordActivity.class)));
    }

    private void attemptLogin() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show();
            return;
        }

        setLoading(true);
        SupabaseAuthManager.login(email, password, new SupabaseAuthManager.AuthCallback() {
            @Override
            public void onSuccess(String userId, String accessToken, String refreshToken) {
                setLoading(false);
                SessionManager.saveSession(accessToken, refreshToken, userId, email, email);
                startActivity(new Intent(LoginActivity.this, MainActivity.class));
                finish();
            }

            @Override
            public void onError(String message) {
                setLoading(false);
                Toast.makeText(LoginActivity.this, message, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void setLoading(boolean loading) {
        progressLogin.setVisibility(loading ? View.VISIBLE : View.GONE);
    }
}
