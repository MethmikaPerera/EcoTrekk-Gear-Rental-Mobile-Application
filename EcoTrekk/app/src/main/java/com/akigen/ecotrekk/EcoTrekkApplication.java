package com.akigen.ecotrekk;

import android.app.Application;

import com.akigen.ecotrekk.manager.ThemePreferenceManager;
import com.akigen.ecotrekk.manager.FcmManager;
import com.akigen.ecotrekk.sync.LocalDataSyncScheduler;

public class EcoTrekkApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        new ThemePreferenceManager(this).applySavedThemeMode();
        FcmManager.initialize(this);
        LocalDataSyncScheduler.schedule(this);
    }
}
