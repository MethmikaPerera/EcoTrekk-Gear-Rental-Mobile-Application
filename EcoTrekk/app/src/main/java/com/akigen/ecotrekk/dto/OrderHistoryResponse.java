package com.akigen.ecotrekk.dto;

import java.util.List;

public class OrderHistoryResponse {
    private Long id;
    private String orderNumber;
    private Long customerId;
    private String startDate;
    private String endDate;
    private String pickupHubName;
    private Double pickupHubLatitude;
    private Double pickupHubLongitude;
    private Double subtotal;
    private Double refundableDeposit;
    private Double totalAmount;
    private String status;
    private Long createdAt;
    private List<OrderItemResponse> items;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public String getPickupHubName() {
        return pickupHubName;
    }

    public void setPickupHubName(String pickupHubName) {
        this.pickupHubName = pickupHubName;
    }

    public Double getPickupHubLatitude() {
        return pickupHubLatitude;
    }

    public void setPickupHubLatitude(Double pickupHubLatitude) {
        this.pickupHubLatitude = pickupHubLatitude;
    }

    public Double getPickupHubLongitude() {
        return pickupHubLongitude;
    }

    public void setPickupHubLongitude(Double pickupHubLongitude) {
        this.pickupHubLongitude = pickupHubLongitude;
    }

    public Double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(Double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public Double getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(Double subtotal) {
        this.subtotal = subtotal;
    }

    public Double getRefundableDeposit() {
        return refundableDeposit;
    }

    public void setRefundableDeposit(Double refundableDeposit) {
        this.refundableDeposit = refundableDeposit;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Long createdAt) {
        this.createdAt = createdAt;
    }

    public List<OrderItemResponse> getItems() {
        return items;
    }

    public void setItems(List<OrderItemResponse> items) {
        this.items = items;
    }
}
