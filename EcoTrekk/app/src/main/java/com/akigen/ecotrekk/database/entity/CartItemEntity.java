package com.akigen.ecotrekk.database.entity;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

/**
 * Cart item entity for storing user's shopping cart items locally
 */
@Entity(tableName = "cart_items")
public class CartItemEntity {
    
    @PrimaryKey(autoGenerate = true)
    private long id;
    
    private long gearId;
    private String gearName;
    private String category;
    private int quantity;
    private long startDate;  // timestamp
    private long endDate;    // timestamp
    private double pricePerDay;
    private double depositAmount;
    private long hubId;
    private String hubName;
    private String imageUrl;
    private long addedAt;    // timestamp
    
    @Ignore
    public CartItemEntity() {
    }
    
    public CartItemEntity(long gearId, String gearName, String category, int quantity,
                         long startDate, long endDate, double pricePerDay,
                         double depositAmount, long hubId, String hubName, String imageUrl) {
        this.gearId = gearId;
        this.gearName = gearName;
        this.category = category;
        this.quantity = quantity;
        this.startDate = startDate;
        this.endDate = endDate;
        this.pricePerDay = pricePerDay;
        this.depositAmount = depositAmount;
        this.hubId = hubId;
        this.hubName = hubName;
        this.imageUrl = imageUrl;
        this.addedAt = System.currentTimeMillis();
    }
    
    // Getters and Setters
    public long getId() {
        return id;
    }
    
    public void setId(long id) {
        this.id = id;
    }
    
    public long getGearId() {
        return gearId;
    }
    
    public void setGearId(long gearId) {
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
    
    public int getQuantity() {
        return quantity;
    }
    
    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
    
    public long getStartDate() {
        return startDate;
    }
    
    public void setStartDate(long startDate) {
        this.startDate = startDate;
    }
    
    public long getEndDate() {
        return endDate;
    }
    
    public void setEndDate(long endDate) {
        this.endDate = endDate;
    }
    
    public double getPricePerDay() {
        return pricePerDay;
    }
    
    public void setPricePerDay(double pricePerDay) {
        this.pricePerDay = pricePerDay;
    }
    
    public double getDepositAmount() {
        return depositAmount;
    }
    
    public void setDepositAmount(double depositAmount) {
        this.depositAmount = depositAmount;
    }
    
    public long getHubId() {
        return hubId;
    }
    
    public void setHubId(long hubId) {
        this.hubId = hubId;
    }
    
    public String getHubName() {
        return hubName;
    }
    
    public void setHubName(String hubName) {
        this.hubName = hubName;
    }
    
    public String getImageUrl() {
        return imageUrl;
    }
    
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
    
    public long getAddedAt() {
        return addedAt;
    }
    
    public void setAddedAt(long addedAt) {
        this.addedAt = addedAt;
    }
    
    /**
     * Calculate total price for this cart item
     */
    public double getTotalPrice() {
        long rentalDays = getRentalDays();
        return pricePerDay * quantity * Math.max(1, rentalDays);
    }
    
    /**
     * Get number of rental days
     */
    public long getRentalDays() {
        if (startDate > 0 && endDate > 0) {
            return (endDate - startDate) / (1000 * 60 * 60 * 24);
        }
        return 1;
    }
}
