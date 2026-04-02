package com.akigen.ecotrekk.manager;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;

import androidx.annotation.NonNull;

import com.akigen.ecotrekk.R;
import com.akigen.ecotrekk.api.ApiService;
import com.akigen.ecotrekk.client.RetrofitClient;
import com.akigen.ecotrekk.dto.DeviceFcmTokenRequest;
import com.akigen.ecotrekk.dto.DeviceFcmTokenResponse;
import com.google.firebase.messaging.FirebaseMessaging;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public final class FcmManager {

    private static final String TAG = "FcmManager";
    private static final String PREFS_NAME = "ecotrekk_fcm";
    private static final String KEY_FCM_TOKEN = "fcm_token";

    public static final String CHANNEL_GENERAL_ID = "ecotrekk_general_notifications";

    private FcmManager() {
    }

    public static void initialize(@NonNull Context context) {
        Context appContext = context.getApplicationContext();
        createNotificationChannel(appContext);
        fetchAndStoreToken(appContext);
    }

    public static void createNotificationChannel(@NonNull Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return;
        }

        NotificationManager manager = context.getSystemService(NotificationManager.class);
        if (manager == null) {
            return;
        }

        NotificationChannel channel = new NotificationChannel(
                CHANNEL_GENERAL_ID,
                context.getString(R.string.fcm_channel_name),
                NotificationManager.IMPORTANCE_HIGH
        );
        channel.setDescription(context.getString(R.string.fcm_channel_description));
        manager.createNotificationChannel(channel);
    }

    public static void fetchAndStoreToken(@NonNull Context context) {
        FirebaseMessaging.getInstance().getToken()
                .addOnSuccessListener(token -> {
                    if (token != null && !token.isBlank()) {
                        saveToken(context, token);
                    }
                })
                .addOnFailureListener(e -> Log.w(TAG, "Failed to fetch FCM token", e));
    }

    public static void onNewToken(@NonNull Context context, @NonNull String token) {
        saveToken(context, token);
        syncTokenToBackend(context, token);
    }

    public static void syncTokenToBackend(@NonNull Context context) {
        String token = getToken(context);
        if (token == null || token.trim().isEmpty()) {
            return;
        }
        syncTokenToBackend(context, token);
    }

    public static void syncTokenToBackend(@NonNull Context context, @NonNull String token) {
        Context appContext = context.getApplicationContext();
        SessionManager sessionManager = new SessionManager(appContext);
        long customerId = sessionManager.getCustomerId();

        if (customerId <= 0) {
            Log.d(TAG, "Skipping FCM sync: no logged-in customer session");
            return;
        }

        String deviceId = Settings.Secure.getString(
                appContext.getContentResolver(),
                Settings.Secure.ANDROID_ID
        );
        if (deviceId == null || deviceId.trim().isEmpty()) {
            deviceId = "unknown_device";
        }

        String deviceModel = Build.MODEL != null ? Build.MODEL : "Unknown";
        String osVersion = "Android " + Build.VERSION.RELEASE + " (API " + Build.VERSION.SDK_INT + ")";

        DeviceFcmTokenRequest request = new DeviceFcmTokenRequest(token, deviceId, deviceModel, osVersion);
        ApiService apiService = RetrofitClient.getInstance().getApiService();
        apiService.registerDeviceToken(customerId, request).enqueue(new Callback<DeviceFcmTokenResponse>() {
            @Override
            public void onResponse(@NonNull Call<DeviceFcmTokenResponse> call,
                                   @NonNull Response<DeviceFcmTokenResponse> response) {
                if (response.isSuccessful()) {
                    Log.d(TAG, "FCM token synced after refresh for customer: " + customerId);
                } else {
                    Log.w(TAG, "FCM token sync failed with code: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<DeviceFcmTokenResponse> call, @NonNull Throwable t) {
                Log.w(TAG, "FCM token sync request failed", t);
            }
        });
    }

    public static String getToken(@NonNull Context context) {
        SharedPreferences prefs = context.getApplicationContext()
                .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getString(KEY_FCM_TOKEN, "");
    }

    private static void saveToken(@NonNull Context context, @NonNull String token) {
        SharedPreferences prefs = context.getApplicationContext()
                .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        String currentToken = prefs.getString(KEY_FCM_TOKEN, "");
        if (token.equals(currentToken)) {
            return;
        }

        prefs.edit().putString(KEY_FCM_TOKEN, token).apply();
        Log.d(TAG, "FCM token updated");
    }
}
