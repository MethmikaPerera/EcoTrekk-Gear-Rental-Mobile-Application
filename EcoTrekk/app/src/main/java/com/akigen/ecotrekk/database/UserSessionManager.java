package com.akigen.ecotrekk.database;

import android.content.Context;

import com.akigen.ecotrekk.database.entity.UserEntity;
import com.akigen.ecotrekk.database.repository.UserRepository;

/**
 * Database-backed user session manager for storing user authentication state
 * Complements the SharedPreferences-based SessionManager
 */
public class UserSessionManager {
    
    private static volatile UserSessionManager instance;
    private final UserRepository userRepository;
    private final Context context;
    
    private UserSessionManager(Context context) {
        this.context = context.getApplicationContext();
        this.userRepository = UserRepository.getInstance(context);
    }
    
    /**
     * Get singleton instance
     */
    public static UserSessionManager getInstance(Context context) {
        if (instance == null) {
            synchronized (UserSessionManager.class) {
                if (instance == null) {
                    instance = new UserSessionManager(context);
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
        // Avoid synchronous Room reads on main thread during UI-triggered logout.
        userRepository.logoutAll();
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
