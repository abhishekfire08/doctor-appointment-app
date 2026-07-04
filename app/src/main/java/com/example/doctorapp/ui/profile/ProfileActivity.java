package com.example.doctorapp.ui.profile;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.doctorapp.R;
import com.example.doctorapp.config.Config;
import com.example.doctorapp.models.User;
import com.example.doctorapp.network.ApiCallback;
import com.example.doctorapp.network.ApiClient;
import com.example.doctorapp.ui.auth.LoginActivity;
import com.example.doctorapp.utils.Constants;
import com.example.doctorapp.utils.SessionManager;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/** Module 9: Patient Profile (edit profile, update contact, change password, photo). */
public class ProfileActivity extends AppCompatActivity {

    private EditText etProfileName, etProfileEmail, etProfileMobile, etProfileAddress, etProfileBloodGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        findViewById(R.id.tvToolbarBack).setOnClickListener(v -> finish());
        ((TextView) findViewById(R.id.tvToolbarTitle)).setText("My Profile");

        etProfileName = findViewById(R.id.etProfileName);
        etProfileEmail = findViewById(R.id.etProfileEmail);
        etProfileMobile = findViewById(R.id.etProfileMobile);
        etProfileAddress = findViewById(R.id.etProfileAddress);
        etProfileBloodGroup = findViewById(R.id.etProfileBloodGroup);

        findViewById(R.id.tvChangePhoto).setOnClickListener(v ->
                Toast.makeText(this, "Wire this to an image picker + Supabase Storage upload.", Toast.LENGTH_SHORT).show());

        findViewById(R.id.btnSaveProfile).setOnClickListener(v -> saveProfile());
        findViewById(R.id.btnChangePassword).setOnClickListener(v -> changePassword());
        findViewById(R.id.btnLogout).setOnClickListener(v -> confirmLogout());

        loadProfile();
    }

    private void loadProfile() {
        String userId = SessionManager.getUserId();
        String url = Config.restEndpoint(Constants.TABLE_USERS) + "?id=eq." + userId;

        ApiClient.get(url, new ApiCallback() {
            @Override
            public void onSuccess(String responseBody) {
                JsonArray array = JsonParser.parseString(responseBody).getAsJsonArray();
                if (array.size() == 0) return;
                User user = new Gson().fromJson(array.get(0), User.class);
                etProfileName.setText(user.name);
                etProfileEmail.setText(user.email);
                etProfileMobile.setText(user.mobile);
                etProfileAddress.setText(user.address);
                etProfileBloodGroup.setText(user.blood_group);
            }

            @Override
            public void onError(String message) {
                Toast.makeText(ProfileActivity.this, message, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void saveProfile() {
        String name = etProfileName.getText().toString().trim();
        if (TextUtils.isEmpty(name)) {
            Toast.makeText(this, "Name can't be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        JsonObject body = new JsonObject();
        body.addProperty("name", name);
        body.addProperty("mobile", etProfileMobile.getText().toString().trim());
        body.addProperty("address", etProfileAddress.getText().toString().trim());
        body.addProperty("blood_group", etProfileBloodGroup.getText().toString().trim());

        String url = Config.restEndpoint(Constants.TABLE_USERS) + "?id=eq." + SessionManager.getUserId();
        ApiClient.patch(url, body.toString(), new ApiCallback() {
            @Override
            public void onSuccess(String responseBody) {
                SessionManager.updateFullName(name);
                Toast.makeText(ProfileActivity.this, "Profile updated", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(String message) {
                Toast.makeText(ProfileActivity.this, message, Toast.LENGTH_LONG).show();
            }
        });
    }

    /** Uses Supabase Auth's /auth/v1/user endpoint (PUT) with the current access token. */
    private void changePassword() {
        EditText etNewPassword = findViewById(R.id.etNewPassword);
        String newPassword = etNewPassword.getText().toString().trim();
        if (newPassword.length() < 6) {
            Toast.makeText(this, "Password should be at least 6 characters", Toast.LENGTH_SHORT).show();
            return;
        }

        JsonObject body = new JsonObject();
        body.addProperty("password", newPassword);

        ApiClient.put(Config.AUTH_USER_ENDPOINT, body.toString(), new ApiCallback() {
            @Override
            public void onSuccess(String responseBody) {
                Toast.makeText(ProfileActivity.this, "Password updated", Toast.LENGTH_SHORT).show();
                etNewPassword.setText("");
            }

            @Override
            public void onError(String message) {
                Toast.makeText(ProfileActivity.this, message, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void confirmLogout() {
        new AlertDialog.Builder(this)
                .setTitle("Logout?")
                .setMessage("You'll need to log in again to book or manage appointments.")
                .setPositiveButton("Logout", (dialog, which) -> {
                    SessionManager.clear();
                    Intent intent = new Intent(this, LoginActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
