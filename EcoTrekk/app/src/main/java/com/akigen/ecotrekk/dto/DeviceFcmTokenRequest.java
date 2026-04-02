package com.akigen.ecotrekk.dto;

public class DeviceFcmTokenRequest {

    private String fcmToken;
    private String deviceId;
    private String deviceModel;
    private String osVersion;

    public DeviceFcmTokenRequest(String fcmToken, String deviceId, String deviceModel, String osVersion) {
        this.fcmToken = fcmToken;
        this.deviceId = deviceId;
        this.deviceModel = deviceModel;
        this.osVersion = osVersion;
    }

    public String getFcmToken() {
        return fcmToken;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public String getDeviceModel() {
        return deviceModel;
    }

    public String getOsVersion() {
        return osVersion;
    }
}