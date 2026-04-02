package com.akigen.ecotrekk.dto;

public class DeviceFcmTokenResponse {

    private Long id;
    private String deviceId;
    private String deviceModel;
    private String osVersion;
    private Boolean isActive;
    private String lastUpdated;

    public Long getId() {
        return id;
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

    public Boolean getIsActive() {
        return isActive;
    }

    public String getLastUpdated() {
        return lastUpdated;
    }
}