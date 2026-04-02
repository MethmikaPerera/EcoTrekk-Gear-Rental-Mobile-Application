package com.akigen.ecotrekk.manager;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.appcompat.app.AppCompatDelegate;

public class ThemePreferenceManager {

    private static final String PREFS_NAME = "app_settings";
    private static final String KEY_THEME_MODE = "theme_mode";

    private final SharedPreferences preferences;

    public ThemePreferenceManager(Context context) {
        preferences = context.getApplicationContext()
                .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public int getThemeMode() {
        return preferences.getInt(KEY_THEME_MODE, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
    }

    public void saveThemeMode(int mode) {
        preferences.edit().putInt(KEY_THEME_MODE, mode).apply();
    }

    public void applySavedThemeMode() {
        AppCompatDelegate.setDefaultNightMode(getThemeMode());
    }
}
