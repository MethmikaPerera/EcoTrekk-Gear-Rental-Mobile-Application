package com.akigen.ecotrekk.dto;

public class PublicFeaturedGearResponse {
    private Long id;
    private String name;
    private String category;
    private String description;
    private double pricePerDay;
    private double depositAmount;
    private String imageUrl;
    private Integer availableQty;

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getCategory() {
        return category;
    }

    public String getDescription() {
        return description;
    }

    public double getPricePerDay() {
        return pricePerDay;
    }

    public double getDepositAmount() {
        return depositAmount;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public Integer getAvailableQty() {
        return availableQty;
    }
}
