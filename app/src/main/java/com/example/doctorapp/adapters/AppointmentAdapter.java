package com.example.doctorapp.adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.doctorapp.R;
import com.example.doctorapp.models.Appointment;
import com.example.doctorapp.utils.Constants;

import java.util.ArrayList;
import java.util.List;

public class AppointmentAdapter extends RecyclerView.Adapter<AppointmentAdapter.ViewHolder> {

    public interface OnAppointmentActionListener {
        void onCancel(Appointment appointment);
        void onClick(Appointment appointment);
    }

    private List<Appointment> appointments = new ArrayList<>();
    private final OnAppointmentActionListener listener;

    public AppointmentAdapter(OnAppointmentActionListener listener) {
        this.listener = listener;
    }

    public void setAppointments(List<Appointment> list) {
        this.appointments = list;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_appointment, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Appointment appointment = appointments.get(position);
        holder.doctorName.setText(appointment.doctor_name);
        holder.dateTime.setText(appointment.appointment_date + "  \u2022  " + appointment.appointment_time);
        holder.status.setText(appointment.status);

        int color;
        switch (appointment.status) {
            case Constants.STATUS_CONFIRMED: color = Color.parseColor("#2F6FED"); break;
            case Constants.STATUS_COMPLETED: color = Color.parseColor("#21C17C"); break;
            case Constants.STATUS_CANCELLED: color = Color.parseColor("#E5484D"); break;
            default: color = Color.parseColor("#F59E0B");
        }
        holder.status.getBackground().setTint(color);

        boolean cancellable = Constants.STATUS_PENDING.equals(appointment.status)
                || Constants.STATUS_CONFIRMED.equals(appointment.status);
        holder.btnCancel.setVisibility(cancellable ? View.VISIBLE : View.GONE);

        holder.btnCancel.setOnClickListener(v -> listener.onCancel(appointment));
        holder.itemView.setOnClickListener(v -> listener.onClick(appointment));
    }

    @Override
    public int getItemCount() {
        return appointments.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView doctorName, dateTime, status, btnCancel;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            doctorName = itemView.findViewById(R.id.tvAppointmentDoctorName);
            dateTime = itemView.findViewById(R.id.tvAppointmentDateTime);
            status = itemView.findViewById(R.id.tvAppointmentStatus);
            btnCancel = itemView.findViewById(R.id.btnCancelAppointment);
        }
    }
}
