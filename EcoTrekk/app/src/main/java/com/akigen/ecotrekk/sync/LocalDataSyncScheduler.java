package com.akigen.ecotrekk.sync;

import android.content.Context;

import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.ExistingWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import java.util.concurrent.TimeUnit;

/**
 * Schedules periodic backend-to-local synchronization for Room data.
 */
public final class LocalDataSyncScheduler {

    private static final String UNIQUE_WORK_NAME = "ecotrekk_local_data_sync";

    private LocalDataSyncScheduler() {
    }

    public static void schedule(Context context) {
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();

        PeriodicWorkRequest request = new PeriodicWorkRequest.Builder(
                LocalDataSyncWorker.class,
                6,
                TimeUnit.HOURS
        )
                .setConstraints(constraints)
                .build();

        WorkManager.getInstance(context.getApplicationContext())
                .enqueueUniquePeriodicWork(
                        UNIQUE_WORK_NAME,
                        ExistingPeriodicWorkPolicy.KEEP,
                        request
                );
    }

    /**
     * Runs one sync immediately (e.g. when the connection comes back) instead of waiting
     * for the next periodic run. Reuses the same worker and network constraint.
     */
    public static void syncNow(Context context) {
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();

        OneTimeWorkRequest request = new OneTimeWorkRequest.Builder(LocalDataSyncWorker.class)
                .setConstraints(constraints)
                .build();

        WorkManager.getInstance(context.getApplicationContext())
                .enqueueUniqueWork(
                        UNIQUE_WORK_NAME + "_now",
                        ExistingWorkPolicy.REPLACE,
                        request
                );
    }
}
