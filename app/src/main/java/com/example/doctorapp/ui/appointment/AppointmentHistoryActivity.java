package com.example.doctorapp.ui.appointment;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.doctorapp.R;
import com.example.doctorapp.adapters.AppointmentAdapter;
import com.example.doctorapp.config.Config;
import com.example.doctorapp.models.Appointment;
import com.example.doctorapp.network.ApiCallback;
import com.example.doctorapp.network.ApiClient;
import com.example.doctorapp.utils.Constants;
import com.example.doctorapp.utils.SessionManager;
import com.google.gson.Gson;

import java.util.Arrays;
import java.util.List;

/** Module 7 + 13: Appointment Management & Appointment History. */
public class AppointmentHistoryActivity extends AppCompatActivity {

    private AppointmentAdapter adapter;
    private SwipeRefreshLayout swipeRefreshAppointments;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_appointment_history);

        findViewById(R.id.tvToolbarBack).setOnClickListener(v -> finish());
        ((TextView) findViewById(R.id.tvToolbarTitle)).setText("My Appointments");

        RecyclerView rvAppointments = findViewById(R.id.rvAppointments);
        swipeRefreshAppointments = findViewById(R.id.swipeRefreshAppointments);
        rvAppointments.setLayoutManager(new LinearLayoutManager(this));

        adapter = new AppointmentAdapter(new AppointmentAdapter.OnAppointmentActionListener() {
            @Override
            public void onCancel(Appointment appointment) {
                confirmCancel(appointment);
            }

            @Override
            public void onClick(Appointment appointment) {
                // Could open a details screen; kept inline here since the card already
                // shows doctor, date/time and status.
            }
        });
        rvAppointments.setAdapter(adapter);

        swipeRefreshAppointments.setOnRefreshListener(this::loadAppointments);
        loadAppointments();
    }

    private void loadAppointments() {
        String userId = SessionManager.getUserId();
        String url = Config.restEndpoint(Constants.TABLE_APPOINTMENTS)
                + "?patient_id=eq." + userId + "&order=appointment_date.desc";

        ApiClient.get(url, new ApiCallback() {
            @Override
            public void onSuccess(String responseBody) {
                swipeRefreshAppointments.setRefreshing(false);
                List<Appointment> appointments = Arrays.asList(new Gson().fromJson(responseBody, Appointment[].class));
                adapter.setAppointments(appointments);
            }

            @Override
            public void onError(String message) {
                swipeRefreshAppointments.setRefreshing(false);
                Toast.makeText(AppointmentHistoryActivity.this, message, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void confirmCancel(Appointment appointment) {
        new AlertDialog.Builder(this)
                .setTitle("Cancel appointment?")
                .setMessage("This will cancel your appointment with " + appointment.doctor_name + ".")
                .setPositiveButton("Yes, cancel", (dialog, which) -> cancelAppointment(appointment))
                .setNegativeButton("No", null)
                .show();
    }

    private void cancelAppointment(Appointment appointment) {
        String url = Config.restEndpoint(Constants.TABLE_APPOINTMENTS) + "?id=eq." + appointment.id;
        String body = "{\"status\": \"" + Constants.STATUS_CANCELLED + "\"}";

        ApiClient.patch(url, body, new ApiCallback() {
            @Override
            public void onSuccess(String responseBody) {
                Toast.makeText(AppointmentHistoryActivity.this, "Appointment cancelled", Toast.LENGTH_SHORT).show();
                loadAppointments();
            }

            @Override
            public void onError(String message) {
                Toast.makeText(AppointmentHistoryActivity.this, message, Toast.LENGTH_LONG).show();
            }
        });
    }
}
