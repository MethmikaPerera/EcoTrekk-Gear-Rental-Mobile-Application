package com.akigen.ecotrekk.database.repository;

import android.content.Context;

import com.akigen.ecotrekk.database.EcoTrekkDatabase;
import com.akigen.ecotrekk.database.dao.UserDao;
import com.akigen.ecotrekk.database.entity.UserEntity;

/**
 * Repository for managing user session data
 */
public class UserRepository {
    
    private static volatile UserRepository instance;
    private final UserDao userDao;
    
    private UserRepository(Context context) {
        EcoTrekkDatabase database = EcoTrekkDatabase.getInstance(context);
        this.userDao = database.userDao();
    }
    
    /**
     * Get singleton instance
     */
    public static UserRepository getInstance(Context context) {
        if (instance == null) {
            synchronized (UserRepository.class) {
                if (instance == null) {
                    instance = new UserRepository(context);
                }
            }
        }
        return instance;
    }
    
    /**
     * Save or update user login session
     */
    public void saveUser(UserEntity user) {
        new Thread(() -> {
            UserEntity existingUser = userDao.getUserById(user.getUserId());
            if (existingUser != null) {
                user.setId(existingUser.getId());
                userDao.updateUser(user);
            } else {
                userDao.insertUser(user);
            }
        }).start();
    }
    
    /**
     * Get currently logged in user
     */
    public UserEntity getActiveUser() {
        return userDao.getActiveUser();
    }
    
    /**
     * Get user by ID
     */
    public UserEntity getUserById(String userId) {
        return userDao.getUserById(userId);
    }
    
    /**
     * Get user by email
     */
    public UserEntity getUserByEmail(String email) {
        return userDao.getUserByEmail(email);
    }
    
    /**
     * Update user data
     */
    public void updateUser(UserEntity user) {
        new Thread(() -> userDao.updateUser(user)).start();
    }
    
    /**
     * Logout user - mark as not logged in
     */
    public void logoutUser(String userId) {
        new Thread(() -> {
            UserEntity user = userDao.getUserById(userId);
            if (user != null) {
                user.setLoggedIn(false);
                userDao.updateUser(user);
            }
        }).start();
    }
    
    /**
     * Logout all users
     */
    public void logoutAll() {
        new Thread(userDao::logoutAll).start();
    }
    
    /**
     * Delete user session
     */
    public void deleteUser(String userId) {
        new Thread(() -> userDao.deleteUserById(userId)).start();
    }
    
    /**
     * Clear all user data (for app reset/uninstall)
     */
    public void clearAllUsers() {
        new Thread(() -> userDao.logoutAll()).start();
    }
}
