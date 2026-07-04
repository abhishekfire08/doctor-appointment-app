package com.example.doctorapp.ui.doctor;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.doctorapp.R;
import com.example.doctorapp.adapters.CategoryAdapter;
import com.example.doctorapp.adapters.DoctorAdapter;
import com.example.doctorapp.config.Config;
import com.example.doctorapp.models.Doctor;
import com.example.doctorapp.network.ApiCallback;
import com.example.doctorapp.network.ApiClient;
import com.example.doctorapp.ui.notifications.NotificationsActivity;
import com.example.doctorapp.utils.Constants;
import com.google.gson.Gson;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

public class DoctorsHomeActivity extends AppCompatActivity {

    private DoctorAdapter doctorAdapter;
    private SwipeRefreshLayout swipeRefreshDoctors;
    private EditText etSearchDoctor;

    private TextView chipAll, chipTopRated, chipExperienced, chipLowFee;
    private TextView activeChip;

    // Current filter state
    private boolean filterTopRated = false;
    private boolean filterExperienced = false;
    private boolean filterLowFee = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doctors_home);

        etSearchDoctor = findViewById(R.id.etSearchDoctor);
        swipeRefreshDoctors = findViewById(R.id.swipeRefreshDoctors);

        chipAll = findViewById(R.id.chipAll);
        chipTopRated = findViewById(R.id.chipTopRated);
        chipExperienced = findViewById(R.id.chipExperienced);
        chipLowFee = findViewById(R.id.chipLowFee);
        activeChip = chipAll;

        // Notification bell
        findViewById(R.id.tvNotifIcon).setOnClickListener(v ->
                startActivity(new Intent(this, NotificationsActivity.class)));

        // Categories horizontal list
        RecyclerView rvCategories = findViewById(R.id.rvCategories);
        rvCategories.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        rvCategories.setAdapter(new CategoryAdapter(Constants.CATEGORIES, category -> {
            Intent intent = new Intent(this, DoctorListActivity.class);
            intent.putExtra(Constants.EXTRA_CATEGORY, category);
            startActivity(intent);
        }));

        // Doctor list
        RecyclerView rvDoctors = findViewById(R.id.rvDoctors);
        rvDoctors.setLayoutManager(new LinearLayoutManager(this));
        doctorAdapter = new DoctorAdapter(doctor -> {
            Intent intent = new Intent(this, DoctorProfileActivity.class);
            intent.putExtra(Constants.EXTRA_DOCTOR_ID, doctor.id);
            startActivity(intent);
        });
        rvDoctors.setAdapter(doctorAdapter);

        // Search
        etSearchDoctor.setOnEditorActionListener((v, actionId, event) -> {
            loadDoctors();
            return true;
        });

        // Filter chips
        chipAll.setOnClickListener(v -> { resetFilters(); setActiveChip(chipAll); loadDoctors(); });
        chipTopRated.setOnClickListener(v -> { resetFilters(); filterTopRated = true; setActiveChip(chipTopRated); loadDoctors(); });
        chipExperienced.setOnClickListener(v -> { resetFilters(); filterExperienced = true; setActiveChip(chipExperienced); loadDoctors(); });
        chipLowFee.setOnClickListener(v -> { resetFilters(); filterLowFee = true; setActiveChip(chipLowFee); loadDoctors(); });

        swipeRefreshDoctors.setOnRefreshListener(this::loadDoctors);
        loadDoctors();
    }

    private void resetFilters() {
        filterTopRated = false;
        filterExperienced = false;
        filterLowFee = false;
    }

    private void setActiveChip(TextView chip) {
        // Reset previous active chip style
        activeChip.setBackgroundResource(R.drawable.bg_input);
        activeChip.setTextColor(getColor(R.color.text_secondary));

        // Set new active chip
        chip.setBackgroundResource(R.drawable.bg_rounded_button);
        chip.setTextColor(getColor(R.color.white));
        activeChip = chip;
    }

    private void loadDoctors() {
        String query = etSearchDoctor.getText().toString().trim();
        StringBuilder url = new StringBuilder(Config.restEndpoint(Constants.TABLE_DOCTORS));
        url.append("?select=*");

        if (!TextUtils.isEmpty(query)) {
            try {
                String encoded = URLEncoder.encode(query, StandardCharsets.UTF_8.toString());
                url.append("&or=(doctor_name.ilike.*").append(encoded)
                        .append("*,hospital_name.ilike.*").append(encoded)
                        .append("*,specialization.ilike.*").append(encoded).append("*)");
            } catch (Exception ignored) {}
        }

        if (filterTopRated) url.append("&rating=gte.4&order=rating.desc");
        else if (filterExperienced) url.append("&experience=gt.5&order=experience.desc");
        else if (filterLowFee) url.append("&order=fee.asc");
        else url.append("&order=rating.desc");

        ApiClient.get(url.toString(), new ApiCallback() {
            @Override
            public void onSuccess(String responseBody) {
                swipeRefreshDoctors.setRefreshing(false);
                List<Doctor> doctors = Arrays.asList(
                        new Gson().fromJson(responseBody, Doctor[].class));
                doctorAdapter.setDoctors(doctors);
                ((TextView) findViewById(R.id.tvDoctorsLabel))
                        .setText("All Doctors (" + doctors.size() + ")");
            }

            @Override
            public void onError(String message) {
                swipeRefreshDoctors.setRefreshing(false);
                Toast.makeText(DoctorsHomeActivity.this, message, Toast.LENGTH_LONG).show();
            }
        });
    }
}
