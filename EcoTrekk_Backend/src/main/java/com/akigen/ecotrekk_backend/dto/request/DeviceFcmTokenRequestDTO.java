package com.akigen.ecotrekk_backend.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeviceFcmTokenRequestDTO {
    private String fcmToken;      // Firebase token from mobile app
    private String deviceId;       // Hardware device ID
    private String deviceModel;    // Optional: device model name
    private String osVersion;      // Optional: OS version
}
