package com.example.doctorapp.ui.auth;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.doctorapp.R;
import com.example.doctorapp.network.ApiCallback;
import com.example.doctorapp.network.SupabaseAuthManager;
import com.google.gson.JsonObject;

import java.util.Calendar;

public class RegisterActivity extends AppCompatActivity {

    private EditText etFullName, etEmail, etMobile, etPassword, etDob;
    private Spinner spinnerGender;
    private ProgressBar progressRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        etFullName = findViewById(R.id.etFullName);
        etEmail = findViewById(R.id.etEmail);
        etMobile = findViewById(R.id.etMobile);
        etPassword = findViewById(R.id.etPassword);
        etDob = findViewById(R.id.etDob);
        spinnerGender = findViewById(R.id.spinnerGender);
        progressRegister = findViewById(R.id.progressRegister);

        ArrayAdapter<String> genderAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item,
                new String[]{"Male", "Female", "Other"});
        spinnerGender.setAdapter(genderAdapter);

        etDob.setOnClickListener(v -> showDatePicker());

        TextView btnRegister = findViewById(R.id.btnRegister);
        TextView tvGoToLogin = findViewById(R.id.tvGoToLogin);

        btnRegister.setOnClickListener(v -> attemptRegister());
        tvGoToLogin.setOnClickListener(v -> finish());
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog dialog = new DatePickerDialog(this, (view, year, month, day) -> {
            String formatted = String.format("%02d-%02d-%04d", day, month + 1, year);
            etDob.setText(formatted);
        }, calendar.get(Calendar.YEAR) - 25, calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        dialog.show();
    }

    private void attemptRegister() {
        String fullName = etFullName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String mobile = etMobile.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String gender = spinnerGender.getSelectedItem().toString();
        String dob = etDob.getText().toString().trim();

        if (TextUtils.isEmpty(fullName) || TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        setLoading(true);
        JsonObject metadata = new JsonObject();
        metadata.addProperty("full_name", fullName);
        metadata.addProperty("mobile", mobile);
        metadata.addProperty("gender", gender);
        metadata.addProperty("dob", dob);

        SupabaseAuthManager.signUp(email, password, metadata, new ApiCallback() {
            @Override
            public void onSuccess(String responseBody) {
                // The "handle_new_user" DB trigger (see README section 6) creates the
                // public.users row automatically from this metadata — no client-side
                // insert needed, which avoids fighting Row Level Security at signup time.
                setLoading(false);
                Toast.makeText(RegisterActivity.this,
                        "Registration successful. Check your email if confirmation is required, then log in.",
                        Toast.LENGTH_LONG).show();
                goToLogin();
            }

            @Override
            public void onError(String message) {
                setLoading(false);
                Toast.makeText(RegisterActivity.this, message, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void goToLogin() {
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }

    private void setLoading(boolean loading) {
        progressRegister.setVisibility(loading ? View.VISIBLE : View.GONE);
    }
}
