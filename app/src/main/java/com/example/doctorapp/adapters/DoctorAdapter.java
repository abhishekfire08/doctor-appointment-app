package com.example.doctorapp.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.doctorapp.R;
import com.example.doctorapp.models.Doctor;

import java.util.ArrayList;
import java.util.List;

public class DoctorAdapter extends RecyclerView.Adapter<DoctorAdapter.DoctorViewHolder> {

    public interface OnDoctorClickListener {
        void onDoctorClick(Doctor doctor);
    }

    private List<Doctor> doctors = new ArrayList<>();
    private final OnDoctorClickListener listener;

    public DoctorAdapter(OnDoctorClickListener listener) {
        this.listener = listener;
    }

    public void setDoctors(List<Doctor> newDoctors) {
        this.doctors = newDoctors;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public DoctorViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_doctor, parent, false);
        return new DoctorViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DoctorViewHolder holder, int position) {
        Doctor doctor = doctors.get(position);
        holder.name.setText(doctor.doctor_name);
        holder.specialization.setText(doctor.specialization);
        holder.hospital.setText(doctor.hospital_name);
        holder.fee.setText(String.format("\u20b9%.0f", doctor.fee));
        holder.rating.setText(String.format("\u2605 %.1f", doctor.rating));

        Glide.with(holder.itemView.getContext())
                .load(doctor.image)
                .placeholder(R.drawable.ic_placeholder_doctor)
                .error(R.drawable.ic_placeholder_doctor)
                .circleCrop()
                .into(holder.image);

        holder.itemView.setOnClickListener(v -> listener.onDoctorClick(doctor));
    }

    @Override
    public int getItemCount() {
        return doctors.size();
    }

    static class DoctorViewHolder extends RecyclerView.ViewHolder {
        ImageView image;
        TextView name, specialization, hospital, fee, rating;

        DoctorViewHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.imgDoctor);
            name = itemView.findViewById(R.id.tvDoctorName);
            specialization = itemView.findViewById(R.id.tvSpecialization);
            hospital = itemView.findViewById(R.id.tvHospital);
            fee = itemView.findViewById(R.id.tvFee);
            rating = itemView.findViewById(R.id.tvRating);
        }
    }
}
