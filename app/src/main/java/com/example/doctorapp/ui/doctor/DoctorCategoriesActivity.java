package com.example.doctorapp.ui.doctor;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.doctorapp.R;
import com.example.doctorapp.adapters.CategoryAdapter;
import com.example.doctorapp.utils.Constants;

public class DoctorCategoriesActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doctor_categories);

        findViewById(R.id.tvToolbarBack).setOnClickListener(v -> finish());
        ((TextView) findViewById(R.id.tvToolbarTitle)).setText(getString(R.string.doctor_categories));

        RecyclerView rvAllCategories = findViewById(R.id.rvAllCategories);
        rvAllCategories.setLayoutManager(new GridLayoutManager(this, 2));
        rvAllCategories.setAdapter(new CategoryAdapter(Constants.CATEGORIES, category -> {
            Intent intent = new Intent(this, DoctorListActivity.class);
            intent.putExtra(Constants.EXTRA_CATEGORY, category);
            startActivity(intent);
        }));
    }
}
