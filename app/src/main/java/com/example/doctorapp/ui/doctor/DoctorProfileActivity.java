package com.example.doctorapp.ui.doctor;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.doctorapp.R;
import com.example.doctorapp.config.Config;
import com.example.doctorapp.models.Doctor;
import com.example.doctorapp.network.ApiCallback;
import com.example.doctorapp.network.ApiClient;
import com.example.doctorapp.ui.appointment.AppointmentBookingActivity;
import com.example.doctorapp.ui.reviews.ReviewsActivity;
import com.example.doctorapp.utils.Constants;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;

public class DoctorProfileActivity extends AppCompatActivity {

    private Doctor doctor;
    private String doctorId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doctor_profile);

        findViewById(R.id.tvToolbarBack).setOnClickListener(v -> finish());
        ((TextView) findViewById(R.id.tvToolbarTitle)).setText("Doctor Profile");

        doctorId = getIntent().getStringExtra(Constants.EXTRA_DOCTOR_ID);
        loadDoctor();

        findViewById(R.id.btnBookAppointment).setOnClickListener(v -> {
            if (doctor == null) return;
            Intent intent = new Intent(this, AppointmentBookingActivity.class);
            intent.putExtra(Constants.EXTRA_DOCTOR_ID, doctor.id);
            intent.putExtra(Constants.EXTRA_DOCTOR_NAME, doctor.doctor_name);
            intent.putExtra(Constants.EXTRA_DOCTOR_FEE, doctor.fee);
            startActivity(intent);
        });

        findViewById(R.id.btnAddFavorite).setOnClickListener(v ->
                Toast.makeText(this, "Added to favourites (wire to a favorites table to persist).", Toast.LENGTH_SHORT).show());

        findViewById(R.id.tvViewReviews).setOnClickListener(v -> {
            Intent intent = new Intent(this, ReviewsActivity.class);
            intent.putExtra(Constants.EXTRA_DOCTOR_ID, doctorId);
            intent.putExtra(Constants.EXTRA_DOCTOR_NAME, doctor != null ? doctor.doctor_name : "");
            startActivity(intent);
        });
    }

    private void loadDoctor() {
        String url = Config.restEndpoint(Constants.TABLE_DOCTORS) + "?id=eq." + doctorId + "&select=*";
        ApiClient.get(url, new ApiCallback() {
            @Override
            public void onSuccess(String responseBody) {
                JsonArray array = JsonParser.parseString(responseBody).getAsJsonArray();
                if (array.size() == 0) {
                    Toast.makeText(DoctorProfileActivity.this, "Doctor not found", Toast.LENGTH_SHORT).show();
                    return;
                }
                doctor = new Gson().fromJson(array.get(0), Doctor.class);
                bindDoctor();
            }

            @Override
            public void onError(String message) {
                Toast.makeText(DoctorProfileActivity.this, message, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void bindDoctor() {
        ((TextView) findViewById(R.id.tvProfileName)).setText(doctor.doctor_name);
        String qualification = doctor.qualification != null ? " \u2022 " + doctor.qualification : "";
        ((TextView) findViewById(R.id.tvProfileSpecialization)).setText(doctor.specialization + qualification);
        ((TextView) findViewById(R.id.tvProfileHospital)).setText(doctor.hospital_name);
        ((TextView) findViewById(R.id.tvProfileExperience)).setText(doctor.experience + " yrs");
        ((TextView) findViewById(R.id.tvProfileFee)).setText(String.format("\u20b9%.0f", doctor.fee));
        ((TextView) findViewById(R.id.tvProfileRating)).setText(String.format("\u2605 %.1f", doctor.rating));

        Glide.with(this)
                .load(doctor.image)
                .placeholder(R.drawable.ic_placeholder_doctor)
                .error(R.drawable.ic_placeholder_doctor)
                .circleCrop()
                .into((ImageView) findViewById(R.id.imgDoctorProfile));
    }
}
