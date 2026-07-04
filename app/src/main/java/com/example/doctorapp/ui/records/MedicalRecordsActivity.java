package com.example.doctorapp.ui.records;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.doctorapp.R;
import com.example.doctorapp.adapters.MedicalRecordAdapter;
import com.example.doctorapp.config.Config;
import com.example.doctorapp.models.MedicalRecord;
import com.example.doctorapp.network.ApiCallback;
import com.example.doctorapp.network.ApiClient;
import com.example.doctorapp.utils.Constants;
import com.example.doctorapp.utils.SessionManager;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/** Module 8: Medical Records Management. */
public class MedicalRecordsActivity extends AppCompatActivity {

    private MedicalRecordAdapter adapter;
    private SwipeRefreshLayout swipeRefreshRecords;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_medical_records);

        findViewById(R.id.tvToolbarBack).setOnClickListener(v -> finish());
        ((TextView) findViewById(R.id.tvToolbarTitle)).setText("Medical Records");

        RecyclerView rvMedicalRecords = findViewById(R.id.rvMedicalRecords);
        swipeRefreshRecords = findViewById(R.id.swipeRefreshRecords);
        rvMedicalRecords.setLayoutManager(new LinearLayoutManager(this));

        adapter = new MedicalRecordAdapter(record -> {
            if (!TextUtils.isEmpty(record.file_url)) {
                Toast.makeText(this, "Open this URL in a browser/PDF viewer: " + record.file_url, Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "No file URL stored for this record.", Toast.LENGTH_SHORT).show();
            }
        });
        rvMedicalRecords.setAdapter(adapter);

        swipeRefreshRecords.setOnRefreshListener(this::loadRecords);
        findViewById(R.id.btnUploadReport).setOnClickListener(v -> showAddRecordDialog());

        loadRecords();
    }

    private void loadRecords() {
        String userId = SessionManager.getUserId();
        String url = Config.restEndpoint(Constants.TABLE_MEDICAL_RECORDS)
                + "?patient_id=eq." + userId + "&order=report_date.desc";

        ApiClient.get(url, new ApiCallback() {
            @Override
            public void onSuccess(String responseBody) {
                swipeRefreshRecords.setRefreshing(false);
                List<MedicalRecord> records = Arrays.asList(new Gson().fromJson(responseBody, MedicalRecord[].class));
                adapter.setRecords(records);
            }

            @Override
            public void onError(String message) {
                swipeRefreshRecords.setRefreshing(false);
                Toast.makeText(MedicalRecordsActivity.this, message, Toast.LENGTH_LONG).show();
            }
        });
    }

    /**
     * Stand-in "upload" flow: this demo lets you record a report name + a file URL
     * (e.g. a link you already have, or a Supabase Storage public URL once you wire
     * actual file uploads via Supabase Storage's REST API).
     */
    private void showAddRecordDialog() {
        android.view.View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_record, null);
        EditText etReportName = dialogView.findViewById(R.id.etReportName);
        EditText etFileUrl = dialogView.findViewById(R.id.etFileUrl);

        new AlertDialog.Builder(this)
                .setTitle("Add Medical Record")
                .setView(dialogView)
                .setPositiveButton("Save", (dialog, which) -> {
                    String name = etReportName.getText().toString().trim();
                    String fileUrl = etFileUrl.getText().toString().trim();
                    if (TextUtils.isEmpty(name)) {
                        Toast.makeText(this, "Enter a report name", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    saveRecord(name, fileUrl);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void saveRecord(String reportName, String fileUrl) {
        JsonObject body = new JsonObject();
        body.addProperty("patient_id", SessionManager.getUserId());
        body.addProperty("report_name", reportName);
        body.addProperty("report_date", new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date()));
        body.addProperty("file_url", fileUrl);

        ApiClient.post(Config.restEndpoint(Constants.TABLE_MEDICAL_RECORDS), body.toString(), true, new ApiCallback() {
            @Override
            public void onSuccess(String responseBody) {
                Toast.makeText(MedicalRecordsActivity.this, "Record saved", Toast.LENGTH_SHORT).show();
                loadRecords();
            }

            @Override
            public void onError(String message) {
                Toast.makeText(MedicalRecordsActivity.this, message, Toast.LENGTH_LONG).show();
            }
        });
    }
}
