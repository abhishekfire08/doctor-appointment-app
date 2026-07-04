package com.example.doctorapp.models;

public class User {
    public String id;
    public String name;
    public String email;
    public String mobile;
    public String gender;
    public String dob;
    public String profile_image;
    public String address;
    public String blood_group;

    public User() {}

    public User(String id, String name, String email, String mobile) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.mobile = mobile;
    }
}
