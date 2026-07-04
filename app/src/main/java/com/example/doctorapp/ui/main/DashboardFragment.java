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
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.doctorapp.R;
import com.example.doctorapp.config.Config;
import com.example.doctorapp.models.Appointment;
import com.example.doctorapp.network.ApiCallback;
import com.example.doctorapp.network.ApiClient;
import com.example.doctorapp.ui.appointment.AppointmentHistoryActivity;
import com.example.doctorapp.ui.doctor.DoctorsHomeActivity;
import com.example.doctorapp.ui.notifications.NotificationsActivity;
import com.example.doctorapp.ui.records.MedicalRecordsActivity;
import com.example.doctorapp.utils.Constants;
import com.example.doctorapp.utils.SessionManager;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;

import java.util.Arrays;
import java.util.List;

public class DashboardFragment extends Fragment {

    private TextView tvPatientName, tvUpcomingValue, tvTotalValue, tvRecordsValue;
    private TextView tvNextDoctorName, tvNextDateTime, tvNextStatus;
    private SwipeRefreshLayout swipeRefresh;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_dashboard, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvPatientName    = view.findViewById(R.id.tvPatientName);
        tvUpcomingValue  = view.findViewById(R.id.tvUpcomingValue);
        tvTotalValue     = view.findViewById(R.id.tvTotalValue);
        tvRecordsValue   = view.findViewById(R.id.tvRecordsValue);
        tvNextDoctorName = view.findViewById(R.id.tvNextDoctorName);
        tvNextDateTime   = view.findViewById(R.id.tvNextDateTime);
        tvNextStatus     = view.findViewById(R.id.tvNextStatus);
        swipeRefresh     = view.findViewById(R.id.swipeRefresh);

        // Greeting
        String name = SessionManager.getFullName();
        tvPatientName.setText(TextUtils.isEmpty(name) ? "Welcome back!" : name);

        // Search bar → opens DoctorsHomeActivity
        EditText etSearch = view.findViewById(R.id.etSearch);
        etSearch.setFocusable(false);
        etSearch.setOnClickListener(v ->
                startActivity(new Intent(getContext(), DoctorsHomeActivity.class)));

        // Notification bell
        view.findViewById(R.id.tvNotifBell).setOnClickListener(v ->
                startActivity(new Intent(getContext(), NotificationsActivity.class)));

        // Quick action cards
        view.findViewById(R.id.cardFindDoctor).setOnClickListener(v ->
                startActivity(new Intent(getContext(), DoctorsHomeActivity.class)));
        view.findViewById(R.id.cardMyAppointments).setOnClickListener(v ->
                startActivity(new Intent(getContext(), AppointmentHistoryActivity.class)));
        view.findViewById(R.id.cardMyRecords).setOnClickListener(v ->
                startActivity(new Intent(getContext(), MedicalRecordsActivity.class)));
        view.findViewById(R.id.cardFavorites).setOnClickListener(v ->
                startActivity(new Intent(getContext(), AppointmentHistoryActivity.class))); // wire to favorites when ready

        // Next appointment card
        view.findViewById(R.id.cardNextAppointment).setOnClickListener(v ->
                startActivity(new Intent(getContext(), AppointmentHistoryActivity.class)));

        swipeRefresh.setColorSchemeResources(R.color.primary, R.color.accent);
        swipeRefresh.setOnRefreshListener(this::loadDashboardData);
        loadDashboardData();
    }

    private void loadDashboardData() {
        loadAppointments();
        loadRecordsCount();
    }

    private void loadAppointments() {
        String userId = SessionManager.getUserId();
        String url = Config.restEndpoint(Constants.TABLE_APPOINTMENTS)
                + "?patient_id=eq." + userId + "&order=appointment_date.asc";

        ApiClient.get(url, new ApiCallback() {
            @Override
            public void onSuccess(String responseBody) {
                swipeRefresh.setRefreshing(false);
                try {
                    List<Appointment> appointments = Arrays.asList(
                            new Gson().fromJson(responseBody, Appointment[].class));

                    int total = appointments.size();
                    int upcoming = 0;
                    Appointment next = null;

                    for (Appointment a : appointments) {
                        if (Constants.STATUS_PENDING.equals(a.status)
                                || Constants.STATUS_CONFIRMED.equals(a.status)) {
                            upcoming++;
                            if (next == null) next = a;
                        }
                    }

                    tvTotalValue.setText(String.valueOf(total));
                    tvUpcomingValue.setText(String.valueOf(upcoming));

                    if (next != null) {
                        tvNextDoctorName.setText(next.doctor_name);
                        tvNextDateTime.setText(next.appointment_date + "  •  " + next.appointment_time);
                        tvNextStatus.setText(next.status);
                        tvNextStatus.setVisibility(View.VISIBLE);
                    } else {
                        tvNextDoctorName.setText("No upcoming appointments");
                        tvNextDateTime.setText("Book one from the Doctors tab");
                        tvNextStatus.setVisibility(View.GONE);
                    }
                } catch (Exception e) {
                    tvTotalValue.setText("0");
                    tvUpcomingValue.setText("0");
                }
            }

            @Override
            public void onError(String message) {
                swipeRefresh.setRefreshing(false);
                tvTotalValue.setText("--");
                tvUpcomingValue.setText("--");
            }
        });
    }

    private void loadRecordsCount() {
        String userId = SessionManager.getUserId();
        String url = Config.restEndpoint(Constants.TABLE_MEDICAL_RECORDS)
                + "?patient_id=eq." + userId;

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
    }
}
