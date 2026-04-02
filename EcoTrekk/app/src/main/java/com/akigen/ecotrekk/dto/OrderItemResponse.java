package com.akigen.ecotrekk.dto;

public class OrderItemResponse {
    private Long id;
    private Long gearId;
    private String gearName;
    private String gearCategory;
    private String imageUrl;
    private Integer quantity;
    private Double rentPrice;
    private Double refundableDeposit;

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

    public String getGearCategory() {
        return gearCategory;
    }

    public void setGearCategory(String gearCategory) {
        this.gearCategory = gearCategory;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public Double getRentPrice() {
        return rentPrice;
    }

    public void setRentPrice(Double rentPrice) {
        this.rentPrice = rentPrice;
    }

    public Double getRefundableDeposit() {
        return refundableDeposit;
    }

    public void setRefundableDeposit(Double refundableDeposit) {
        this.refundableDeposit = refundableDeposit;
    }
}
