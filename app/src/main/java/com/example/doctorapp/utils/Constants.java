package com.example.doctorapp.utils;

public class Constants {
    public static final String TABLE_USERS = "users";
    public static final String TABLE_DOCTORS = "doctors";
    public static final String TABLE_APPOINTMENTS = "appointments";
    public static final String TABLE_MEDICAL_RECORDS = "medical_records";
    public static final String TABLE_REVIEWS = "reviews";
    public static final String TABLE_NOTIFICATIONS = "notifications";

    public static final String STATUS_PENDING = "Pending";
    public static final String STATUS_CONFIRMED = "Confirmed";
    public static final String STATUS_COMPLETED = "Completed";
    public static final String STATUS_CANCELLED = "Cancelled";

    public static final String EXTRA_DOCTOR_ID = "extra_doctor_id";
    public static final String EXTRA_DOCTOR_NAME = "extra_doctor_name";
    public static final String EXTRA_DOCTOR_FEE = "extra_doctor_fee";
    public static final String EXTRA_CATEGORY = "extra_category";
    public static final String EXTRA_APPOINTMENT_ID = "extra_appointment_id";

    public static final String[] TIME_SLOTS = {
            "09:00 AM", "10:00 AM", "11:00 AM", "12:00 PM",
            "02:00 PM", "03:00 PM", "04:00 PM", "05:00 PM"
    };

    public static final String[] CATEGORIES = {
            "General Physician", "Cardiologist", "Dermatologist", "Orthopedic",
            "Neurologist", "Pediatrician", "Dentist", "Gynecologist",
            "ENT Specialist", "Ophthalmologist"
    };
}
