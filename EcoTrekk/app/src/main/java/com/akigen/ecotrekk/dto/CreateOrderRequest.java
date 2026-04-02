package com.akigen.ecotrekk.dto;

public class CreateOrderRequest {
    private String collectionHubName;
    private long startDateUtc;  // milliseconds since epoch
    private long endDateUtc;    // milliseconds since epoch

    public CreateOrderRequest() {
    }

    public CreateOrderRequest(String collectionHubName) {
        this.collectionHubName = collectionHubName;
    }

    public CreateOrderRequest(String collectionHubName, long startDateUtc, long endDateUtc) {
        this.collectionHubName = collectionHubName;
        this.startDateUtc = startDateUtc;
        this.endDateUtc = endDateUtc;
    }

    public String getCollectionHubName() {
        return collectionHubName;
    }

    public void setCollectionHubName(String collectionHubName) {
        this.collectionHubName = collectionHubName;
    }

    public long getStartDateUtc() {
        return startDateUtc;
    }

    public void setStartDateUtc(long startDateUtc) {
        this.startDateUtc = startDateUtc;
    }

    public long getEndDateUtc() {
        return endDateUtc;
    }

    public void setEndDateUtc(long endDateUtc) {
        this.endDateUtc = endDateUtc;
    }
}
