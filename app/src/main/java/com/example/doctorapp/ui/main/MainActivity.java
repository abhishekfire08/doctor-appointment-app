package com.example.doctorapp.ui.main;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
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
            showFragment(new DashboardFragment());
        }

        BottomNavigationView bottomNavigation = findViewById(R.id.bottomNavigation);
        bottomNavigation.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                showFragment(new DashboardFragment());
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

    private void showFragment(androidx.fragment.app.Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragmentContainer, fragment);
        transaction.commit();
    }
}
