package com.example.doctorapp.ui.auth;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.doctorapp.R;
import com.example.doctorapp.network.ApiCallback;
import com.example.doctorapp.network.SupabaseAuthManager;

public class ForgotPasswordActivity extends AppCompatActivity {

    private EditText etEmail;
    private ProgressBar progressReset;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        etEmail = findViewById(R.id.etEmail);
        progressReset = findViewById(R.id.progressReset);
        TextView btnSendReset = findViewById(R.id.btnSendReset);

        btnSendReset.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            if (TextUtils.isEmpty(email)) {
                Toast.makeText(this, "Enter your email", Toast.LENGTH_SHORT).show();
                return;
            }
            progressReset.setVisibility(View.VISIBLE);
            SupabaseAuthManager.forgotPassword(email, new ApiCallback() {
                @Override
                public void onSuccess(String responseBody) {
                    progressReset.setVisibility(View.GONE);
                    Toast.makeText(ForgotPasswordActivity.this,
                            "Reset link sent. Check your email.", Toast.LENGTH_LONG).show();
                    finish();
                }

                @Override
                public void onError(String message) {
                    progressReset.setVisibility(View.GONE);
                    Toast.makeText(ForgotPasswordActivity.this, message, Toast.LENGTH_LONG).show();
                }
            });
        });
    }
}
