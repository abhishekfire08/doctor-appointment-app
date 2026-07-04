package com.example.doctorapp.ui.main;

import com.example.doctorapp.ui.doctor.DoctorsHomeActivity;
import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.doctorapp.R;
import com.example.doctorapp.ui.appointment.AppointmentHistoryActivity;
import com.example.doctorapp.ui.profile.ProfileActivity;
import com.example.doctorapp.ui.records.MedicalRecordsActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState == null) {
            loadFragment(new DashboardFragment());
        }

        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigation);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                loadFragment(new DashboardFragment());
                return true;
            } else if (id == R.id.nav_doctors) {
                startActivity(new Intent(this, DoctorsHomeActivity.class));
                return true;
            } else if (id == R.id.nav_appointments) {
                startActivity(new Intent(this, AppointmentHistoryActivity.class));
                return true;
            } else if (id == R.id.nav_records) {
                startActivity(new Intent(this, MedicalRecordsActivity.class));
                return true;
            } else if (id == R.id.nav_profile) {
                startActivity(new Intent(this, ProfileActivity.class));
                return true;
            }
            return false;
        });
    }

    private void loadFragment(Fragment fragment) {
        FragmentTransaction tx = getSupportFragmentManager().beginTransaction();
        tx.replace(R.id.fragmentContainer, fragment);
        tx.commit();
    }
}
