package com.akigen.ecotrekk.database.entity;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

/**
 * Favorite/Wishlist entity for storing user's favorite gears locally
 */
@Entity(tableName = "favorites")
public class FavoriteEntity {
    
    @PrimaryKey(autoGenerate = true)
    private long id;
    
    private long gearId;
    private String gearName;
    private String category;
    private double pricePerDay;
    private double depositAmount;
    private String description;
    private String imageUrl;
    private long addedAt;    // timestamp
    
    @Ignore
    public FavoriteEntity() {
    }
    
    public FavoriteEntity(long gearId, String gearName, String category, 
                         double pricePerDay, double depositAmount, 
                         String description, String imageUrl) {
        this.gearId = gearId;
        this.gearName = gearName;
        this.category = category;
        this.pricePerDay = pricePerDay;
        this.depositAmount = depositAmount;
        this.description = description;
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
    
    public long getAddedAt() {
        return addedAt;
    }
    
    public void setAddedAt(long addedAt) {
        this.addedAt = addedAt;
    }
}
