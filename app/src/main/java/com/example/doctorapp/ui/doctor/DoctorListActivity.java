package com.example.doctorapp.ui.doctor;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.doctorapp.R;
import com.example.doctorapp.adapters.DoctorAdapter;
import com.example.doctorapp.config.Config;
import com.example.doctorapp.models.Doctor;
import com.example.doctorapp.network.ApiCallback;
import com.example.doctorapp.network.ApiClient;
import com.example.doctorapp.utils.Constants;
import com.google.gson.Gson;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

/**
 * Lists doctors from the Supabase "doctors" table, with category filter
 * (passed in via EXTRA_CATEGORY), free-text search, and quick filter chips.
 */
public class DoctorListActivity extends AppCompatActivity {

    private RecyclerView rvDoctors;
    private SwipeRefreshLayout swipeRefreshDoctors;
    private DoctorAdapter adapter;
    private String category;
    private boolean filterTopRated = false;
    private boolean filterExperienced = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doctor_list);

        findViewById(R.id.tvToolbarBack).setOnClickListener(v -> finish());
        category = getIntent().getStringExtra(Constants.EXTRA_CATEGORY);
        ((TextView) findViewById(R.id.tvToolbarTitle))
                .setText(TextUtils.isEmpty(category) ? "Find a Doctor" : category);

        rvDoctors = findViewById(R.id.rvDoctors);
        swipeRefreshDoctors = findViewById(R.id.swipeRefreshDoctors);
        rvDoctors.setLayoutManager(new LinearLayoutManager(this));

        adapter = new DoctorAdapter(doctor -> {
            Intent intent = new Intent(this, DoctorProfileActivity.class);
            intent.putExtra(Constants.EXTRA_DOCTOR_ID, doctor.id);
            startActivity(intent);
        });
        rvDoctors.setAdapter(adapter);

        EditText etSearchDoctor = findViewById(R.id.etSearchDoctor);
        String prefilledQuery = getIntent().getStringExtra("search_query");
        if (!TextUtils.isEmpty(prefilledQuery)) {
            etSearchDoctor.setText(prefilledQuery);
        }
        etSearchDoctor.setOnEditorActionListener((v, actionId, event) -> {
            loadDoctors(etSearchDoctor.getText().toString().trim());
            return true;
        });

        findViewById(R.id.chipTopRated).setOnClickListener(v -> {
            filterTopRated = !filterTopRated;
            loadDoctors(etSearchDoctor.getText().toString().trim());
        });
        findViewById(R.id.chipExperienced).setOnClickListener(v -> {
            filterExperienced = !filterExperienced;
            loadDoctors(etSearchDoctor.getText().toString().trim());
        });
        findViewById(R.id.chipAvailableToday).setOnClickListener(v ->
                Toast.makeText(this, "Wire this to your availability field/logic.", Toast.LENGTH_SHORT).show());
        findViewById(R.id.chipLowFee).setOnClickListener(v -> loadDoctors(etSearchDoctor.getText().toString().trim(), true));

        swipeRefreshDoctors.setOnRefreshListener(() -> loadDoctors(etSearchDoctor.getText().toString().trim()));

        loadDoctors(prefilledQuery);
    }

    private void loadDoctors(String query) {
        loadDoctors(query, false);
    }

    private void loadDoctors(String query, boolean sortByFee) {
        StringBuilder url = new StringBuilder(Config.restEndpoint(Constants.TABLE_DOCTORS));
        url.append("?select=*");

        if (!TextUtils.isEmpty(category)) {
            url.append("&specialization=eq.").append(encode(category));
        }
        if (!TextUtils.isEmpty(query)) {
            // PostgREST "or" filter across a couple of text columns
            url.append("&or=(doctor_name.ilike.*").append(encode(query))
                    .append("*,hospital_name.ilike.*").append(encode(query)).append("*)");
        }
        if (filterExperienced) {
            url.append("&experience=gt.5");
        }
        if (filterTopRated) {
            url.append("&rating=gte.4");
        }
        url.append(sortByFee ? "&order=fee.asc" : "&order=rating.desc");

        ApiClient.get(url.toString(), new ApiCallback() {
            @Override
            public void onSuccess(String responseBody) {
                swipeRefreshDoctors.setRefreshing(false);
                List<Doctor> doctors = Arrays.asList(new Gson().fromJson(responseBody, Doctor[].class));
                adapter.setDoctors(doctors);
            }

            @Override
            public void onError(String message) {
                swipeRefreshDoctors.setRefreshing(false);
                Toast.makeText(DoctorListActivity.this, message, Toast.LENGTH_LONG).show();
            }
        });
    }

    private String encode(String value) {
        try {
            return URLEncoder.encode(value, StandardCharsets.UTF_8.toString());
        } catch (Exception e) {
            return value;
        }
    }
}
