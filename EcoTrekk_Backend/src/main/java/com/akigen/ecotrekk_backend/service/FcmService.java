package com.akigen.ecotrekk_backend.service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class FcmService {
    
    private final FirebaseMessaging firebaseMessaging;
    
    public FcmService(FirebaseMessaging firebaseMessaging) {
        this.firebaseMessaging = firebaseMessaging;
    }
    
    /**
     * Send a notification to a specific device via FCM token
     */
    public String sendNotification(String fcmToken, String title, String body, String targetScreen) {
        try {
            Message.Builder messageBuilder = Message.builder()
                    .setToken(fcmToken)
                    .setNotification(Notification.builder()
                            .setTitle(title)
                            .setBody(body)
                            .build());
            
            // Add optional target screen for deep-linking
            if (targetScreen != null && !targetScreen.isEmpty()) {
                messageBuilder.putData("target_screen", targetScreen);
            }
            
            String messageId = firebaseMessaging.send(messageBuilder.build());
            log.info("FCM notification sent successfully. Message ID: {}, Token: {}", messageId, fcmToken);
            return messageId;
            
        } catch (Exception e) {
            log.error("Failed to send FCM notification to token: {}", fcmToken, e);
            throw new RuntimeException("Failed to send FCM notification", e);
        }
    }
    
    /**
     * Send a notification with custom data payload
     */
    public String sendNotificationWithData(String fcmToken, String title, String body, 
                                          java.util.Map<String, String> dataPayload) {
        try {
            Message.Builder messageBuilder = Message.builder()
                    .setToken(fcmToken)
                    .setNotification(Notification.builder()
                            .setTitle(title)
                            .setBody(body)
                            .build());
            
            if (dataPayload != null && !dataPayload.isEmpty()) {
                messageBuilder.putAllData(dataPayload);
            }
            
            String messageId = firebaseMessaging.send(messageBuilder.build());
            log.info("FCM notification with data sent successfully. Message ID: {}", messageId);
            return messageId;
            
        } catch (Exception e) {
            log.error("Failed to send FCM notification with data to token: {}", fcmToken, e);
            throw new RuntimeException("Failed to send FCM notification", e);
        }
    }
    
    /**
     * Send notification to multiple devices
     */
    public void sendNotificationToMultipleDevices(java.util.List<String> fcmTokens, 
                                                  String title, String body, 
                                                  String targetScreen) {
        for (String token : fcmTokens) {
            try {
                sendNotification(token, title, body, targetScreen);
            } catch (Exception e) {
                log.warn("Failed to send notification to token: {}", token, e);
                // Continue with other tokens
            }
        }
    }
}
