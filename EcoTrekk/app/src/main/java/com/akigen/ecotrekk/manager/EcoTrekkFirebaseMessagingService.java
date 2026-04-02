package com.akigen.ecotrekk.manager;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import com.akigen.ecotrekk.R;
import com.akigen.ecotrekk.activity.HomeActivity;
import com.akigen.ecotrekk.activity.SplashActivity;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class EcoTrekkFirebaseMessagingService extends FirebaseMessagingService {

    private static final AtomicInteger NOTIFICATION_ID_COUNTER = new AtomicInteger(1000);

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        FcmManager.onNewToken(getApplicationContext(), token);
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        FcmManager.createNotificationChannel(getApplicationContext());

        String title = getString(R.string.fcm_default_title);
        String body = getString(R.string.fcm_default_body);
        String targetScreen = null;

        if (remoteMessage.getNotification() != null) {
            if (remoteMessage.getNotification().getTitle() != null && !remoteMessage.getNotification().getTitle().isBlank()) {
                title = remoteMessage.getNotification().getTitle();
            }
            if (remoteMessage.getNotification().getBody() != null && !remoteMessage.getNotification().getBody().isBlank()) {
                body = remoteMessage.getNotification().getBody();
            }
        }

        Map<String, String> data = remoteMessage.getData();
        if (data != null && !data.isEmpty()) {
            String dataTitle = data.get("title");
            String dataBody = data.get("body");
            if (dataTitle != null && !dataTitle.isBlank()) {
                title = dataTitle;
            }
            if (dataBody != null && !dataBody.isBlank()) {
                body = dataBody;
            }
            targetScreen = data.get(HomeActivity.EXTRA_TARGET_SCREEN);
        }

        showNotification(title, body, targetScreen);
    }

    private void showNotification(@NonNull String title, @NonNull String body, String targetScreen) {
        Intent intent = new Intent(this, SplashActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        if (targetScreen != null && !targetScreen.isBlank()) {
            intent.putExtra(HomeActivity.EXTRA_TARGET_SCREEN, targetScreen);
        }

        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, FcmManager.CHANNEL_GENERAL_ID)
                .setSmallIcon(R.drawable.notifications_24px)
                .setContentTitle(title)
                .setContentText(body)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(body))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                        != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        NotificationManagerCompat.from(this)
                .notify(NOTIFICATION_ID_COUNTER.incrementAndGet(), builder.build());
    }
}
