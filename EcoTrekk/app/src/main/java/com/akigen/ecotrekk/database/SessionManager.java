package com.akigen.ecotrekk.database;

import android.content.Context;

import com.akigen.ecotrekk.database.entity.UserEntity;
import com.akigen.ecotrekk.database.repository.UserRepository;

/**
 * Session manager for handling user authentication state
 * Simplifies user login/logout operations
 */
public class SessionManager {
    
    private static volatile SessionManager instance;
    private final UserRepository userRepository;
    private final Context context;
    
    private SessionManager(Context context) {
        this.context = context.getApplicationContext();
        this.userRepository = UserRepository.getInstance(context);
    }
    
    /**
     * Get singleton instance
     */
    public static SessionManager getInstance(Context context) {
        if (instance == null) {
            synchronized (SessionManager.class) {
                if (instance == null) {
                    instance = new SessionManager(context);
                }
            }
        }
        return instance;
    }
    
    /**
     * Save user login session
     */
    public void loginUser(String userId, String email, String name, String phone, String authToken) {
        UserEntity user = new UserEntity(userId, email, name, phone, authToken);
        userRepository.saveUser(user);
    }
    
    /**
     * Get currently logged-in user
     */
    public UserEntity getCurrentUser() {
        return userRepository.getActiveUser();
    }
    
    /**
     * Check if user is logged in
     */
    public boolean isLoggedIn() {
        return getCurrentUser() != null;
    }
    
    /**
     * Update user profile
     */
    public void updateUserProfile(String userId, String name, String phone) {
        UserEntity user = userRepository.getUserById(userId);
        if (user != null) {
            user.setName(name);
            user.setPhone(phone);
            user.setLastLogin(System.currentTimeMillis());
            userRepository.updateUser(user);
        }
    }
    
    /**
     * Update auth token
     */
    public void updateAuthToken(String userId, String newToken) {
        UserEntity user = userRepository.getUserById(userId);
        if (user != null) {
            user.setAuthToken(newToken);
            userRepository.updateUser(user);
        }
    }
    
    /**
     * Logout current user
     */
    public void logout() {
        UserEntity user = getCurrentUser();
        if (user != null) {
            userRepository.logoutUser(user.getUserId());
        }
    }
    
    /**
     * Logout all users and clear sessions
     */
    public void logoutAll() {
        userRepository.logoutAll();
    }
    
    /**
     * Get auth token if user is logged in
     */
    public String getAuthToken() {
        UserEntity user = getCurrentUser();
        return user != null ? user.getAuthToken() : null;
    }
    
    /**
     * Get current user email
     */
    public String getUserEmail() {
        UserEntity user = getCurrentUser();
        return user != null ? user.getEmail() : null;
    }
    
    /**
     * Get current user name
     */
    public String getUserName() {
        UserEntity user = getCurrentUser();
        return user != null ? user.getName() : null;
    }
}
