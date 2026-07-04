package com.example.doctorapp.ui.appointment;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.GridLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.doctorapp.R;
import com.example.doctorapp.config.Config;
import com.example.doctorapp.network.ApiCallback;
import com.example.doctorapp.network.ApiClient;
import com.example.doctorapp.utils.Constants;
import com.example.doctorapp.utils.SessionManager;
import com.google.gson.JsonObject;

import java.util.Calendar;

public class AppointmentBookingActivity extends AppCompatActivity {

    private String doctorId, doctorName, selectedDate, selectedTime;
    private double fee;
    private TextView tvSelectedDate;
    private ProgressBar progressBooking;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_appointment_booking);

        findViewById(R.id.tvToolbarBack).setOnClickListener(v -> finish());
        ((TextView) findViewById(R.id.tvToolbarTitle)).setText("Book Appointment");

        doctorId = getIntent().getStringExtra(Constants.EXTRA_DOCTOR_ID);
        doctorName = getIntent().getStringExtra(Constants.EXTRA_DOCTOR_NAME);
        fee = getIntent().getDoubleExtra(Constants.EXTRA_DOCTOR_FEE, 0);

        ((TextView) findViewById(R.id.tvBookingDoctorName)).setText(doctorName);
        ((TextView) findViewById(R.id.tvBookingFee)).setText(String.format("Consultation Fee: \u20b9%.0f", fee));

        tvSelectedDate = findViewById(R.id.tvSelectedDate);
        progressBooking = findViewById(R.id.progressBooking);

        tvSelectedDate.setOnClickListener(v -> showDatePicker());
        buildTimeSlotChips();

        findViewById(R.id.btnConfirmAppointment).setOnClickListener(v -> confirmAppointment());
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog dialog = new DatePickerDialog(this, (view, year, month, day) -> {
            selectedDate = String.format("%02d-%02d-%04d", day, month + 1, year);
            tvSelectedDate.setText(selectedDate);
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        dialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
        dialog.show();
    }

    private void buildTimeSlotChips() {
        GridLayout layoutTimeSlots = findViewById(R.id.layoutTimeSlots);
        for (String slot : Constants.TIME_SLOTS) {
            TextView chip = new TextView(this);
            chip.setText(slot);
            chip.setTextSize(12);
            chip.setPadding(20, 16, 20, 16);
            chip.setBackgroundResource(R.drawable.bg_input);
            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.setMargins(6, 6, 6, 6);
            chip.setLayoutParams(params);
            chip.setOnClickListener(v -> selectTimeSlot(layoutTimeSlots, chip, slot));
            layoutTimeSlots.addView(chip);
        }
    }

    private void selectTimeSlot(GridLayout container, TextView selectedChip, String slot) {
        for (int i = 0; i < container.getChildCount(); i++) {
            View child = container.getChildAt(i);
            child.setBackgroundResource(R.drawable.bg_input);
            ((TextView) child).setTextColor(getColor(R.color.text_secondary));
        }
        selectedChip.setBackgroundResource(R.drawable.bg_rounded_button);
        selectedChip.setTextColor(getColor(R.color.white));
        selectedTime = slot;
    }

    private void confirmAppointment() {
        if (selectedDate == null || selectedTime == null) {
            Toast.makeText(this, "Please select a date and time slot", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBooking.setVisibility(View.VISIBLE);

        JsonObject body = new JsonObject();
        body.addProperty("patient_id", SessionManager.getUserId());
        body.addProperty("doctor_id", doctorId);
        body.addProperty("doctor_name", doctorName);
        body.addProperty("appointment_date", selectedDate);
        body.addProperty("appointment_time", selectedTime);
        body.addProperty("status", Constants.STATUS_PENDING);

        ApiClient.post(Config.restEndpoint(Constants.TABLE_APPOINTMENTS), body.toString(), true, new ApiCallback() {
            @Override
            public void onSuccess(String responseBody) {
                progressBooking.setVisibility(View.GONE);
                Toast.makeText(AppointmentBookingActivity.this,
                        "Appointment booked with " + doctorName, Toast.LENGTH_LONG).show();
                finish();
            }

            @Override
            public void onError(String message) {
                progressBooking.setVisibility(View.GONE);
                Toast.makeText(AppointmentBookingActivity.this, message, Toast.LENGTH_LONG).show();
            }
        });
    }
}
