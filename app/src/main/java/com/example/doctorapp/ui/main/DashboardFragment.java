package com.example.doctorapp.ui.main;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
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
import com.example.doctorapp.ui.doctor.DoctorListActivity;
import com.example.doctorapp.ui.doctor.DoctorProfileActivity;
import com.example.doctorapp.ui.records.MedicalRecordsActivity;
import com.example.doctorapp.utils.Constants;
import com.example.doctorapp.utils.SessionManager;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;

import java.util.Arrays;
import java.util.List;

public class DashboardFragment extends Fragment {

    private TextView tvUpcomingValue, tvTotalValue, tvRecordsValue, tvFavoritesValue, tvGreeting;
    private RecyclerView rvCategories, rvPopularDoctors;
    private SwipeRefreshLayout swipeRefresh;
    private DoctorAdapter doctorAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_dashboard, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvGreeting = view.findViewById(R.id.tvGreeting);
        tvUpcomingValue = view.findViewById(R.id.tvUpcomingValue);
        tvTotalValue = view.findViewById(R.id.tvTotalValue);
        tvRecordsValue = view.findViewById(R.id.tvRecordsValue);
        tvFavoritesValue = view.findViewById(R.id.tvFavoritesValue);
        rvCategories = view.findViewById(R.id.rvCategories);
        rvPopularDoctors = view.findViewById(R.id.rvPopularDoctors);
        swipeRefresh = view.findViewById(R.id.swipeRefresh);
        EditText etSearch = view.findViewById(R.id.etSearch);

        String name = SessionManager.getFullName();
        tvGreeting.setText("Hi, " + (TextUtils.isEmpty(name) ? "there" : name));

        rvCategories.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        rvCategories.setAdapter(new CategoryAdapter(Constants.CATEGORIES, category -> {
            Intent intent = new Intent(getContext(), DoctorListActivity.class);
            intent.putExtra(Constants.EXTRA_CATEGORY, category);
            startActivity(intent);
        }));

        doctorAdapter = new DoctorAdapter(doctor -> {
            Intent intent = new Intent(getContext(), DoctorProfileActivity.class);
            intent.putExtra(Constants.EXTRA_DOCTOR_ID, doctor.id);
            startActivity(intent);
        });
        rvPopularDoctors.setLayoutManager(new LinearLayoutManager(getContext()));
        rvPopularDoctors.setAdapter(doctorAdapter);

        view.findViewById(R.id.cardRecords).setOnClickListener(v ->
                startActivity(new Intent(getContext(), MedicalRecordsActivity.class)));

        etSearch.setOnEditorActionListener((textView, actionId, event) -> {
            String query = textView.getText().toString().trim();
            Intent intent = new Intent(getContext(), DoctorListActivity.class);
            intent.putExtra("search_query", query);
            startActivity(intent);
            return true;
        });

        swipeRefresh.setOnRefreshListener(this::loadDashboardData);
        loadDashboardData();
    }

    private void loadDashboardData() {
        loadAppointmentCounts();
        loadRecordsCount();
        loadPopularDoctors();
        swipeRefresh.setRefreshing(false);
    }

    private void loadAppointmentCounts() {
        String userId = SessionManager.getUserId();
        String url = Config.restEndpoint(Constants.TABLE_APPOINTMENTS) + "?patient_id=eq." + userId;
        ApiClient.get(url, new ApiCallback() {
            @Override
            public void onSuccess(String responseBody) {
                try {
                    JsonArray array = JsonParser.parseString(responseBody).getAsJsonArray();
                    int total = array.size();
                    int upcoming = 0;
                    for (int i = 0; i < array.size(); i++) {
                        String status = array.get(i).getAsJsonObject().get("status").getAsString();
                        if (Constants.STATUS_PENDING.equals(status) || Constants.STATUS_CONFIRMED.equals(status)) {
                            upcoming++;
                        }
                    }
                    tvTotalValue.setText(String.valueOf(total));
                    tvUpcomingValue.setText(String.valueOf(upcoming));
                } catch (Exception ignored) {
                    tvTotalValue.setText("0");
                    tvUpcomingValue.setText("0");
                }
            }

            @Override
            public void onError(String message) {
                tvTotalValue.setText("--");
                tvUpcomingValue.setText("--");
            }
        });
    }

    private void loadRecordsCount() {
        String userId = SessionManager.getUserId();
        String url = Config.restEndpoint(Constants.TABLE_MEDICAL_RECORDS) + "?patient_id=eq." + userId;
        ApiClient.get(url, new ApiCallback() {
            @Override
            public void onSuccess(String responseBody) {
                try {
                    JsonArray array = JsonParser.parseString(responseBody).getAsJsonArray();
                    tvRecordsValue.setText(String.valueOf(array.size()));
                } catch (Exception e) {
                    tvRecordsValue.setText("0");
                }
            }

            @Override
            public void onError(String message) {
                tvRecordsValue.setText("--");
            }
        });
        // Favorites are not in the spec's DB tables; wire to local storage if you add a
        // favorites table or a SharedPreferences-backed set.
        tvFavoritesValue.setText("0");
    }

    private void loadPopularDoctors() {
        String url = Config.restEndpoint(Constants.TABLE_DOCTORS) + "?order=rating.desc&limit=5";
        ApiClient.get(url, new ApiCallback() {
            @Override
            public void onSuccess(String responseBody) {
                List<Doctor> doctors = Arrays.asList(new Gson().fromJson(responseBody, Doctor[].class));
                doctorAdapter.setDoctors(doctors);
            }

            @Override
            public void onError(String message) {
                // leave list empty; could show a retry/error state here
            }
        });
    }
}
