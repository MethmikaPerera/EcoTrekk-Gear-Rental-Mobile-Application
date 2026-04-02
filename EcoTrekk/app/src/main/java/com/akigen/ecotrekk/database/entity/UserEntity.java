package com.akigen.ecotrekk.database.entity;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

/**
 * User session entity for storing authenticated user information locally
 */
@Entity(tableName = "users")
public class UserEntity {
    
    @PrimaryKey(autoGenerate = true)
    private long id;
    
    private String userId;
    private String email;
    private String name;
    private String phone;
    private String authToken;
    private long lastLogin;
    private boolean isLoggedIn;
    
    @Ignore
    public UserEntity() {
    }
    
    public UserEntity(String userId, String email, String name, String phone, String authToken) {
        this.userId = userId;
        this.email = email;
        this.name = name;
        this.phone = phone;
        this.authToken = authToken;
        this.lastLogin = System.currentTimeMillis();
        this.isLoggedIn = true;
    }
    
    // Getters and Setters
    public long getId() {
        return id;
    }
    
    public void setId(long id) {
        this.id = id;
    }
    
    public String getUserId() {
        return userId;
    }
    
    public void setUserId(String userId) {
        this.userId = userId;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getPhone() {
        return phone;
    }
    
    public void setPhone(String phone) {
        this.phone = phone;
    }
    
    public String getAuthToken() {
        return authToken;
    }
    
    public void setAuthToken(String authToken) {
        this.authToken = authToken;
    }
    
    public long getLastLogin() {
        return lastLogin;
    }
    
    public void setLastLogin(long lastLogin) {
        this.lastLogin = lastLogin;
    }
    
    public boolean isLoggedIn() {
        return isLoggedIn;
    }
    
    public void setLoggedIn(boolean loggedIn) {
        isLoggedIn = loggedIn;
    }
}
