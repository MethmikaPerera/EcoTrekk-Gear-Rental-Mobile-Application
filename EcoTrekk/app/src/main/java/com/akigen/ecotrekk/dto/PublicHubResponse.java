package com.akigen.ecotrekk.dto;

public class PublicHubResponse {
    private Long id;
    private String name;
    private String address;
    private String phone;
    private double latitude;
    private double longitude;
    private String mapLocationUrl;
    private String imageUrl;

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }

    public String getPhone() {
        return phone;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public String getMapLocationUrl() {
        return mapLocationUrl;
    }

    public String getImageUrl() {
        return imageUrl;
    }
}
