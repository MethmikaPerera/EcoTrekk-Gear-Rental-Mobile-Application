package com.akigen.ecotrekk.dto;

public class CartAddRequest {
    private final Long gearId;
    private final int quantity;
    private final Long hubId;
    private final String startDate;
    private final String endDate;

    public CartAddRequest(Long gearId, int quantity, Long hubId, String startDate, String endDate) {
        this.gearId = gearId;
        this.quantity = quantity;
        this.hubId = hubId;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public Long getGearId() {
        return gearId;
    }

    public int getQuantity() {
        return quantity;
    }

    public Long getHubId() {
        return hubId;
    }

    public String getStartDate() {
        return startDate;
    }

    public String getEndDate() {
        return endDate;
    }
}
