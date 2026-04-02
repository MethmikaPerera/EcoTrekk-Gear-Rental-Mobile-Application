package com.akigen.ecotrekk.dto;

public class WishlistItemResponse {
    private Long id;
    private Long gearId;
    private String gearName;
    private String category;
    private Double pricePerDay;
    private Double depositAmount;
    private String description;
    private String imageUrl;
    private Long addedAt;

    // Default constructor
    public WishlistItemResponse() {
    }

    // Full constructor
    public WishlistItemResponse(Long id, Long gearId, String gearName, String category, 
                                Double pricePerDay, Double depositAmount, String description, 
                                String imageUrl, Long addedAt) {
        this.id = id;
        this.gearId = gearId;
        this.gearName = gearName;
        this.category = category;
        this.pricePerDay = pricePerDay;
        this.depositAmount = depositAmount;
        this.description = description;
        this.imageUrl = imageUrl;
        this.addedAt = addedAt;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getGearId() {
        return gearId;
    }

    public void setGearId(Long gearId) {
        this.gearId = gearId;
    }

    public String getGearName() {
        return gearName;
    }

    public void setGearName(String gearName) {
        this.gearName = gearName;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Double getPricePerDay() {
        return pricePerDay;
    }

    public void setPricePerDay(Double pricePerDay) {
        this.pricePerDay = pricePerDay;
    }

    public Double getDepositAmount() {
        return depositAmount;
    }

    public void setDepositAmount(Double depositAmount) {
        this.depositAmount = depositAmount;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public Long getAddedAt() {
        return addedAt;
    }

    public void setAddedAt(Long addedAt) {
        this.addedAt = addedAt;
    }
}
