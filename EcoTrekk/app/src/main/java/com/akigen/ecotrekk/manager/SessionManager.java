package com.akigen.ecotrekk.manager;

import android.content.Context;
import android.content.SharedPreferences;

import com.akigen.ecotrekk.dto.CustomerAuthResponse;

public class SessionManager {

    private static final String PREF_NAME = "ecotrekk_session";
    private static final String KEY_CUSTOMER_ID = "customer_id";
    private static final String KEY_FULL_NAME = "full_name";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_PHONE = "phone";
    private static final String KEY_PHOTO_URL = "photo_url";
    private static final String KEY_LOGGED_IN = "logged_in";

    private final SharedPreferences prefs;
    private final SharedPreferences.Editor editor;

    public SessionManager(Context context) {
        prefs = context.getApplicationContext()
                .getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = prefs.edit();
    }

    public void saveSession(CustomerAuthResponse response) {
        editor.putBoolean(KEY_LOGGED_IN, true);
        editor.putLong(KEY_CUSTOMER_ID, response.getCustomerId() != null ? response.getCustomerId() : -1L);
        editor.putString(KEY_FULL_NAME, response.getFullName());
        editor.putString(KEY_EMAIL, response.getEmail());
        editor.putString(KEY_PHONE, response.getPhone() != null ? response.getPhone() : "");
        editor.putString(KEY_PHOTO_URL, response.getProfilePictureUrl() != null ? response.getProfilePictureUrl() : "");
        editor.apply();
    }

    public boolean isLoggedIn() {
        return prefs.getBoolean(KEY_LOGGED_IN, false);
    }

    public long getCustomerId() {
        return prefs.getLong(KEY_CUSTOMER_ID, -1L);
    }

    public String getFullName() {
        return prefs.getString(KEY_FULL_NAME, "");
    }

    public String getEmail() {
        return prefs.getString(KEY_EMAIL, "");
    }

    public String getPhone() {
        return prefs.getString(KEY_PHONE, "");
    }

    public String getPhotoUrl() {
        return prefs.getString(KEY_PHOTO_URL, "");
    }

    public void clearSession() {
        editor.clear().apply();
    }
}
