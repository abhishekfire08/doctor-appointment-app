package com.example.doctorapp.config;

/**
 * Fill these in with your own Supabase project details.
 * Project Settings -> API in the Supabase dashboard.
 */
public class Config {
    public static final String SUPABASE_URL = "https://dojjbzwyakagxgfioouj.supabase.co";
    public static final String SUPABASE_ANON_KEY = "sb_publishable_uPhDB6_nAM9efgJ4R5O0zg_dHa02oJd";

    public static final String AUTH_SIGNUP_ENDPOINT = SUPABASE_URL + "/auth/v1/signup";
    public static final String AUTH_LOGIN_ENDPOINT = SUPABASE_URL + "/auth/v1/token?grant_type=password";
    public static final String AUTH_RECOVER_ENDPOINT = SUPABASE_URL + "/auth/v1/recover";
    public static final String AUTH_USER_ENDPOINT = SUPABASE_URL + "/auth/v1/user";

    public static String restEndpoint(String table) {
        return SUPABASE_URL + "/rest/v1/" + table;
    }
}
