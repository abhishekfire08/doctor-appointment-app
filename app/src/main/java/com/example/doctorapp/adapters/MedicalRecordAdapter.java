package com.example.doctorapp.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.doctorapp.R;
import com.example.doctorapp.models.MedicalRecord;

import java.util.ArrayList;
import java.util.List;

public class MedicalRecordAdapter extends RecyclerView.Adapter<MedicalRecordAdapter.ViewHolder> {

    public interface OnRecordClickListener {
        void onDownload(MedicalRecord record);
    }

    private List<MedicalRecord> records = new ArrayList<>();
    private final OnRecordClickListener listener;

    public MedicalRecordAdapter(OnRecordClickListener listener) {
        this.listener = listener;
    }

    public void setRecords(List<MedicalRecord> list) {
        this.records = list;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_medical_record, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MedicalRecord record = records.get(position);
        holder.name.setText(record.report_name);
        holder.date.setText(record.report_date);
        holder.btnDownload.setOnClickListener(v -> listener.onDownload(record));
    }

    @Override
    public int getItemCount() {
        return records.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView name, date, btnDownload;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.tvRecordName);
            date = itemView.findViewById(R.id.tvRecordDate);
            btnDownload = itemView.findViewById(R.id.btnDownloadRecord);
        }
    }
}
