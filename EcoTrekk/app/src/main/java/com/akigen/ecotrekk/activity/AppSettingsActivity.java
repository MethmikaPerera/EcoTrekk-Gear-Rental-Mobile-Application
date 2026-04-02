package com.akigen.ecotrekk.activity;

import android.os.Bundle;
import android.widget.RadioGroup;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.akigen.ecotrekk.R;
import com.akigen.ecotrekk.databinding.ActivityAppSettingsBinding;
import com.akigen.ecotrekk.manager.ThemePreferenceManager;

public class AppSettingsActivity extends AppCompatActivity {

    private ActivityAppSettingsBinding binding;
    private ThemePreferenceManager themePreferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        themePreferenceManager = new ThemePreferenceManager(this);
        binding = ActivityAppSettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.appSettingsBackButton.setOnClickListener(v -> {
            finish();
        });

        setupThemeSelector();
    }

    private void setupThemeSelector() {
        RadioGroup themeGroup = binding.themeModeRadioGroup;
        int currentMode = themePreferenceManager.getThemeMode();

        if (currentMode == AppCompatDelegate.MODE_NIGHT_YES) {
            themeGroup.check(R.id.radioThemeDark);
        } else if (currentMode == AppCompatDelegate.MODE_NIGHT_NO) {
            themeGroup.check(R.id.radioThemeLight);
        } else {
            themeGroup.check(R.id.radioThemeSystem);
        }

        themeGroup.setOnCheckedChangeListener((group, checkedId) -> {
            int selectedMode = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;

            if (checkedId == R.id.radioThemeLight) {
                selectedMode = AppCompatDelegate.MODE_NIGHT_NO;
            } else if (checkedId == R.id.radioThemeDark) {
                selectedMode = AppCompatDelegate.MODE_NIGHT_YES;
            }

            AppCompatDelegate.setDefaultNightMode(selectedMode);
            themePreferenceManager.saveThemeMode(selectedMode);
        });
    }
}