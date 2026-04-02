package com.akigen.ecotrekk.dto;

public class CartItemResponse {
    private Long id;
    private Long cartId;
    private Long gearId;
    private String gearName;
    private String category;
    private Double pricePerDay;
    private Double depositAmount;
    private String description;
    private Long hubId;
    private String hubName;
    private Integer quantity;
    private String startDate;
    private String endDate;
    private Long addedAt;
    private String imageUrl;
    private Integer availableQty;

    public Long getId() {
        return id;
    }

    /** Alias for compatibility with code expecting getCartItemId() */
    public Long getCartItemId() {
        return id;
    }

    public Long getCartId() {
        return cartId;
    }

    public Long getGearId() {
        return gearId;
    }

    public String getGearName() {
        return gearName;
    }

    public String getCategory() {
        return category;
    }

    public Double getPricePerDay() {
        return pricePerDay;
    }

    public Double getDepositAmount() {
        return depositAmount;
    }

    public String getDescription() {
        return description;
    }

    public Long getHubId() {
        return hubId;
    }

    public String getHubName() {
        return hubName;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public String getStartDate() {
        return startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public Long getAddedAt() {
        return addedAt;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public Integer getAvailableQty() {
        return availableQty;
    }
}
