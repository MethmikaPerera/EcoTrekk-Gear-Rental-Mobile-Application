package com.akigen.ecotrekk.model;

public class HubLocation {
    private final String name;
    private final String city;
    private final String phone;
    private final String imageUrl;
    private final String distanceText;
    private final String openHoursText;
    private final double latitude;
    private final double longitude;

    public HubLocation(String name,
                       String city,
                       String phone,
                       String imageUrl,
                       String distanceText,
                       String openHoursText,
                       double latitude,
                       double longitude) {
        this.name = name;
        this.city = city;
        this.phone = phone;
        this.imageUrl = imageUrl;
        this.distanceText = distanceText;
        this.openHoursText = openHoursText;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getName() {
        return name;
    }

    public String getCity() {
        return city;
    }

    public String getPhone() {
        return phone;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getDistanceText() {
        return distanceText;
    }

    public String getOpenHoursText() {
        return openHoursText;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }
}
